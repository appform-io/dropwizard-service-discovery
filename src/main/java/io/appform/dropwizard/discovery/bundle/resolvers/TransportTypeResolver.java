package io.appform.dropwizard.discovery.bundle.resolvers;

import io.appform.ranger.core.model.TransportType;
import lombok.val;
import org.eclipse.jetty.server.Server;

public class TransportTypeResolver implements CriteriaResolver<TransportType, Server> {

  @Override
  public TransportType resolve(Server server) {
    val connectionFactory = server.getConnectors()[0].getConnectionFactory("ssl");
    return null == connectionFactory ? TransportType.HTTP
      : TransportType.HTTPS;
  }
}
