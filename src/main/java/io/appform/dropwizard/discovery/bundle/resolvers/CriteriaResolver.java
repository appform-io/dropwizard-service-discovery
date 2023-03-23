package io.appform.dropwizard.discovery.bundle.resolvers;

/**
 * CriteriaResolver.java
 * Interface to help resolve from an argument A to the typed object T.
 * Keeping this as the qualified class instead of using Function so that in the future if all criteria resolvers were to be fetched to register using reflections et. al, there is a qualified naming.
 */
@FunctionalInterface
public interface CriteriaResolver<T, A> {

    T resolve(A args);

}
