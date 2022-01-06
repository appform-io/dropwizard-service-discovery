/*
 * Copyright (c) 2019 Santanu Sinha <santanu.sinha@gmail.com>
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
package io.appform.dropwizard.discovery.bundle.criteria;

import io.appform.ranger.common.server.ShardInfo;
import lombok.var;
import org.junit.Assert;
import org.junit.Test;

public class HierarchicalEnvAwareCriteriaTest {

    @Test
    public void testHierarchicalEnvAwareCriteria(){
        var hCriteria = HierarchicalEnvAwareCriteria.builder()
                .environment("*")
                .build();
        Assert.assertTrue(hCriteria.test(ShardInfo.builder().environment("env").build()));
        hCriteria = HierarchicalEnvAwareCriteria.builder()
                .environment("la")
                .build();
        Assert.assertTrue(hCriteria.test(ShardInfo.builder().environment("la").build()));
        hCriteria = HierarchicalEnvAwareCriteria.builder()
                .environment("la.cf")
                .build();
        Assert.assertTrue(hCriteria.test(ShardInfo.builder().environment("cf").build()));
        Assert.assertTrue(hCriteria.test(ShardInfo.builder().environment("cf").region("r1").build()));
        hCriteria = HierarchicalEnvAwareCriteria.builder()
                .environment("la.cf")
                .region("r1")
                .build();
        Assert.assertTrue(hCriteria.test(ShardInfo.builder().environment("cf").region("r1").build()));
        Assert.assertFalse(hCriteria.test(ShardInfo.builder().environment("cf").region("r2").build()));
    }
}
