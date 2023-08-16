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

package io.appform.dropwizard.discovery.bundle;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import lombok.experimental.UtilityClass;

/**
 * Constants
 */
@UtilityClass
public class Constants {
    public static final String DEFAULT_NAMESPACE = "default";
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = -1;
    public static final int DEFAULT_DW_CHECK_INTERVAL = 15;
    public static final int DEFAULT_RETRY_CONN_INTERVAL = 5000;

    public static final String ZOOKEEPER_HOST_DELIMITER = ",";
    public static final String HOST_PORT_DELIMITER = ":";
    public static final String PATH_DELIMITER = "/";

    public static final Set<String> LOCAL_ADDRESSES = ImmutableSet.of("127.0.0.1", "127.0.1.1", "localhost");
}
