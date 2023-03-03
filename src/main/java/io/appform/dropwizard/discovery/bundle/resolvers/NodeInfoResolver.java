package io.appform.dropwizard.discovery.bundle.resolvers;

import io.appform.dropwizard.discovery.bundle.ServiceDiscoveryConfiguration;
import io.appform.ranger.common.server.ShardInfo;

public interface NodeInfoResolver {

  ShardInfo node(ServiceDiscoveryConfiguration configuration);

}
