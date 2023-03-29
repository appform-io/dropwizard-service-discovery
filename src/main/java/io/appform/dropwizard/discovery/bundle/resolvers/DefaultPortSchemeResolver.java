package io.appform.dropwizard.discovery.bundle.resolvers;

import io.appform.ranger.core.model.PortSchemes;
import io.dropwizard.Configuration;
import io.dropwizard.jetty.ConnectorFactory;
import io.dropwizard.jetty.HttpsConnectorFactory;
import io.dropwizard.server.DefaultServerFactory;
import io.dropwizard.server.ServerFactory;
import io.dropwizard.server.SimpleServerFactory;
import java.util.Optional;
import lombok.val;

/**
 * DefaultPortSchemeResolver.java
 * To derive PortScheme from the ServerFactory from Dropwizard startup config
 */
public class DefaultPortSchemeResolver<T extends Configuration> implements PortSchemeResolver<T> {

  /**
   * Returns a PortScheme basis the configuration. The default in case of a new
   * Connector found (Possibly on version upgrades, if we have forgotten mutate it,
   * is HTTP)
   * @param configuration {@link Configuration} the dropwizard startup config
   * @return {@link String} The relevant portScheme with HTTP as default
   */
    @Override
    public String resolve(T configuration) {
      val connectionFactory = getConnectorFactory(
        configuration.getServerFactory());
      return connectionFactory.filter(HttpsConnectorFactory.class::isInstance)
        .map(factory -> PortSchemes.HTTPS).orElse(PortSchemes.HTTP);
    }

    private Optional<ConnectorFactory> getConnectorFactory(
      ServerFactory serverFactory) {
      if(serverFactory instanceof DefaultServerFactory) {
        val defaultFactory = (DefaultServerFactory) serverFactory;
        return defaultFactory.getApplicationConnectors()
          .stream()
          .findFirst();
      } else if(serverFactory instanceof SimpleServerFactory) {
        val defaultFactory = (SimpleServerFactory) serverFactory;
        return Optional.ofNullable(defaultFactory.getConnector());
      }else{
        return Optional.empty();
      }
    }
}
