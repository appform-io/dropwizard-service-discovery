package io.appform.dropwizard.discovery.bundle.resolvers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import io.dropwizard.core.Configuration;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.jetty.HttpsConnectorFactory;
import io.dropwizard.core.server.DefaultServerFactory;
import io.dropwizard.core.server.SimpleServerFactory;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DefaultPortSchemeResolverTest {

    @Test
    void testPortSchemeDefaultServerFactory() {
        val server = mock(DefaultServerFactory.class);
        val connectorFactory = mock(HttpConnectorFactory.class);
        when(server.getApplicationConnectors()).thenReturn(Lists.newArrayList(connectorFactory));
        val resolver = new DefaultPortSchemeResolver<>();
        val configuration = mock(Configuration.class);
        when(configuration.getServerFactory()).thenReturn(server);
        Assertions.assertEquals("http", resolver.resolve(configuration));
    }

    @Test
    void testPortSchemeSimpleServerFactory() {
        val server = mock(SimpleServerFactory.class);
        val connectorFactory = mock(HttpsConnectorFactory.class);
        when(server.getConnector()).thenReturn(connectorFactory);
        val resolver = new DefaultPortSchemeResolver<>();
        val configuration = mock(Configuration.class);
        when(configuration.getServerFactory()).thenReturn(server);
        Assertions.assertEquals("https", resolver.resolve(configuration));
    }

    @Test
    void testPortSchemeDefault() {
        val server = mock(SimpleServerFactory.class);
        when(server.getConnector()).thenReturn(null);
        val resolver = new DefaultPortSchemeResolver<>();
        val configuration = mock(Configuration.class);
        when(configuration.getServerFactory()).thenReturn(server);
        Assertions.assertEquals("http", resolver.resolve(configuration));
    }
}
