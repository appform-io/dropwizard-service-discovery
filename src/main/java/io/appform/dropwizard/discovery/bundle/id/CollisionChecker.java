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

import java.util.BitSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.extern.slf4j.Slf4j;

/**
 * Checks collisions between ids in given period
 */
@Slf4j
public class CollisionChecker {
    private final BitSet bitSet = new BitSet(1000);
    private long currentInstant = 0;

    private final Lock dataLock = new ReentrantLock();

    private final TimeUnit resolution;

    public CollisionChecker() {
        this.resolution = TimeUnit.MILLISECONDS;
    }

    public CollisionChecker(TimeUnit resolution) {
        this.resolution = resolution;
    }

    public boolean check(long timeInMillis, int location) {
        dataLock.lock();
        try {
            long resolvedTime = resolution.convert(timeInMillis, TimeUnit.MILLISECONDS);;
            if (currentInstant != resolvedTime) {
                currentInstant = resolvedTime;
                bitSet.clear();
            }

            if (bitSet.get(location)) {
                return false;
            }
            bitSet.set(location);
            return true;
        }
        finally {
            dataLock.unlock();
        }
    }

    public void free(long time, int location) {
        dataLock.lock();
        try {
            if (currentInstant != time) {
                return;
            }
            bitSet.clear(location);
        }
        finally {
            dataLock.unlock();
        }
    }
}
