package io.appform.dropwizard.discovery.bundle.id.formatter;

import java.math.BigInteger;
import java.time.ZonedDateTime;

public class Base36IdFormatter implements IdFormatter {

    private final IdFormatter idFormatter;

    public Base36IdFormatter(IdFormatter idFormatter) {
        this.idFormatter = idFormatter;
    }

    @Override
    public String format(final ZonedDateTime dateTime,
                         final int nodeId,
                         final int randomNonce) {
        return toBase36(idFormatter.format(dateTime, nodeId, randomNonce));
    }

    private static String toBase36(final String payload) {
        return new BigInteger(payload).toString(36).toUpperCase();
    }
}
