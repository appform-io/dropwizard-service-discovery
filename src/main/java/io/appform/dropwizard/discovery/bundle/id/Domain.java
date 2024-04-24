package io.appform.dropwizard.discovery.bundle.id;

import io.appform.dropwizard.discovery.bundle.id.constraints.IdValidationConstraint;
import io.appform.dropwizard.discovery.bundle.id.formatter.IdFormatter;
import io.appform.dropwizard.discovery.bundle.id.formatter.IdFormatters;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Getter
public class Domain {

    private final String domain;
    private final List<IdValidationConstraint> constraints;
    private final IdFormatter idFormatter;
    private final CollisionChecker collisionChecker;


    @Builder
    public Domain(@NonNull String domain,
                  @NonNull List<IdValidationConstraint> constraints,
                  IdFormatter idFormatter,
                  TimeUnit resolution) {
        this.domain = domain;
        this.constraints = constraints;
        this.idFormatter = Objects.isNull(idFormatter)
                ? IdFormatters.original()
                : idFormatter;
        this.collisionChecker = Objects.isNull(resolution)
                ? new CollisionChecker(TimeUnit.MILLISECONDS)
                : new CollisionChecker(resolution);
    }

}
