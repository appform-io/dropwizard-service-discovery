package io.appform.dropwizard.discovery.bundle.resolvers;

import org.eclipse.jetty.server.Server;

/**
 * NodeInfoResolver.java
 * Interface to help build a portScheme basis the server {@link Server}
 */
@FunctionalInterface
public interface PortSchemeResolver extends CriteriaResolver<String, Server> {

}
