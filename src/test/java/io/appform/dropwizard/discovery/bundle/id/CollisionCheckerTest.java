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


import java.util.stream.IntStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test on {@link CollisionChecker}
 */
class CollisionCheckerTest {

    @Test
    void testCheck() {
        CollisionChecker collisionChecker = new CollisionChecker();
        Assertions.assertTrue(collisionChecker.check(100, 1));
        Assertions.assertFalse(collisionChecker.check(100, 1));
        IntStream.range(0, 1000).forEach(i -> {
            Assertions.assertTrue(collisionChecker.check(101, i));
            Assertions.assertFalse(collisionChecker.check(101, i));
        });
    }
}