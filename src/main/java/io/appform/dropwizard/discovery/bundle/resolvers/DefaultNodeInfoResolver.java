package io.appform.dropwizard.discovery.bundle.resolvers;

import io.appform.dropwizard.discovery.bundle.ServiceDiscoveryConfiguration;
import io.appform.ranger.common.server.ShardInfo;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@NoArgsConstructor
@Slf4j
public class DefaultNodeInfoResolver implements
  NodeInfoResolver {

  private static final String FARM_ID = "FARM_ID";

  @Override
  public ShardInfo node(ServiceDiscoveryConfiguration configuration) {
    val region = System.getenv(FARM_ID);
    log.debug("The farm id received from the env variable FARM_ID is {}. Setting the same in nodeInfo", region);
    return ShardInfo.builder()
      .environment(configuration.getEnvironment())
      .region(region)
      .tags(configuration.getTags())
      .build();
  }
}
