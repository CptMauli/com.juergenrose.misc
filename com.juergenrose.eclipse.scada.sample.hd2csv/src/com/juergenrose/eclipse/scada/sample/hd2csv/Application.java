package com.juergenrose.eclipse.scada.sample.hd2csv;

import org.joda.time.DateTime;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionHandlerRegistry;
import org.kohsuke.args4j.ParserProperties;

public class Application {

    public static void main(String[] args) throws Exception {

        OptionHandlerRegistry.getRegistry().registerHandler(DateTime.class, DateTimeOptionHandler.class);
        final Options options = new Options();
        final CmdLineParser parser = new CmdLineParser(options, ParserProperties.defaults().withUsageWidth(80));
        try {
            parser.parseArgument(args);
            // print version information screen
            if (options.version) {
                System.out.println("hd2csv " + Application.class.getPackage().getImplementationVersion()
                        + "\nCopyright (C) 2015 Juergen Rose <juergen.rose@ibh-systems.com>" //
                        + "\nLicense: The MIT License (MIT) <http://opensource.org/licenses/MIT>" //
                        + "\nThis is free software: you are free to change and redistribute it." //
                        + "\nThere is NO WARRANTY, to the extent permitted by law.");
            }
            // print help screen
            if (options.help) {
                parser.printUsage(System.out);
            }
            // if either help or version is set, then just exit here already
            if (options.help || options.version) {
                return;
            }
            new Hd2csv(options).run();
        } catch (CmdLineException e) {
            System.err.println(e.getLocalizedMessage());
            parser.printUsage(System.err);
        }
    }

}
