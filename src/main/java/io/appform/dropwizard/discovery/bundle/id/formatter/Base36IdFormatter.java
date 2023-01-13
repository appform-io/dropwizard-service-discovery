package io.appform.dropwizard.discovery.bundle.id.formatter;

import lombok.val;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.math.BigInteger;

public class Base36IdFormatter implements IdFormatter {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyMMddHHmmssSSS");

    @Override
    public String transform(final String prefix,
                            final DateTime dateTime,
                            final int nodeId,
                            final int randomNonce) {
        val uniqueId = String.format("%s%04d%03d", DATE_TIME_FORMATTER.print(dateTime), nodeId, randomNonce);
        return String.format("%s%s", prefix, toBase36(uniqueId));
    }

    private static String toBase36(final String payload) {
        return new BigInteger(payload).toString(36).toUpperCase();
    }
}
