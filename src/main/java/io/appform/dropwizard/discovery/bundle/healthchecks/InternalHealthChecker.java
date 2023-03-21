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

package io.appform.dropwizard.discovery.bundle.healthchecks;

import io.appform.ranger.core.healthcheck.Healthcheck;
import io.appform.ranger.core.healthcheck.HealthcheckStatus;
import java.util.List;

/**
 * Evaluates all registered healthchecks
 */
public class InternalHealthChecker implements Healthcheck {
    private final List<Healthcheck> healthchecks;

    public InternalHealthChecker(List<Healthcheck> healthchecks) {
        this.healthchecks = healthchecks;
    }

    @Override
    public HealthcheckStatus check() {
        return healthchecks.stream()
                .map(Healthcheck::check)
                .filter(healthcheckStatus -> healthcheckStatus == HealthcheckStatus.unhealthy)
                .findFirst()
                .orElse(HealthcheckStatus.healthy);
    }
}
