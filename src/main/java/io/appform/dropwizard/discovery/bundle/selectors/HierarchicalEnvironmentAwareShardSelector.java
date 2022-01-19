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

import com.google.common.base.Preconditions;
import com.google.common.collect.ListMultimap;
import io.appform.dropwizard.discovery.bundle.Constants;
import io.appform.ranger.common.server.ShardInfo;
import io.appform.ranger.core.finder.serviceregistry.MapBasedServiceRegistry;
import io.appform.ranger.core.model.ServiceNode;
import io.appform.ranger.core.model.ShardSelector;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *
 */
@Slf4j
public class HierarchicalEnvironmentAwareShardSelector implements ShardSelector<ShardInfo, MapBasedServiceRegistry<ShardInfo>> {

    private List<ServiceNode<ShardInfo>> allNodes(ListMultimap<ShardInfo, ServiceNode<ShardInfo>> serviceNodes) {
        return serviceNodes.asMap()
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<ServiceNode<ShardInfo>> nodes(
            Predicate<ShardInfo> predicate, MapBasedServiceRegistry<ShardInfo> serviceRegistry) {
        Preconditions.checkArgument(predicate instanceof HierarchicalSelectionPredicate);
        val requiredInfo = ((HierarchicalSelectionPredicate) predicate).getShardInfo();
        val serviceNodes = serviceRegistry.nodes();
        val serviceName = serviceRegistry.getService().getServiceName();
        val environment = requiredInfo.getEnvironment();

        if (Objects.equals(environment, Constants.ALL_ENV)) {
            return allNodes(serviceNodes);
        }
        for (ShardInfo shardInfo : requiredInfo) {
            val currentEnvNodes = serviceNodes.get(shardInfo);
            if (!currentEnvNodes.isEmpty()) {
                log.debug("Effective environment for discovery of {} is {}", serviceName, environment);
                return currentEnvNodes;
            }
        }
        return Collections.emptyList();    }
}
