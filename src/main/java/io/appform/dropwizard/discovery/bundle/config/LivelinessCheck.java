package io.appform.dropwizard.discovery.bundle.config;

import io.appform.dropwizard.discovery.bundle.Constants;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.Min;

@Value
@Builder
@Jacksonized
public class LivelinessCheck {
    @Builder.Default
    int initialDelayForMonitor = Constants.DEFAULT_INITIAL_DELAY;
    @Builder.Default
    int checkInterval = Constants.DEFAULT_DW_CHECK_INTERVAL;
    @Builder.Default
    int stalesnessInterval = Constants.DEFAULT_DW_STALENESS_SECONDS;
    @Min(0)
    @Builder.Default
    double unhealthyThreshold = Constants.DEFAULT_LIVELINESS_THRESHOLD;
}
