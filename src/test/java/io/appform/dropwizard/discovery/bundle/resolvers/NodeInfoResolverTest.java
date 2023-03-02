package io.appform.dropwizard.discovery.bundle.resolvers;

import io.appform.dropwizard.discovery.bundle.ServiceDiscoveryConfiguration;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NodeInfoResolverTest {

  @Test
  void testNodeInfoResolver(){
    val configuration = ServiceDiscoveryConfiguration.builder()
      .zookeeper("connectionString")
      .namespace("test")
      .environment("testing")
      .connectionRetryIntervalMillis(5000)
      .publishedHost("TestHost")
      .publishedPort(8021)
      .initialRotationStatus(true)
      .build();
    val resolver = new NodeInfoResolver();
    val nodeInfo = resolver.getValue(configuration);
    Assertions.assertNotNull(nodeInfo);
    Assertions.assertEquals("testing", configuration.getEnvironment());
    Assertions.assertNull(nodeInfo.getRegion());
    Assertions.assertNull(nodeInfo.getTags());
  }
}
