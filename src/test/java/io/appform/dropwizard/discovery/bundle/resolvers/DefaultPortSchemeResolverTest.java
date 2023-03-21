package io.appform.dropwizard.discovery.bundle.resolvers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.server.DefaultServerFactory;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DefaultPortSchemeResolverTest {

  @Test
  void testTransportTypeResolver(){
    val server = mock(DefaultServerFactory.class);
    val connectorFactory = mock(HttpConnectorFactory.class);
    when(server.getApplicationConnectors()).thenReturn(Lists.newArrayList(connectorFactory));
    val resolver = new DefaultPortSchemeResolver();
    Assertions.assertEquals("http", resolver.resolve(server));
  }
}
