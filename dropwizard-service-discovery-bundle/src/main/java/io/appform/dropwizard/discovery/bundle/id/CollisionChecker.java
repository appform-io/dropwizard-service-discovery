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

import lombok.extern.slf4j.Slf4j;

import java.util.BitSet;

/**
 * Checks collisions between ids in given period
 */
@Slf4j
public class CollisionChecker {
    private final BitSet bitSet = new BitSet(1000);
    private long currentInstant = 0;

    public CollisionChecker() {
        //Nothing to do here
    }

    public boolean check(long time, int location) {
        if(currentInstant != time) {
            currentInstant = time;
            bitSet.clear();
        }

        if(bitSet.get(location)) {
            return false;
        }
        bitSet.set(location);
        return true;
    }

    public void free(long time, int location) {
        if(currentInstant != time) {
            return;
        }
        bitSet.clear(location);
    }
}
