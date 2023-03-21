package io.appform.dropwizard.discovery.bundle.resolvers;

import io.appform.ranger.core.model.PortSchemes;
import io.dropwizard.jetty.ConnectorFactory;
import io.dropwizard.jetty.HttpsConnectorFactory;
import io.dropwizard.server.DefaultServerFactory;
import io.dropwizard.server.ServerFactory;
import lombok.val;

public class DefaultPortSchemeResolver implements PortSchemeResolver {

  private String getScheme(ConnectorFactory connectorFactory){
    if(null == connectorFactory) return PortSchemes.HTTP;
    return connectorFactory instanceof HttpsConnectorFactory ? PortSchemes.HTTPS : PortSchemes.HTTP;
  }

  @Override
  public String resolve(ServerFactory serverFactory) {
    if(serverFactory instanceof DefaultServerFactory){
      val defaultFactory = (DefaultServerFactory) serverFactory;
      return getScheme(defaultFactory.getApplicationConnectors().stream().findFirst().orElse(null));
    }else{
      return PortSchemes.HTTP;
    }
  }
}
