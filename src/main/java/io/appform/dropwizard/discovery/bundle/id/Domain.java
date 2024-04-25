package io.appform.dropwizard.discovery.bundle.id;

import io.appform.dropwizard.discovery.bundle.id.constraints.IdValidationConstraint;
import io.appform.dropwizard.discovery.bundle.id.formatter.DefaultIdFormatter;
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
    public static final String DEFAULT_DOMAIN_NAME = "__DEFAULT_DOMAIN__";
    public static final Domain DEFAULT = new Domain(DEFAULT_DOMAIN_NAME,
                                                    List.of(),
                                                    new DefaultIdFormatter(),
                                                    TimeUnit.MILLISECONDS);

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
        this.idFormatter = Objects.requireNonNullElse(idFormatter, IdFormatters.original());
        this.collisionChecker = new CollisionChecker(Objects.requireNonNullElse(resolution, TimeUnit.MILLISECONDS));
    }

}
