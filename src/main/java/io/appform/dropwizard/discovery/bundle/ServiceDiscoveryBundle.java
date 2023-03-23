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

package io.appform.dropwizard.discovery.bundle;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.appform.dropwizard.discovery.bundle.healthchecks.InitialDelayChecker;
import io.appform.dropwizard.discovery.bundle.healthchecks.InternalHealthChecker;
import io.appform.dropwizard.discovery.bundle.healthchecks.RotationCheck;
import io.appform.dropwizard.discovery.bundle.id.IdGenerator;
import io.appform.dropwizard.discovery.bundle.id.NodeIdManager;
import io.appform.dropwizard.discovery.bundle.id.constraints.IdValidationConstraint;
import io.appform.dropwizard.discovery.bundle.monitors.DropwizardHealthMonitor;
import io.appform.dropwizard.discovery.bundle.monitors.DropwizardServerStartupCheck;
import io.appform.dropwizard.discovery.bundle.resolvers.DefaultNodeInfoResolver;
import io.appform.dropwizard.discovery.bundle.resolvers.DefaultPortSchemeResolver;
import io.appform.dropwizard.discovery.bundle.resolvers.NodeInfoResolver;
import io.appform.dropwizard.discovery.bundle.resolvers.PortSchemeResolver;
import io.appform.dropwizard.discovery.bundle.rotationstatus.BIRTask;
import io.appform.dropwizard.discovery.bundle.rotationstatus.DropwizardServerStatus;
import io.appform.dropwizard.discovery.bundle.rotationstatus.OORTask;
import io.appform.dropwizard.discovery.bundle.rotationstatus.RotationStatus;
import io.appform.dropwizard.discovery.bundle.selectors.HierarchicalEnvironmentAwareShardSelector;
import io.appform.ranger.client.RangerClient;
import io.appform.ranger.client.zk.SimpleRangerZKClient;
import io.appform.ranger.common.server.ShardInfo;
import io.appform.ranger.core.finder.serviceregistry.MapBasedServiceRegistry;
import io.appform.ranger.core.healthcheck.Healthcheck;
import io.appform.ranger.core.healthcheck.HealthcheckStatus;
import io.appform.ranger.core.healthservice.TimeEntity;
import io.appform.ranger.core.healthservice.monitor.IsolatedHealthMonitor;
import io.appform.ranger.core.model.ServiceNode;
import io.appform.ranger.core.model.ShardSelector;
import io.appform.ranger.core.serviceprovider.ServiceProvider;
import io.appform.ranger.zookeeper.ServiceProviderBuilders;
import io.appform.ranger.zookeeper.serde.ZkNodeDataSerializer;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.lifecycle.ServerLifecycleListener;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;

/**
 * A dropwizard bundle for service discovery.
 */
@SuppressWarnings("unused")
@Slf4j
public abstract class ServiceDiscoveryBundle<T extends Configuration> implements ConfiguredBundle<T> {

    private final List<Healthcheck> healthchecks = Lists.newArrayList();
    private final List<IdValidationConstraint> globalIdConstraints;
    private ServiceDiscoveryConfiguration serviceDiscoveryConfiguration;
    private ServiceProvider<ShardInfo, ZkNodeDataSerializer<ShardInfo>> serviceProvider;

    @Getter
    private CuratorFramework curator;
    @Getter
    private RangerClient<ShardInfo, MapBasedServiceRegistry<ShardInfo>> serviceDiscoveryClient;
    @Getter
    @VisibleForTesting
    private RotationStatus rotationStatus;
    @Getter
    @VisibleForTesting
    private DropwizardServerStatus serverStatus;
    @Getter
    @VisibleForTesting
    private ServerLifecycleListener serverLifecycleListener;

    protected ServiceDiscoveryBundle() {
        globalIdConstraints = Collections.emptyList();
    }

    protected ServiceDiscoveryBundle(List<IdValidationConstraint> globalIdConstraints) {
        this.globalIdConstraints = globalIdConstraints != null
                                   ? globalIdConstraints
                                   : Collections.emptyList();
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {

    }

    @Override
    public void run(T configuration, Environment environment) throws Exception {
        val portSchemeResolver = createPortSchemeResolver();
        Preconditions.checkNotNull(portSchemeResolver, "Port scheme resolver can't be null");

        serviceDiscoveryConfiguration = getRangerConfiguration(configuration);
        val objectMapper = environment.getObjectMapper();
        val namespace = serviceDiscoveryConfiguration.getNamespace();
        val serviceName = getServiceName(configuration);
        val hostname = getHost();
        val port = getPort(configuration);
        val initialCriteria = getInitialCriteria(configuration);
        val useInitialCriteria = alwaysMergeWithInitialCriteria(configuration);
        val shardSelector = getShardSelector(configuration);
        rotationStatus = new RotationStatus(serviceDiscoveryConfiguration.isInitialRotationStatus());
        serverStatus = new DropwizardServerStatus(false);
        curator = CuratorFrameworkFactory.builder()
                .connectString(serviceDiscoveryConfiguration.getZookeeper())
                .namespace(namespace)
                .retryPolicy(new RetryForever(serviceDiscoveryConfiguration.getConnectionRetryIntervalMillis()))
                .build();
        serviceDiscoveryClient = buildDiscoveryClient(
                environment,
                namespace,
                serviceName,
                initialCriteria,
                useInitialCriteria,
                shardSelector
        );
        /*
            Writing this inside the lambda scope, because creating another class will mean having to pass, environment, objectMapper and rest of the
            args to it, including curator and serviceDiscoveryClient, just un-necessary bloat up!
            Extracting into a different class, would mean we'll have to end up passing pretty much every arg in the bundle. That's too verbose.
            For testing : bundle.run doesn't necessarily invoke this, this is a callback on server start. Creating a variable for easy testing.
            As good as moving this to a new class, without all the hassle of moving the entire args from the bundle there!

            Also moving the server start check here, to avoid redundant lifecycleListeners.
         */
        serverLifecycleListener = server -> {
            log.info("Starting the service provider, since the server has started here");
            serviceProvider = buildServiceProvider(
              environment,
              objectMapper,
              namespace,
              serviceName,
              hostname,
              port,
              portSchemeResolver.resolve(server)
            );
            curator.start();
            serviceProvider.start();
            serviceDiscoveryClient.start();
            val nodeIdManager = new NodeIdManager(curator, serviceName);
            IdGenerator.initialize(nodeIdManager.fixNodeId(), globalIdConstraints, Collections.emptyMap());
            serverStatus.markStarted();
            log.info("Service Provider has been successfully started. Server status marked as healthy");
        };
        environment.lifecycle().addServerLifecycleListener(serverLifecycleListener);
        environment.lifecycle().manage(new ServiceDiscoveryManager(serviceName));
        environment.jersey()
                .register(new InfoResource(serviceDiscoveryClient));
        environment.admin()
                .addTask(new OORTask(rotationStatus));
        environment.admin()
                .addTask(new BIRTask(rotationStatus));

    }

    protected ShardSelector<ShardInfo, MapBasedServiceRegistry<ShardInfo>> getShardSelector(T configuration) {
        return new HierarchicalEnvironmentAwareShardSelector(getRangerConfiguration(configuration).getEnvironment());
    }

    protected abstract ServiceDiscoveryConfiguration getRangerConfiguration(T configuration);

    protected abstract String getServiceName(T configuration);

    protected NodeInfoResolver createNodeInfoResolver(){
        return new DefaultNodeInfoResolver();
    }

    protected PortSchemeResolver createPortSchemeResolver(){
        return new DefaultPortSchemeResolver();
    }

    /**
        Override the following if you require.
     **/
    protected Predicate<ShardInfo> getInitialCriteria(T configuration){
        return shardInfo -> true;
    }

    protected boolean alwaysMergeWithInitialCriteria(T configuration){
        return false;
    }

    protected List<IsolatedHealthMonitor<HealthcheckStatus>> getHealthMonitors() {
        return Collections.emptyList();
    }

    @SuppressWarnings("unused")
    protected int getPort(T configuration) {
        Preconditions.checkArgument(
                Constants.DEFAULT_PORT != serviceDiscoveryConfiguration.getPublishedPort()
                        && 0 != serviceDiscoveryConfiguration.getPublishedPort(),
                "Looks like publishedPost has not been set and getPort() has not been overridden. This is wrong. \n" +
                        "Either set publishedPort in config or override getPort() to return the port on which the service is running");
        return serviceDiscoveryConfiguration.getPublishedPort();
    }

    protected String getHost() throws UnknownHostException {
        val host = serviceDiscoveryConfiguration.getPublishedHost();
        if (Strings.isNullOrEmpty(host) || host.equals(Constants.DEFAULT_HOST)) {
            return InetAddress.getLocalHost()
                    .getCanonicalHostName();
        }
        return host;
    }

    public void registerHealthcheck(Healthcheck healthcheck) {
        this.healthchecks.add(healthcheck);
    }

    public void registerHealthchecks(List<Healthcheck> healthchecks) {
        this.healthchecks.addAll(healthchecks);
    }


    private RangerClient<ShardInfo, MapBasedServiceRegistry<ShardInfo>> buildDiscoveryClient(
            Environment environment,
            String namespace,
            String serviceName,
            Predicate<ShardInfo> initialCriteria,
            boolean mergeWithInitialCriteria,
            ShardSelector<ShardInfo, MapBasedServiceRegistry<ShardInfo>> shardSelector) {
        return SimpleRangerZKClient.<ShardInfo>builder()
                .curatorFramework(curator)
                .namespace(namespace)
                .serviceName(serviceName)
                .mapper(environment.getObjectMapper())
                .nodeRefreshIntervalMs(serviceDiscoveryConfiguration.getRefreshTimeMs())
                .disableWatchers(serviceDiscoveryConfiguration.isDisableWatchers())
                .deserializer(
                        data -> {
                            try {
                                return environment.getObjectMapper().readValue(data, new TypeReference<ServiceNode<ShardInfo>>() {
                                });
                            } catch (IOException e) {
                                log.warn("Error parsing node data with value {}", new String(data));
                            }
                            return null;
                        }
                )
                .initialCriteria(initialCriteria)
                .alwaysUseInitialCriteria(mergeWithInitialCriteria)
                .shardSelector(shardSelector)
                .build();
    }

    private ServiceProvider<ShardInfo, ZkNodeDataSerializer<ShardInfo>> buildServiceProvider(
            Environment environment,
            ObjectMapper objectMapper,
            String namespace,
            String serviceName,
            String hostname,
            int port,
            String portScheme) {
        val nodeInfoResolver = createNodeInfoResolver();
        val nodeInfo = nodeInfoResolver.resolve(serviceDiscoveryConfiguration);
        val initialDelayForMonitor = serviceDiscoveryConfiguration.getInitialDelaySeconds() > 1
                                     ? serviceDiscoveryConfiguration.getInitialDelaySeconds() - 1
                                     : 0;
        val dwMonitoringInterval = serviceDiscoveryConfiguration.getDropwizardCheckInterval() == 0
                                   ? Constants.DEFAULT_DW_CHECK_INTERVAL
                                   : serviceDiscoveryConfiguration.getDropwizardCheckInterval();
        val dwMonitoringStaleness
                = Math.max(serviceDiscoveryConfiguration.getDropwizardCheckStaleness(), dwMonitoringInterval + 1);
        val serviceProviderBuilder = ServiceProviderBuilders.<ShardInfo>shardedServiceProviderBuilder()
                .withCuratorFramework(curator)
                .withNamespace(namespace)
                .withServiceName(serviceName)
                .withSerializer(data -> {
                    try {
                        return objectMapper.writeValueAsBytes(data);
                    }
                    catch (Exception e) {
                        log.warn("Could not parse node data", e);
                    }
                    return null;
                })
                .withPortScheme(portScheme)
                .withNodeData(nodeInfo)
                .withHostname(hostname)
                .withPort(port)
                .withHealthcheck(new InternalHealthChecker(healthchecks))
                .withHealthcheck(new RotationCheck(rotationStatus))
                .withHealthcheck(new InitialDelayChecker(serviceDiscoveryConfiguration.getInitialDelaySeconds()))
                .withHealthcheck(new DropwizardServerStartupCheck(serverStatus))
                .withIsolatedHealthMonitor(
                        new DropwizardHealthMonitor(
                                new TimeEntity(initialDelayForMonitor, dwMonitoringInterval, TimeUnit.SECONDS),
                                dwMonitoringStaleness * 1_000L, environment))
                .withHealthUpdateIntervalMs(serviceDiscoveryConfiguration.getRefreshTimeMs())
                .withStaleUpdateThresholdMs(10000);

        val healthMonitors = getHealthMonitors();
        if (healthMonitors != null && !healthMonitors.isEmpty()) {
            healthMonitors.forEach(serviceProviderBuilder::withIsolatedHealthMonitor);
        }
        return serviceProviderBuilder.build();
    }

    @AllArgsConstructor
    private class ServiceDiscoveryManager implements Managed{

        private final String serviceName;

        @Override
        public void start() {
            //Nothing to do here. Everything is done in the lifecycle manager above!
        }

        @Override
        public void stop() {
            serviceDiscoveryClient.stop();
            serviceProvider.stop();
            curator.close();
            IdGenerator.cleanUp();
        }
    }

}
