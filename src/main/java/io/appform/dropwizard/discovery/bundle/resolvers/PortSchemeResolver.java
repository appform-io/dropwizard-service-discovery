package io.appform.dropwizard.discovery.bundle.resolvers;

import io.dropwizard.Configuration;

/**
 * NodeInfoResolver.java
 * Interface to help build a portScheme basis the server {@link Configuration}
 */
@FunctionalInterface
public interface PortSchemeResolver<T extends Configuration> extends CriteriaResolver<String, T> {

}
