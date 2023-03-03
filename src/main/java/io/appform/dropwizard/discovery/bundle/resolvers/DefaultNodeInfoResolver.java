package io.appform.dropwizard.discovery.bundle.resolvers;

import io.appform.dropwizard.discovery.bundle.ServiceDiscoveryConfiguration;
import io.appform.ranger.common.server.ShardInfo;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DefaultNodeInfoResolver implements
  NodeInfoResolver {

  private static final String FARM_ID = "FARM_ID";

  @Override
  public ShardInfo node(ServiceDiscoveryConfiguration configuration) {
    return ShardInfo.builder()
      .environment(configuration.getEnvironment())
      .region(System.getenv(FARM_ID))
      .tags(configuration.getTags())
      .build();
  }
}
