/*
 * Copyright (c) 2022 Santanu Sinha <santanu.sinha@gmail.com>
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

package io.appform.dropwizard.discovery.bundle.selectors;

import io.appform.ranger.common.server.ShardInfo;
import lombok.Getter;

import java.util.function.Predicate;

/**
 *
 */
public class HierarchicalSelectionPredicate implements Predicate<ShardInfo> {

    @Getter
    private final ShardInfo shardInfo;

    public HierarchicalSelectionPredicate(ShardInfo requiredEnv) {
        this.shardInfo = requiredEnv;
    }

    @Override
    public boolean test(ShardInfo shardInfo) {
        return false;
    }
}
