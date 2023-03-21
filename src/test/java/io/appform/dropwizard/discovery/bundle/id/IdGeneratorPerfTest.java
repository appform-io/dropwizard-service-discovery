/*
 * Copyright 2022. Santanu Sinha
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package io.appform.dropwizard.discovery.bundle.id;

import io.appform.dropwizard.discovery.bundle.id.formatter.IdFormatters;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;

/**
 * Test performance between different constructs
 */
@Slf4j
public class IdGeneratorPerfTest extends BenchmarkTest {

    @State(Scope.Benchmark)
    public static class BenchmarkState {

        @Setup(Level.Trial)
        public void setUp() throws IOException {
            IdGenerator.initialize(23);
        }
    }

    @SneakyThrows
    @Benchmark
    public void testGenerateBase36(Blackhole blackhole, BenchmarkState state) {
        IdGenerator.generate("X", IdFormatters.base36());
    }

    @SneakyThrows
    @Benchmark
    public void testGenerate(Blackhole blackhole, BenchmarkState state) {
        IdGenerator.generate("X", IdFormatters.original());
    }
}