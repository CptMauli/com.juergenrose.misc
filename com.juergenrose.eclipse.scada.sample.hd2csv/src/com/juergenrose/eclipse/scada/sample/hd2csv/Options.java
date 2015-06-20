package com.juergenrose.eclipse.scada.sample.hd2csv;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

public class Options {

    static final DateTime now = new DateTime(DateTimeZone.UTC);

    static enum AggregationOption {
        AVG, MIN, MAX
    };

    @Option(name = "-?", usage = "prints this help", aliases = { "--help" })
    boolean help;

    @Option(name = "-version", usage = "prints information about its name, version, origin and legal status", aliases = { "--version" })
    boolean version;

    @Option(name = "-h", usage = "sets the host name", aliases = { "--host" }, forbids = { "-u" })
    String host = "localhost";

    @Option(name = "-p", usage = "sets the port", aliases = { "--port" }, forbids = { "-u" })
    int port = 8082;

    @Option(name = "-a", usage = "sets the aggregation", aliases = { "--aggregation" }, forbids = { "-u" })
    AggregationOption aggregation = AggregationOption.AVG;

    @Option(name = "-u", usage = "alternatively sets a complete URL (without query parameters)", aliases = { "--url" }, forbids = { "-h", "-p", "-a" })
    URI url;

    @Option(name = "-f", usage = "start from (default is begin of current hour)", aliases = { "--from" })
    DateTime from = now.hourOfDay().roundFloorCopy();

    @Option(name = "-t", usage = "end at (default is end of current hour)", aliases = { "--to" })
    DateTime to = now.hourOfDay().roundCeilingCopy();

    @Option(name = "-n", usage = "number of values to query", aliases = { "--no" })
    int no = 60;

    @Option(name = "-w", usage = "write to file (default is stdout)", aliases = { "--file" })
    File file;

    @Option(name = "-e", usage = "append to given file (implies skipping header)", aliases = { "--append" })
    boolean append;

    @Option(name = "-s", usage = "skips the header for the csv file", aliases = { "--skip-header" })
    boolean skipHeader = false;

    @Option(name = "-q", usage = "sets what quality is required to consider the value as valid [between 0.0 and 1.0", aliases = { "--quality" })
    double quality = 1.0;

    @Option(name = "-v", usage = "sets verbosity", aliases = { "--verbose" })
    boolean verbose = false;

    @Argument(metaVar = "ITEM", required = true, usage = "name of item(s) to query")
    List<String> items = new ArrayList<String>();

}
