package io.appform.dropwizard.discovery.bundle.resolvers;

import io.appform.ranger.core.model.PortSchemes;
import lombok.val;
import org.eclipse.jetty.server.Server;

public class DefaultPortSchemeResolver implements PortSchemeResolver {

    @Override
    public String resolve(Server server) {
      val connectionFactory = server.getConnectors()[0].getConnectionFactory("ssl");
      return null == connectionFactory ? PortSchemes.HTTP : PortSchemes.HTTPS;
    }
}
