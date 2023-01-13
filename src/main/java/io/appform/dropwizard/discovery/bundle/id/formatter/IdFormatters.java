package io.appform.dropwizard.discovery.bundle.id.formatter;

import lombok.experimental.UtilityClass;

@UtilityClass
public class IdFormatters {

    private static final IdFormatter originalIdFormatter = new DefaultIdFormatter();
    private static final IdFormatter base36IdFormatter = new Base36IdFormatter();

    public static IdFormatter original() {
        return originalIdFormatter;
    }

    public static IdFormatter base36() {
        return base36IdFormatter;
    }

}
