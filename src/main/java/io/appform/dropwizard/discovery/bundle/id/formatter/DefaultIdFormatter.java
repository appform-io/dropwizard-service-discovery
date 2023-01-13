package io.appform.dropwizard.discovery.bundle.id.formatter;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DefaultIdFormatter implements IdFormatter {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyMMddHHmmssSSS");

    @Override
    public String format(final String prefix,
                         final DateTime dateTime,
                         final int nodeId,
                         final int randomNonce) {
        return String.format("%s%s%04d%03d", prefix, DATE_TIME_FORMATTER.print(dateTime), nodeId, randomNonce);
    }
}
