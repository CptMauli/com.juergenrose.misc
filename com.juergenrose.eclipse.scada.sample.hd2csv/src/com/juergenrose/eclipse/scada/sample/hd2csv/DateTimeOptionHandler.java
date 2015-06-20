package com.juergenrose.eclipse.scada.sample.hd2csv;

import org.joda.time.DateTime;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OneArgumentOptionHandler;
import org.kohsuke.args4j.spi.Setter;

public class DateTimeOptionHandler extends OneArgumentOptionHandler<DateTime> {

    public DateTimeOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super DateTime> setter) {
        super(parser, option, setter);
    }

    @Override
    public String getDefaultMetaVariable() {
        return "DATETIME";
    }

    @Override
    protected DateTime parse(String argument) throws NumberFormatException, CmdLineException {
        try {
            if (argument.length() == 10) {
                return Utility.dfd.parseDateTime(argument);
            } else if (argument.length() == 13) {
                return Utility.dfh.parseDateTime(argument);
            } else if (argument.length() == 16) {
                return Utility.dfm.parseDateTime(argument);
            } else if (argument.length() == 19) {
                return Utility.dfs.parseDateTime(argument);
            } else {
                return Utility.df.parseDateTime(argument);
            }
        } catch (Exception e) {
            throw new CmdLineException(owner, e);
        }
    }
}
