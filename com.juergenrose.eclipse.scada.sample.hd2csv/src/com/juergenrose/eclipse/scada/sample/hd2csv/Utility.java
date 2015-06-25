package com.juergenrose.eclipse.scada.sample.hd2csv;

import java.util.Locale;

import org.apache.poi.ss.util.DateFormatConverter;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

public class Utility {
    static final DateTimeFormatter df = new DateTimeFormatterBuilder().appendYear(4, 4).appendLiteral("-").appendMonthOfYear(2).appendLiteral("-")
            .appendDayOfMonth(2).appendLiteral(" ").appendHourOfDay(2).appendLiteral(":").appendMinuteOfHour(2).appendLiteral(":").appendSecondOfMinute(2)
            .appendLiteral(".").appendMillisOfSecond(3).toFormatter();

    static final DateTimeFormatter dfs = new DateTimeFormatterBuilder().appendYear(4, 4).appendLiteral("-").appendMonthOfYear(2).appendLiteral("-")
            .appendDayOfMonth(2).appendLiteral(" ").appendHourOfDay(2).appendLiteral(":").appendMinuteOfHour(2).appendLiteral(":").appendSecondOfMinute(2)
            .toFormatter();

    static final DateTimeFormatter dfm = new DateTimeFormatterBuilder().appendYear(4, 4).appendLiteral("-").appendMonthOfYear(2).appendLiteral("-")
            .appendDayOfMonth(2).appendLiteral(" ").appendHourOfDay(2).appendLiteral(":").appendMinuteOfHour(2).toFormatter();

    static final DateTimeFormatter dfh = new DateTimeFormatterBuilder().appendYear(4, 4).appendLiteral("-").appendMonthOfYear(2).appendLiteral("-")
            .appendDayOfMonth(2).appendLiteral(" ").appendHourOfDay(2).toFormatter();

    static final DateTimeFormatter dfd = new DateTimeFormatterBuilder().appendYear(4, 4).appendLiteral("-").appendMonthOfYear(2).appendLiteral("-")
            .appendDayOfMonth(2).toFormatter();
    
    static final String excelDateFormat = DateFormatConverter.convert(Locale.GERMAN, "yyyy-MM-dd HH:mm:ss");
}
