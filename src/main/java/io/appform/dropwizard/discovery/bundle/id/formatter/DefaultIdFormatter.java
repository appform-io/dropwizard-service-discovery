package io.appform.dropwizard.discovery.bundle.id.formatter;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DefaultIdFormatter implements IdFormatter {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyMMddHHmmssSSS");

    @Override
    public String format(final ZonedDateTime dateTime,
                         final int nodeId,
                         final int randomNonce) {
        return String.format("%s%04d%03d", DATE_TIME_FORMATTER.format(dateTime), nodeId, randomNonce);
    }
}
