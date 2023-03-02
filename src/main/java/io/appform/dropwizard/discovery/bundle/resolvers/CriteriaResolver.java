package io.appform.dropwizard.discovery.bundle.resolvers;

public interface CriteriaResolver<T, A> {

  T getValue(A arg);

}
