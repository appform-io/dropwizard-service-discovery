package io.appform.dropwizard.discovery.bundle.id;

import io.appform.dropwizard.discovery.bundle.id.constraints.IdValidationConstraint;
import io.appform.dropwizard.discovery.bundle.id.formatter.IdFormatter;
import io.appform.dropwizard.discovery.bundle.id.formatter.IdFormatters;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Getter
public class Domain {

    @NonNull
    private final String domain;

    @Builder.Default
    private List<IdValidationConstraint> constraints = new ArrayList<>();

    @Builder.Default
    private IdFormatter idFormatter = IdFormatters.original();

    @Builder.Default
    private final TimeUnit resolution = TimeUnit.MILLISECONDS;

    private final CollisionChecker collisionChecker;

    @Builder
    public Domain(@NonNull String domain,
                  @NonNull List<IdValidationConstraint> constraints,
                  @NonNull IdFormatter idFormatter,
                  @NonNull TimeUnit resolution) {
        this.domain = domain;
        this.constraints = constraints;
        this.idFormatter = idFormatter;
        this.collisionChecker = new CollisionChecker(resolution);
    }

}
