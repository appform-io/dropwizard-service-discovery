package io.appform.dropwizard.discovery.bundle.id.formatter;

import java.time.ZonedDateTime;

public interface IdFormatter {

    String format(final ZonedDateTime dateTime,
                  final int nodeId,
                  final int randomNonce);


}
