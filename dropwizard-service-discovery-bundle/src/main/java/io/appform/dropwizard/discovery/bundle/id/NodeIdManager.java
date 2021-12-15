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

import com.google.common.base.Predicate;
import dev.failsafe.Failsafe;
import dev.failsafe.FailsafeExecutor;
import dev.failsafe.RetryPolicy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

import java.security.SecureRandom;
import java.util.Collections;

/**
 * Created by santanu on 2/5/16.
 */
@Slf4j
public class NodeIdManager {

    private static final RetryPolicy<Boolean> RETRY_POLICY = RetryPolicy.<Boolean>builder()
            .withMaxAttempts(-1)
            .handleIf((Predicate<Throwable>) throwable -> true)
            .handleResultIf(result -> !result)
            .build();

    private static final FailsafeExecutor<Boolean> RETRIER = Failsafe.with(
            Collections.singletonList(RETRY_POLICY));

    private final CuratorFramework curatorFramework;
    private final SecureRandom secureRandom;
    private final CuratorPathUtils pathUtils;

    @Getter
    private int node;

    public NodeIdManager(CuratorFramework curatorFramework, String processName) {
        this.curatorFramework = curatorFramework;
        this.secureRandom = new SecureRandom(Long.toBinaryString(System.currentTimeMillis()).getBytes());
        this.pathUtils = new CuratorPathUtils(processName);
    }

    public int fixNodeId() {
        try {
            log.info("Waiting for curator to start");
            curatorFramework.blockUntilConnected();
            log.info("Curator started");
        } catch (InterruptedException e) {
            log.error("Wait for curator start interrupted", e);
        }
        RETRIER.get(() -> {
            node = secureRandom.nextInt(Constants.MAX_NUM_NODES);
            final String path = pathUtils.path(node);
            try {
                curatorFramework.create()
                        .creatingParentContainersIfNeeded()
                        .withMode(CreateMode.EPHEMERAL)
                        .forPath(path);
            } catch (KeeperException.NodeExistsException e) {
                log.warn("Collision on node {}, will retry with new node.", node);
                return false;
            }
            log.info("Node will be set to node id {}", node);
            return true;
        });
        return node;
    }
}
