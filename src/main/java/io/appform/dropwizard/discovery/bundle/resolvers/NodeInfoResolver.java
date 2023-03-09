package io.appform.dropwizard.discovery.bundle.resolvers;

import io.appform.dropwizard.discovery.bundle.ServiceDiscoveryBundle;
import io.appform.dropwizard.discovery.bundle.ServiceDiscoveryConfiguration;
import io.appform.ranger.common.server.ShardInfo;

/**
 * NodeInfoResolver.java
 * Interface to help build a node to be saved in the discovery backend while building the serviceProvider.
 * To define your custom nodeData {@link ShardInfo}, please define your own implementation, during the bundle {@link ServiceDiscoveryBundle} init.
 */
public interface NodeInfoResolver {

  ShardInfo node(ServiceDiscoveryConfiguration configuration);

}
