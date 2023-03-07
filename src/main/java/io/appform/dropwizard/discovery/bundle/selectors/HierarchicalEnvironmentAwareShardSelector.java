/*
 * Copyright (c) 2022 Santanu Sinha <santanu.sinha@gmail.com>
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

package io.appform.dropwizard.discovery.bundle.selectors;

import com.google.common.base.Strings;
import io.appform.ranger.common.server.ShardInfo;
import io.appform.ranger.core.finder.serviceregistry.MapBasedServiceRegistry;
import io.appform.ranger.core.model.ServiceNode;
import io.appform.ranger.core.model.ShardSelector;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *
 */
@Slf4j
public class HierarchicalEnvironmentAwareShardSelector implements ShardSelector<ShardInfo, MapBasedServiceRegistry<ShardInfo>> {
    private static final String DEFAULT_SEPARATOR = ".";
    private static final Predicate<ShardInfo> DEFAULT_PREDICATE = shardInfo -> true;

    private final String environment;
    private final String separator;

    public HierarchicalEnvironmentAwareShardSelector(String environment) {
        this(environment, DEFAULT_SEPARATOR);
    }

    public HierarchicalEnvironmentAwareShardSelector(String environment, String separator) {
        this.environment = environment;
        this.separator = separator;
    }

    @Override
    public List<ServiceNode<ShardInfo>> nodes(
            Predicate<ShardInfo> criteria, MapBasedServiceRegistry<ShardInfo> serviceRegistry) {
        val serviceNodes = serviceRegistry.nodes();
        val serviceName = serviceRegistry.getService().getServiceName();
        val evalPredicate = null != criteria
                            ? criteria
                            : DEFAULT_PREDICATE;
        for (val env : new IterableEnvironment(environment, separator)) {
            val eligibleNodes = serviceNodes.entries()
                    .stream()
                    .filter(e -> e.getKey().getEnvironment().equals(env.environment) && evalPredicate.test(e.getKey()))
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());
            if (!eligibleNodes.isEmpty()) {
                log.debug("Effective environment for discovery of {} is {}", serviceName, env.environment);
                return eligibleNodes;
            }
            log.trace("No nodes found for environment: {}", env.environment);
        }
        log.warn("No valid nodes could be found for environment: {}", environment);
        return Collections.emptyList();
    }

    private static final class IterableEnvironment implements Iterable<IterableEnvironment> {
        private final String environment;
        private final String separator;

        private IterableEnvironment(String environment, String separator) {
            this.environment = environment;
            this.separator = separator;
        }

        @Override
        public Iterator<IterableEnvironment> iterator() {
            return new EnvironmentIterator(environment, separator);
        }

        public static final class EnvironmentIterator implements Iterator<IterableEnvironment> {

            private String remainingEnvironment;
            private final String separator;

            public EnvironmentIterator(String remainingEnvironment, String separator) {
                this.remainingEnvironment = remainingEnvironment;
                this.separator = separator;
            }

            @Override
            public boolean hasNext() {
                return !Strings.isNullOrEmpty(remainingEnvironment);
            }

            @Override
            public IterableEnvironment next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                log.debug("Effective environment for discovery is {}", remainingEnvironment);
                val shardInfo = new IterableEnvironment(remainingEnvironment, separator);
                val sepIndex = remainingEnvironment.indexOf(this.separator);
                remainingEnvironment = sepIndex < 0
                                       ? ""
                                       : remainingEnvironment.substring(0, sepIndex);
                return shardInfo;
            }
        }
    }
}
