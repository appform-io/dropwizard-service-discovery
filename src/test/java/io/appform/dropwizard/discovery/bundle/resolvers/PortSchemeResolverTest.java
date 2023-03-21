package io.appform.dropwizard.discovery.bundle.resolvers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import lombok.val;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.LocalConnector;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PortSchemeResolverTest {

  @Test
  void testTransportTypeResolver(){
    val server = mock(Server.class);
    val connector = mock(LocalConnector.class);
    when(connector.getConnectionFactory(Mockito.anyString())).thenReturn(null);
    val connectors = new Connector[1];
    connectors[0] = connector;
    when(server.getConnectors()).thenReturn(connectors);
    val resolver = new PortSchemeResolver();
    Assertions.assertEquals("http", resolver.resolve(server));
  }
}
