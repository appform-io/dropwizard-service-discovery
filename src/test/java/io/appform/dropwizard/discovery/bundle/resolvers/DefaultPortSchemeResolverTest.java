package io.appform.dropwizard.discovery.bundle.resolvers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import lombok.val;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DefaultPortSchemeResolverTest {

    @Test
    void testTransportTypeResolver(){
      val server = mock(Server.class);
      val connector = mock(ServerConnector.class);
      when(connector.getConnectionFactory(Mockito.anyString())).thenReturn(null);
      when(server.getConnectors()).thenReturn(new ServerConnector[] { connector });
      val resolver = new DefaultPortSchemeResolver();
      Assertions.assertEquals("http", resolver.resolve(server));
    }
}
