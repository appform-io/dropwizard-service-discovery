package io.appform.dropwizard.discovery.bundle.id.formatter;

import org.joda.time.DateTime;

public interface IdFormatter {

    String format(final DateTime dateTime,
                  final int nodeId,
                  final int randomNonce);


}
