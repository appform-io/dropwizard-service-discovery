/*
 * Copyright (c) 2019 Santanu Sinha <santanu.sinha@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.appform.dropwizard.discovery.bundle.monitors;

import com.codahale.metrics.health.HealthCheck;
import io.appform.ranger.core.healthcheck.HealthcheckStatus;
import io.appform.ranger.core.healthservice.TimeEntity;
import io.appform.ranger.core.healthservice.monitor.IsolatedHealthMonitor;
import io.dropwizard.setup.Environment;

/**
 * This monitor calls dropwizard healthchecks every few secs.
 */
public class DropwizardHealthMonitor extends IsolatedHealthMonitor<HealthcheckStatus> {

    private final Environment environment;

    public DropwizardHealthMonitor(
            TimeEntity runInterval,
            long stalenessAllowedInMillis,
            Environment environment) {
        super("dropwizard-health-monitor", runInterval, stalenessAllowedInMillis);
        this.environment = environment;
    }

    @Override
    public HealthcheckStatus monitor() {
        return (null != environment.healthChecks()
                && environment.healthChecks()
                .runHealthChecks()
                .values()
                .stream()
                .allMatch(HealthCheck.Result::isHealthy))
               ? HealthcheckStatus.healthy
               : HealthcheckStatus.unhealthy;
    }
}
