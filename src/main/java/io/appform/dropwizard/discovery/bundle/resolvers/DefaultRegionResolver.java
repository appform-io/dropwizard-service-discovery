package io.appform.dropwizard.discovery.bundle.resolvers;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DefaultRegionResolver implements CriteriaResolver<String>{

  private static final String FARM_ID = "FARM_ID";

  @Override
  public String getValue() {
    return System.getenv(FARM_ID);
  }
}
