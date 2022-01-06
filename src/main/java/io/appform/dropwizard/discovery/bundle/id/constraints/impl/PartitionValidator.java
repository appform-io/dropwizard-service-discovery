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

import com.google.common.base.Preconditions;
import io.appform.dropwizard.discovery.bundle.id.Id;
import io.appform.dropwizard.discovery.bundle.id.constraints.IdValidationConstraint;
import lombok.extern.slf4j.Slf4j;

/**
 * Checks if key is same partition as provided.
 */
@Slf4j
public class PartitionValidator implements IdValidationConstraint {

    private final int partition;
    private final KeyPartitioner partitioner;

    public PartitionValidator(int partition, KeyPartitioner partitioner) {
        Preconditions.checkArgument(partition > 0,
                                    "Provide a non-negative and non-zero partition count");
        Preconditions.checkArgument(partitioner != null,
                                    "Provide a non null key partitioner");
        this.partition = partition;
        this.partitioner = partitioner;
    }

    @Override
    public boolean isValid(Id id) {
        return partition == partitioner.partition(id);
    }
}
