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
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import dev.failsafe.Failsafe;
import dev.failsafe.FailsafeExecutor;
import dev.failsafe.RetryPolicy;
import io.appform.dropwizard.discovery.bundle.id.constraints.IdValidationConstraint;
import lombok.Data;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.zookeeper.Op;
import org.checkerframework.checker.nullness.Opt;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.swing.text.html.Option;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Id generation
 */
@Slf4j
public class IdGenerator {

    private static final int MINIMUM_ID_LENGTH = 22;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom(Long.toBinaryString(System.currentTimeMillis()).getBytes());
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyMMddHHmmssSSS");
    private static final CollisionChecker COLLISION_CHECKER = new CollisionChecker();
    private static final Map<String, List<IdValidationConstraint>> DOMAIN_SPECIFIC_CONSTRAINTS = new HashMap<>();
    private static final RetryPolicy<GenerationResult> RETRY_POLICY = RetryPolicy.<GenerationResult>builder()
            .withMaxAttempts(512)
            .handleIf((Predicate<Throwable>) throwable -> true)
            .handleResultIf(Objects::isNull)
            .handleResultIf(generationResult -> generationResult.getState() == IdValidationState.INVALID_RETRYABLE)
            .build();
    private static final FailsafeExecutor<GenerationResult> RETRIER = Failsafe.with(
            Collections.singletonList(RETRY_POLICY));
    private static final String PATTERN_STRING = "(.*)([0-9]{15})([0-9]{4})([0-9]{3})";
    private static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);
    private static int nodeId;
    private static List<IdValidationConstraint> globalConstraints = Collections.emptyList();

    public static void initialize(int node) {
        nodeId = node;
    }

    public static synchronized void cleanUp() {
        globalConstraints.clear();
        DOMAIN_SPECIFIC_CONSTRAINTS.clear();
    }

    public synchronized static void initialize(
            int node, List<IdValidationConstraint> globalConstraints, Map<String, List<IdValidationConstraint>> domainSpecificConstraints) {
        nodeId = node;
        IdGenerator.globalConstraints = globalConstraints != null
                ? globalConstraints
                : Collections.emptyList();
        IdGenerator.DOMAIN_SPECIFIC_CONSTRAINTS.putAll(domainSpecificConstraints);
    }

    public static synchronized void registerGlobalConstraints(IdValidationConstraint... constraints) {
        registerGlobalConstraints(ImmutableList.copyOf(constraints));
    }

    public static synchronized void registerGlobalConstraints(List<IdValidationConstraint> constraints) {
        Preconditions.checkArgument(null != constraints && !constraints.isEmpty());
        if (null == globalConstraints) {
            globalConstraints = new ArrayList<>();
        }
        globalConstraints.addAll(constraints);
    }

    public static synchronized void registerDomainSpecificConstraints(String domain, IdValidationConstraint... validationConstraints) {
        registerDomainSpecificConstraints(domain, ImmutableList.copyOf(validationConstraints));
    }

    public static synchronized void registerDomainSpecificConstraints(String domain, List<IdValidationConstraint> validationConstraints) {
        Preconditions.checkArgument(null != validationConstraints && !validationConstraints.isEmpty());
        if (!DOMAIN_SPECIFIC_CONSTRAINTS.containsKey(domain)) {
            DOMAIN_SPECIFIC_CONSTRAINTS.put(domain, new ArrayList<>());
        }
        DOMAIN_SPECIFIC_CONSTRAINTS.get(domain).addAll(validationConstraints);
    }

    /**
     * Generate id with given prefix
     *
     * @param prefix String prefix with will be used to blindly merge
     * @return Generated Id
     */
    public static Id generate(String prefix) {
        final IdInfo idInfo = random();
        DateTime dateTime = new DateTime(idInfo.time);
        final String id = String.format("%s%s%04d%03d", prefix, DATE_TIME_FORMATTER.print(dateTime), nodeId, idInfo.exponent);
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
     * @return
     */
    public static Optional<Id> generateWithConstraints(String prefix, String domain) {
        return generateWithConstraints(prefix, DOMAIN_SPECIFIC_CONSTRAINTS.getOrDefault(domain, Collections.emptyList()), true);
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
        return generateWithConstraints(prefix, DOMAIN_SPECIFIC_CONSTRAINTS.getOrDefault(domain, Collections.emptyList()), skipGlobal);
    }

    /**
     * Generate id that mathces all passed constraints.
     * NOTE: There are performance implications for this.
     * The evaluation of constraints will take it's toll on id generation rates. Tun rests to check speed.
     *
     * @param prefix        String prefix
     * @param inConstraints Constraints that need to be validate.
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
            Matcher matcher = PATTERN.matcher(idString);
            if (matcher.find()) {
                return Optional.of(Id.builder()
                        .id(idString)
                        .node(Integer.parseInt(matcher.group(3)))
                        .exponent(Integer.parseInt(matcher.group(4)))
                        .generatedDate(DATE_TIME_FORMATTER.parseDateTime(matcher.group(2)).toDate())
                        .build());
            }
            return Optional.empty();
        } catch (Exception e) {
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
        return Optional.ofNullable(RETRIER.get(
                        () -> {
                            Id id = generate(prefix);
                            return new GenerationResult(id, validateId(inConstraints, id, skipGlobal));
                        }))
                .filter(generationResult -> generationResult.getState() == IdValidationState.VALID)
                .map(GenerationResult::getId);
    }

    private static synchronized IdInfo random() {
        int randomGen;
        long time;
        do {
            time = System.currentTimeMillis();
            randomGen = SECURE_RANDOM.nextInt(Constants.MAX_ID_PER_MS);
        } while (!COLLISION_CHECKER.check(time, randomGen));
        return new IdInfo(randomGen, time);
    }

    private static IdValidationState validateId(List<IdValidationConstraint> inConstraints, Id id, boolean skipGlobal) {
        //First evaluate global constraints
        final IdValidationConstraint failedGlobalConstraint
                = skipGlobal || null == globalConstraints
                ? null
                : globalConstraints.stream()
                .filter(constraint -> !constraint.isValid(id))
                .findFirst()
                .orElse(null);
        if (null != failedGlobalConstraint) {
            return failedGlobalConstraint.failFast()
                    ? IdValidationState.INVALID_NON_RETRYABLE
                    : IdValidationState.INVALID_RETRYABLE;
        }
        //Evaluate local + domain constraints
        final IdValidationConstraint failedLocalConstraint
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
    }
}
