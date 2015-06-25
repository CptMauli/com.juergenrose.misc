package com.juergenrose.eclipse.scada.sample.hd2csv;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.DateTime;

import com.google.common.reflect.TypeToken;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class Hd2csv {

	private final Options options;

	private static final String csvSeparator = ",";

	public Hd2csv(Options options) {
		this.options = options;
	}

	public void run() throws IOException {
		final List<Query> queries = makeQueries();
		final SortedMap<Long, Map<String, Double>> result = new TreeMap<Long, Map<String, Double>>();
		for (Query query : queries) {
			String json = doQuery(query);
			if (json != null) {
				Type listType = new TypeToken<List<HDEntry>>() {
					private static final long serialVersionUID = 1L;
				}.getType();
				List<HDEntry> entries = new GsonBuilder()
						.registerTypeAdapter(DateTime.class, new DateTimeDeserializer()).create()
						.fromJson(json, listType);
				for (HDEntry entry : entries) {
					// // {timestamp=2015-06-20 17:44:00.000, value=9843.0,
					// quality=1.0, manual=0.0}
					if (!result.containsKey(entry.timestamp.toInstant().getMillis())) {
						result.put(entry.timestamp.toInstant().getMillis(),
								new HashMap<String, Double>(options.items.size()));
					}
					result.get(entry.timestamp.toInstant().getMillis()).put(query.item.intern(),
							entry.quality >= options.quality ? entry.value : null);
				}
			}
		}

		if (options.asExcel) {
			writeExcel(result);
		} else {
			writeCsv(result);
		}
	}

	private void writeCsv(SortedMap<Long, Map<String, Double>> result) throws IOException {
		Writer writer;
		if (options.file != null) {
			writer = new BufferedWriter(new FileWriter(options.file, options.append));
		} else {
			writer = new BufferedWriter(new OutputStreamWriter(System.out));
		}
		if (!(options.skipHeader || options.append)) {
			writer.append("sep=;" + System.lineSeparator());
			writer.append(csvQuote("timestamp"));
			for (String item : options.items) {
				writer.append(csvSeparator);
				writer.append(csvQuote(item));
			}
			writer.append(System.lineSeparator());
			writer.flush();
		}
		for (long millis : result.keySet()) {
			Map<String, Double> values = result.get(millis);
			writer.append(Utility.dfs.print(millis));
			for (String item : options.items) {
				writer.append(csvSeparator);
				writer.append(values.get(item) == null ? "" : "" + values.get(item));
			}
			writer.append(System.lineSeparator());
			writer.flush();
		}
		writer.close();
	}

	private void writeExcel(SortedMap<Long, Map<String, Double>> result) throws IOException {
		Workbook workbook;
		Sheet sheet;
		int rowNum = 0;
		if (options.append && options.file.exists()) {
			workbook = new HSSFWorkbook(new FileInputStream(options.file));
			sheet = workbook.getSheetAt(0);
			rowNum = sheet.getLastRowNum();
		} else {
			workbook = new HSSFWorkbook();
			sheet = workbook.createSheet();
		}
		final CellStyle dateCellStyle = workbook.createCellStyle();
		final DataFormat poiFormat = workbook.createDataFormat();
		dateCellStyle.setDataFormat(poiFormat.getFormat(Utility.excelDateFormat));

		if (!(options.skipHeader || (options.append && rowNum > 0))) {
			int colNum = 0;
			Row row = sheet.createRow(rowNum++);
			Cell cell = row.createCell(colNum++);
			cell.setCellValue("timestamp");
			for (String item : options.items) {
				cell = row.createCell(colNum++);
				cell.setCellValue(item);
			}
		}
		for (long millis : result.keySet()) {
			int colNum = 0;
			Map<String, Double> values = result.get(millis);
			Row row = sheet.createRow(rowNum++);
			Cell cell = row.createCell(colNum++);
			cell.setCellValue(new Date(millis));
			cell.setCellStyle(dateCellStyle);
			for (String item : options.items) {
				cell = row.createCell(colNum++);
				if (values.get(item) == null) {
					cell.setCellType(Cell.CELL_TYPE_BLANK);
				} else {
					cell.setCellValue(values.get(item));
				}
			}
		}
		sheet.setDefaultColumnWidth(40);
		sheet.autoSizeColumn(0);

		workbook.write(new FileOutputStream(options.file));
		workbook.close();
	}

	private String csvQuote(String toQuote) {
		return "\"" + toQuote.replace("\"", "\"\"") + "\"";
	}

	private String doQuery(Query query) {
		try {
			if (options.verbose) {
				System.err.println(String.format("executing request to %s with query parameters from=%s, to=%s, no=%s",
						query.uri, query.queryParams.get("from"), query.queryParams.get("to"),
						query.queryParams.get("no")));
			}
			HttpResponse<String> jsonResponse = Unirest.get(query.uri.toString()).header("accept", "application/json")
					.header("accept", "application/javascript").queryString("from", query.queryParams.get("from"))
					.queryString("to", query.queryParams.get("to")).queryString("no", query.queryParams.get("no"))
					.asString();
			if (jsonResponse.getStatus() == 200) {
				return jsonResponse.getBody();
			}
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		return null;
	}

	private List<Query> makeQueries() {
		final List<Query> queries = new ArrayList<Hd2csv.Query>(options.items.size());
		for (String item : options.items) {
			URI uri;
			if (options.url != null) {
				uri = options.url;
			} else {
				uri = URI.create("http://" + options.host + ":" + options.port + "/org.eclipse.scada.hd/items/" + item
						+ "/" + options.aggregation + "/");
			}
			Query query = new Query();
			query.item = item;
			query.uri = uri;
			query.queryParams = new HashMap<String, String>(3);
			query.queryParams.put("from", Utility.df.print(options.from.getMillis()));
			query.queryParams.put("to", Utility.df.print(options.to.getMillis()));
			query.queryParams.put("no", "" + options.no);
			queries.add(query);
		}
		return queries;
	}

	class Query {
		String item;

		URI uri;

		Map<String, String> queryParams;
	}

	class HDEntry {
		DateTime timestamp;

		Double value;

		Double quality;

		Double manual;
	}

	class DateTimeDeserializer implements JsonDeserializer<DateTime> {
		public DateTime deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context)
				throws JsonParseException {
			return Utility.df.parseDateTime(jsonElement.getAsString());
		}
	}
}
