/*
 * Copyright (c) 2018 Santanu Sinha <santanu.sinha@gmail.com>
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

package io.appform.dropwizard.discovery.bundle.id.constraints.impl;

import com.google.common.hash.Hashing;
import io.appform.dropwizard.discovery.bundle.id.Id;

import java.nio.charset.StandardCharsets;

/**
 *
 */
public class MurmurBasedKeyPartitioner implements KeyPartitioner {

    private final int maxPartitions;

    public MurmurBasedKeyPartitioner(int maxPartitions) {
        this.maxPartitions = maxPartitions;
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public int partition(Id id) {
        int hashCode = Hashing.murmur3_128().hashString(id.toString(), StandardCharsets.UTF_8).asInt();
        hashCode *= hashCode < 0 ? -1 : 1;
        return hashCode % maxPartitions;
    }
}
