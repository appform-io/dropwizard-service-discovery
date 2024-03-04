/*
 * Copyright (c) 2016 Santanu Sinha <santanu.sinha@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.appform.dropwizard.discovery.bundle.id;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import dev.failsafe.Failsafe;
import dev.failsafe.FailsafeExecutor;
import dev.failsafe.RetryPolicy;
import io.appform.dropwizard.discovery.bundle.id.constraints.IdValidationConstraint;
import io.appform.dropwizard.discovery.bundle.id.formatter.IdFormatter;
import io.appform.dropwizard.discovery.bundle.id.formatter.IdFormatters;
import io.appform.dropwizard.discovery.bundle.id.request.IdGenerationRequest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Id generation
 */
@SuppressWarnings("unused")
@Slf4j
public class IdGenerator {

    private static final int MINIMUM_ID_LENGTH = 22;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom(Long.toBinaryString(System.currentTimeMillis()).getBytes());
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyMMddHHmmssSSS");

    private static final Map<String, CollisionChecker> DOMAIN_COLLISION_CHECKERS = new HashMap<>();
    private static final CollisionChecker DEFAULT_COLLISION_CHECKER = new CollisionChecker();
    private static final RetryPolicy<GenerationResult> RETRY_POLICY = RetryPolicy.<GenerationResult>builder()
            .withMaxAttempts(readRetryCount())
            .handleIf(throwable -> true)
            .handleResultIf(Objects::isNull)
            .handleResultIf(generationResult -> generationResult.getState() == IdValidationState.INVALID_RETRYABLE)
            .onRetry(event -> {
                val res = event.getLastResult();
                if(null != res && !res.getState().equals(IdValidationState.VALID)) {
                    val id = res.getId();
                    CollisionChecker collisionChecker = res.getDomain() == null
                            ? DEFAULT_COLLISION_CHECKER
                            : DOMAIN_COLLISION_CHECKERS.getOrDefault(res.getDomain(), DEFAULT_COLLISION_CHECKER);
                    collisionChecker.free(id.getGeneratedDate().getTime(), id.getExponent());
                }
            })
            .build();
    private static final FailsafeExecutor<GenerationResult> RETRIER
            = Failsafe.with(Collections.singletonList(RETRY_POLICY));
    private static final Pattern PATTERN = Pattern.compile("(.*)([0-9]{15})([0-9]{4})([0-9]{3})");

    private static final List<IdValidationConstraint> GLOBAL_CONSTRAINTS = new ArrayList<>();
    private static final Map<String, List<IdValidationConstraint>> DOMAIN_SPECIFIC_CONSTRAINTS = new HashMap<>();
    private static int nodeId;

    public static void initialize(int node) {
        nodeId = node;
    }

    public static synchronized void cleanUp() {
        GLOBAL_CONSTRAINTS.clear();
        DOMAIN_SPECIFIC_CONSTRAINTS.clear();
        DOMAIN_COLLISION_CHECKERS.clear();
    }

    public static synchronized void initialize(
            int node, List<IdValidationConstraint> globalConstraints,
            Map<String, List<IdValidationConstraint>> domainSpecificConstraints) {
        initialize(node, globalConstraints, domainSpecificConstraints, Collections.emptyMap());
    }


    public static synchronized void initialize(
            int node, List<IdValidationConstraint> globalConstraints,
            Map<String, List<IdValidationConstraint>> domainSpecificConstraints,
            Map<String, TimeUnit> domainSpecificResolutions) {
        nodeId = node;
        if(null != globalConstraints) {
            IdGenerator.GLOBAL_CONSTRAINTS.addAll(globalConstraints);
        }
        if(null != domainSpecificConstraints) {
            IdGenerator.DOMAIN_SPECIFIC_CONSTRAINTS.putAll(domainSpecificConstraints);
        }
        if (null != domainSpecificResolutions) {
            IdGenerator.DOMAIN_COLLISION_CHECKERS.putAll(domainSpecificResolutions.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> new CollisionChecker(entry.getValue()))));
        }
    }

    public static synchronized void registerGlobalConstraints(IdValidationConstraint... constraints) {
        registerGlobalConstraints(ImmutableList.copyOf(constraints));
    }

    public static synchronized void registerGlobalConstraints(List<IdValidationConstraint> constraints) {
        Preconditions.checkArgument(null != constraints && !constraints.isEmpty());
        GLOBAL_CONSTRAINTS.addAll(constraints);
    }

    public static synchronized void registerDomainSpecificConstraints(
            String domain,
            IdValidationConstraint... validationConstraints) {
        registerDomainSpecificConstraints(domain, ImmutableList.copyOf(validationConstraints));
    }

    public static synchronized void registerDomainSpecificConstraints(
            String domain,
            List<IdValidationConstraint> validationConstraints) {
        Preconditions.checkArgument(null != validationConstraints && !validationConstraints.isEmpty());
        DOMAIN_SPECIFIC_CONSTRAINTS.computeIfAbsent(domain, key -> new ArrayList<>())
                .addAll(validationConstraints);
    }

    public static synchronized void registerDomainSpecificConstraints(
            String domain,
            List<IdValidationConstraint> validationConstraints,
            TimeUnit resolution) {
        Preconditions.checkArgument(null != validationConstraints && !validationConstraints.isEmpty());
        DOMAIN_SPECIFIC_CONSTRAINTS.computeIfAbsent(domain, key -> new ArrayList<>())
                .addAll(validationConstraints);
        DOMAIN_COLLISION_CHECKERS.computeIfAbsent(domain, key -> new CollisionChecker(resolution));
    }


    /**
     * Generate id with given prefix
     *
     * @param prefix String prefix with will be used to blindly merge
     * @return Generated Id
     */
    public static Id generate(String prefix) {
        return generate(prefix, IdFormatters.original(), DEFAULT_COLLISION_CHECKER);
    }

    public static Id generate(final String prefix,
                              final IdFormatter idFormatter) {
        return generate(prefix, idFormatter, DEFAULT_COLLISION_CHECKER);
    }

    private static Id generate(final String prefix,
                               final IdFormatter idFormatter,
                               final CollisionChecker collisionChecker) {
        val idInfo = random(collisionChecker);
        val dateTime = new DateTime(idInfo.time);
        val id = String.format("%s%s", prefix, idFormatter.format(dateTime, nodeId, idInfo.exponent));
        return Id.builder()
                .id(id)
                .exponent(idInfo.exponent)
                .generatedDate(dateTime.toDate())
                .node(nodeId)
                .build();
    }



    /**
     * Generate id that mathces all passed constraints.
     * NOTE: There are performance implications for this.
     * The evaluation of constraints will take it's toll on id generation rates. Tun rests to check speed.
     *
     * @param prefix String prefix
     * @param domain Domain for constraint selection
     * @return Return generated id or empty if it was impossible to satisfy constraints and generate
     */
    public static Optional<Id> generateWithConstraints(String prefix, String domain) {
        return generateWithConstraints(prefix, domain, DOMAIN_SPECIFIC_CONSTRAINTS.getOrDefault(domain, Collections.emptyList()), true);
    }

    /**
     * Generate id that mathces all passed constraints.
     * NOTE: There are performance implications for this.
     * The evaluation of constraints will take it's toll on id generation rates. Tun rests to check speed.
     *
     * @param prefix     String prefix
     * @param domain     Domain for constraint selection
     * @param skipGlobal Skip global constrains and use only passed ones
     * @return Id if it could be generated
     */
    public static Optional<Id> generateWithConstraints(String prefix, String domain, boolean skipGlobal) {
        return generateWithConstraints(prefix, domain, DOMAIN_SPECIFIC_CONSTRAINTS.getOrDefault(domain, Collections.emptyList()), skipGlobal);
    }

    /**
     * Generate id that mathces all passed constraints.
     * NOTE: There are performance implications for this.
     * The evaluation of constraints will take it's toll on id generation rates. Tun rests to check speed.
     *
     * @param prefix        String prefix
     * @param inConstraints Constraints that need to be validated.
     * @return Id if it could be generated
     */
    public static Optional<Id> generateWithConstraints(String prefix, final List<IdValidationConstraint> inConstraints) {
        return generateWithConstraints(prefix, inConstraints, false);
    }

    /**
     * Generate id by parsing given string
     *
     * @param idString String idString
     * @return Id if it could be generated
     */
    public static Optional<Id> parse(final String idString) {
        if (idString == null
                || idString.length() < MINIMUM_ID_LENGTH) {
            return Optional.empty();
        }
        try {
            val matcher = PATTERN.matcher(idString);
            if (matcher.find()) {
                return Optional.of(Id.builder()
                        .id(idString)
                        .node(Integer.parseInt(matcher.group(3)))
                        .exponent(Integer.parseInt(matcher.group(4)))
                        .generatedDate(DATE_TIME_FORMATTER.parseDateTime(matcher.group(2)).toDate())
                        .build());
            }
            return Optional.empty();
        }
        catch (Exception e) {
            log.warn("Could not parse idString {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Generate id that mathces all passed constraints.
     * NOTE: There are performance implications for this.
     * The evaluation of constraints will take it's toll on id generation rates. Tun rests to check speed.
     *
     * @param prefix        String prefix
     * @param inConstraints Constraints that need to be validate.
     * @param skipGlobal    Skip global constrains and use only passed ones
     * @return Id if it could be generated
     */
    public static Optional<Id> generateWithConstraints(String prefix, final List<IdValidationConstraint> inConstraints, boolean skipGlobal) {
        return generate(IdGenerationRequest.builder()
                .prefix(prefix)
                .constraints(inConstraints)
                .skipGlobal(skipGlobal)
                .idFormatter(IdFormatters.original())
                .build());
    }

    /**
     * Generate id that mathces all passed constraints.
     * NOTE: There are performance implications for this.
     * The evaluation of constraints will take it's toll on id generation rates. Tun rests to check speed.
     *
     * @param prefix        String prefix
     * @param inConstraints Constraints that need to be validate.
     * @param skipGlobal    Skip global constrains and use only passed ones
     * @param domain        Domain
     * @return Id if it could be generated
     */
    public static Optional<Id> generateWithConstraints(String prefix, String domain,
                                                       final List<IdValidationConstraint> inConstraints, boolean skipGlobal) {
        return generate(IdGenerationRequest.builder()
                .prefix(prefix)
                .constraints(inConstraints)
                .skipGlobal(skipGlobal)
                .domain(domain)
                .idFormatter(IdFormatters.original())
                .build());
    }

    public static Optional<Id> generate(final IdGenerationRequest request) {
        return Optional.ofNullable(RETRIER.get(
                        () -> {
                            Id id = generate(request.getPrefix(), request.getIdFormatter(), request.getDomain() != null
                                    ? DOMAIN_COLLISION_CHECKERS.getOrDefault(request.getDomain(), DEFAULT_COLLISION_CHECKER)
                                    : DEFAULT_COLLISION_CHECKER);
                            return new GenerationResult(id,
                                    validateId(request.getConstraints(), id, request.isSkipGlobal()), request.getDomain());
                        }))
                .filter(generationResult -> generationResult.getState() == IdValidationState.VALID)
                .map(GenerationResult::getId);
    }

    private static IdInfo random(CollisionChecker collisionChecker) {
        int randomGen;
        long time;
        do {
            time = System.currentTimeMillis();
            randomGen = SECURE_RANDOM.nextInt(Constants.MAX_ID_PER_MS);
        } while (!collisionChecker.check(time, randomGen));
        return new IdInfo(randomGen, time);
    }

    private static IdValidationState validateId(List<IdValidationConstraint> inConstraints, Id id, boolean skipGlobal) {
        //First evaluate global constraints
        val failedGlobalConstraint
                = skipGlobal
                  ? null
                  : GLOBAL_CONSTRAINTS.stream()
                          .filter(constraint -> !constraint.isValid(id))
                          .findFirst()
                          .orElse(null);
        if (null != failedGlobalConstraint) {
            return failedGlobalConstraint.failFast()
                   ? IdValidationState.INVALID_NON_RETRYABLE
                   : IdValidationState.INVALID_RETRYABLE;
        }
        //Evaluate local + domain constraints
        val failedLocalConstraint
                = null == inConstraints
                  ? null
                  : inConstraints.stream()
                          .filter(constraint -> !constraint.isValid(id))
                          .findFirst()
                          .orElse(null);
        if (null != failedLocalConstraint) {
            return failedLocalConstraint.failFast()
                   ? IdValidationState.INVALID_NON_RETRYABLE
                   : IdValidationState.INVALID_RETRYABLE;
        }
        return IdValidationState.VALID;
    }

    private static int readRetryCount() {
        try {
            val count = Integer.parseInt(System.getenv().getOrDefault("NUM_ID_GENERATION_RETRIES", "512"));
            if (count <= 0) {
                throw new IllegalArgumentException(
                        "Negative number of retries does not make sense. Please set a proper value for " +
                                "NUM_ID_GENERATION_RETRIES");
            }
            return count;
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Please provide a valid positive integer for NUM_ID_GENERATION_RETRIES");
        }
    }

    private enum IdValidationState {
        VALID,
        INVALID_RETRYABLE,
        INVALID_NON_RETRYABLE
    }

    @Value
    private static class IdInfo {
        int exponent;
        long time;

        public IdInfo(int exponent, long time) {
            this.exponent = exponent;
            this.time = time;
        }
    }

    @Value
    private static class GenerationResult {
        Id id;
        IdValidationState state;
        String domain;
    }
}
