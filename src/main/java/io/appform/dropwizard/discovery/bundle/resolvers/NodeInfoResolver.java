package io.appform.dropwizard.discovery.bundle.resolvers;

import io.appform.dropwizard.discovery.bundle.ServiceDiscoveryConfiguration;
import io.appform.ranger.common.server.ShardInfo;

public interface NodeInfoResolver extends CriteriaResolver<ShardInfo, ServiceDiscoveryConfiguration> {

}
