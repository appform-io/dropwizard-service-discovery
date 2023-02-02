package io.appform.dropwizard.discovery.bundle.id.request;

import io.appform.dropwizard.discovery.bundle.id.constraints.IdValidationConstraint;
import io.appform.dropwizard.discovery.bundle.id.formatter.IdFormatter;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class IdGenerationRequest {

    String prefix;
    String domain;
    boolean skipGlobal;
    List<IdValidationConstraint> constraints;
    IdFormatter idFormatter;

}
