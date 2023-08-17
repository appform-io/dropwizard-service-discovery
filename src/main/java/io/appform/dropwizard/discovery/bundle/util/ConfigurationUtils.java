package io.appform.dropwizard.discovery.bundle.util;

import static io.appform.dropwizard.discovery.bundle.Constants.HOST_PORT_DELIMITER;
import static io.appform.dropwizard.discovery.bundle.Constants.PATH_DELIMITER;
import static io.appform.dropwizard.discovery.bundle.Constants.ZOOKEEPER_HOST_DELIMITER;

import com.google.common.base.Strings;
import io.appform.dropwizard.discovery.bundle.Constants;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConfigurationUtils {

    public static String resolveNonEmptyPublishedHost(String publishedHost) throws UnknownHostException {
        if (Strings.isNullOrEmpty(publishedHost) || publishedHost.equals(Constants.DEFAULT_HOST)) {
            return InetAddress.getLocalHost()
                    .getCanonicalHostName();
        }
        return publishedHost;
    }

    public static Set<String> resolveZookeeperHosts(String zkHostString) {
        return Arrays.stream(zkHostString.split(ZOOKEEPER_HOST_DELIMITER))
                .map(zkHostPort -> zkHostPort.split(HOST_PORT_DELIMITER)[0])
                .map(zkHostPath -> zkHostPath.split(PATH_DELIMITER)[0])
                .collect(Collectors.toSet());
    }

}
