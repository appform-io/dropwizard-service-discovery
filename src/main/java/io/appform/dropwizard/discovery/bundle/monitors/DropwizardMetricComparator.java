package io.appform.dropwizard.discovery.bundle.monitors;

import com.codahale.metrics.MetricRegistry;
import io.appform.ranger.core.healthcheck.HealthcheckStatus;
import io.appform.ranger.core.healthservice.TimeEntity;
import io.appform.ranger.core.healthservice.monitor.IsolatedHealthMonitor;
import io.dropwizard.setup.Environment;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import lombok.var;

import javax.inject.Singleton;


/*
    Users can use this metricComparator class, so long as the value to be compared is a number and can
    build multiple isolatedHealthMonitors as required.
 */
@Singleton
@Slf4j
public class DropwizardMetricComparator extends IsolatedHealthMonitor<HealthcheckStatus> {

    private final String metricName;
    private final MetricRegistry metricRegistry;
    private final double unhealthyThreshold;

    @Builder
    public DropwizardMetricComparator(TimeEntity runInterval,
                                      long stalenessAllowedInMillis,
                                      Environment environment,
                                      String metricName,
                                      double unhealthyThreshold) {
        super(String.format("/%s-%s", "dw-metric-monitor", metricName), runInterval,  stalenessAllowedInMillis);

        this.metricRegistry = environment.metrics();
        this.metricName = metricName;
        this.unhealthyThreshold = unhealthyThreshold;
    }


    @Override
    public HealthcheckStatus monitor() {
        var healthcheckStatus = HealthcheckStatus.healthy;
        val gauge = metricRegistry.getGauges().get(metricName);
        if(null != gauge && null != gauge.getValue()){
            try{
                double tUtilization = Double.parseDouble(gauge.getValue().toString());
                if(tUtilization >= unhealthyThreshold){
                    healthcheckStatus = HealthcheckStatus.unhealthy;
                }
            }catch (Exception e){
                log.error("There is an exception {} while trying to fetch the metric for {}", e, metricName);
            }
        }
        return healthcheckStatus;
    }
}
