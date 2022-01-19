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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import io.appform.ranger.common.server.ShardInfo;
import io.appform.ranger.core.finder.serviceregistry.MapBasedServiceRegistry;
import io.appform.ranger.core.healthcheck.HealthcheckStatus;
import io.appform.ranger.core.model.Service;
import io.appform.ranger.core.model.ServiceNode;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

/**
 *
 */
public class HierarchicalEnvironmentAwareShardSelectorTest {


    private HierarchicalEnvironmentAwareShardSelector hierarchicalEnvironmentAwareShardSelector;

    @Mock
    private MapBasedServiceRegistry<ShardInfo> serviceRegistry;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.hierarchicalEnvironmentAwareShardSelector = new HierarchicalEnvironmentAwareShardSelector();
    }

    @Test
    public void testNoNodeAvailableForTheEnvironment() {
        val serviceName = UUID.randomUUID().toString();
        val service = Mockito.mock(Service.class);
        doReturn(serviceName).when(service).getServiceName();
        doReturn(service).when(serviceRegistry).getService();

        ListMultimap<ShardInfo, ServiceNode<ShardInfo>> serviceNodes = ArrayListMultimap.create();
        serviceNodes.put(
                ShardInfo.builder().environment("x.y").build(),
                new ServiceNode<>("host1",
                                  8888,
                                  ShardInfo.builder().environment("x.y").build(),
                                  HealthcheckStatus.healthy,
                                  System.currentTimeMillis()));

        serviceNodes.put(
                ShardInfo.builder().environment("x").build(),
                new ServiceNode<>("host2",
                                  8888,
                                  ShardInfo.builder().environment("x.y").build(),
                                  HealthcheckStatus.healthy,
                                  System.currentTimeMillis()));

        doReturn(serviceNodes).when(serviceRegistry).nodes();

        val nodes = hierarchicalEnvironmentAwareShardSelector.nodes(
                new HierarchicalSelectionPredicate(ShardInfo.builder().environment("z").build()),
                serviceRegistry);
        assertEquals(0, nodes.size());
    }

    @Test
    public void testNodeAvailableForChildEnv() {
        val serviceName = UUID.randomUUID().toString();
        val service = Mockito.mock(Service.class);
        doReturn(serviceName).when(service).getServiceName();
        doReturn(service).when(serviceRegistry).getService();

        ListMultimap<ShardInfo, ServiceNode<ShardInfo>> serviceNodes = ArrayListMultimap.create();
        serviceNodes.put(
                ShardInfo.builder().environment("x.y").build(),
                new ServiceNode<>("host1",
                                  8888,
                                  ShardInfo.builder().environment("x.y").build(),
                                  HealthcheckStatus.healthy,
                                  System.currentTimeMillis()));
        serviceNodes.put(
                ShardInfo.builder().environment("x").build(),
                new ServiceNode<>("host2",
                                  8888,
                                  ShardInfo.builder().environment("x").build(),
                                  HealthcheckStatus.healthy,
                                  System.currentTimeMillis()));
        doReturn(serviceNodes).when(serviceRegistry).nodes();

        val nodes = hierarchicalEnvironmentAwareShardSelector.nodes(
                new HierarchicalSelectionPredicate(ShardInfo.builder().environment("x.y").build()),
                serviceRegistry);
        assertEquals(1, nodes.size());
        assertEquals("host1", nodes.get(0).getHost());
        assertEquals(8888, nodes.get(0).getPort());
    }

    @Test
    public void testNoNodeAvailableForChildEnvButAvailableForParentEnv() {
        val serviceName = UUID.randomUUID().toString();
        val service = Mockito.mock(Service.class);
        doReturn(serviceName).when(service).getServiceName();
        doReturn(service).when(serviceRegistry).getService();

        ListMultimap<ShardInfo, ServiceNode<ShardInfo>> serviceNodes = ArrayListMultimap.create();
        serviceNodes.put(
                ShardInfo.builder().environment("x.y.z").build(),
                new ServiceNode<>("host1",
                                  8888,
                                  ShardInfo.builder().environment("x.y").build(),
                                  HealthcheckStatus.healthy,
                                  System.currentTimeMillis()));
        serviceNodes.put(
                ShardInfo.builder().environment("x").build(),
                new ServiceNode<>("host2",
                                  9999,
                                  ShardInfo.builder().environment("x").build(),
                                  HealthcheckStatus.healthy,
                                  System.currentTimeMillis()));
        doReturn(serviceNodes).when(serviceRegistry).nodes();

        val nodes = hierarchicalEnvironmentAwareShardSelector.nodes(
                new HierarchicalSelectionPredicate(ShardInfo.builder().environment("x.y").build()),
                serviceRegistry);
        assertEquals(1, nodes.size());
        assertEquals("host2", nodes.get(0).getHost());
        assertEquals(9999, nodes.get(0).getPort());
    }

    @Test
    public void testAllNodes() {
        val serviceName = UUID.randomUUID().toString();
        val service = Mockito.mock(Service.class);
        doReturn(serviceName).when(service).getServiceName();
        doReturn(service).when(serviceRegistry).getService();

        ListMultimap<ShardInfo, ServiceNode<ShardInfo>> serviceNodes = ArrayListMultimap.create();
        serviceNodes.put(
                ShardInfo.builder().environment("x.y.z").build(),
                new ServiceNode<>("host1",
                                  8888,
                                  ShardInfo.builder().environment("x.y").build(),
                                  HealthcheckStatus.healthy,
                                  System.currentTimeMillis()));
        serviceNodes.put(
                ShardInfo.builder().environment("x").build(),
                new ServiceNode<>("host2",
                                  8888,
                                  ShardInfo.builder().environment("x").build(),
                                  HealthcheckStatus.healthy,
                                  System.currentTimeMillis()));
        doReturn(serviceNodes).when(serviceRegistry).nodes();

        val nodes = hierarchicalEnvironmentAwareShardSelector.nodes(
                new HierarchicalSelectionPredicate(ShardInfo.builder().environment("*").build()),
                serviceRegistry);
        assertEquals(2, nodes.size());
    }

}