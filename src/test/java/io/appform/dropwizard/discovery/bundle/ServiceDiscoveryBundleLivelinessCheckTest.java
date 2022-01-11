package io.appform.dropwizard.discovery.bundle;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.appform.dropwizard.discovery.bundle.config.LivelinessCheck;
import io.appform.ranger.core.healthcheck.HealthcheckStatus;
import io.appform.ranger.core.healthservice.monitor.IsolatedHealthMonitor;
import io.dropwizard.Configuration;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.AdminEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.curator.test.TestingCluster;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.LoggerFactory;

import java.util.SortedMap;

import static io.appform.dropwizard.discovery.bundle.TestUtils.assertNodeAbsence;
import static io.appform.dropwizard.discovery.bundle.TestUtils.assertNodePresence;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@SuppressWarnings("ALL")
@Slf4j
public class ServiceDiscoveryBundleLivelinessCheckTest {


    private final HealthCheckRegistry healthChecks = mock(HealthCheckRegistry.class);
    private final JerseyEnvironment jerseyEnvironment = mock(JerseyEnvironment.class);
    private final MetricRegistry metricRegistry = mock(MetricRegistry.class);
    private final LifecycleEnvironment lifecycleEnvironment = new LifecycleEnvironment(metricRegistry);
    private final Environment environment = mock(Environment.class);
    private final Bootstrap<?> bootstrap = mock(Bootstrap.class);
    private final Configuration configuration = mock(Configuration.class);
    private final SortedMap<String, Gauge> gauges = mock(SortedMap.class);
    private final Gauge gauge = mock(Gauge.class);
    private String gaugeRatio = "0.5";

    static {
        val root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
    }


    private final ServiceDiscoveryBundle<Configuration> bundle = new ServiceDiscoveryBundle<Configuration>() {
        @Override
        protected ServiceDiscoveryConfiguration getRangerConfiguration(Configuration configuration) {
            return serviceDiscoveryConfiguration;
        }

        @Override
        protected String getServiceName(Configuration configuration) {
            return "TestService";
        }

    };

    private ServiceDiscoveryConfiguration serviceDiscoveryConfiguration;
    private final TestingCluster testingCluster = new TestingCluster(1);

    @BeforeEach
    void setup() throws Exception {
        when(gauge.getValue()).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocationOnMock) throws Throwable {
                return gaugeRatio;
            }
        });
        when(gauges.get(Constants.THREAD_UTIL_GAUGE)).thenReturn(gauge);
        when(metricRegistry.getGauges()).thenReturn(gauges);
        when(environment.metrics()).thenReturn(metricRegistry);
        when(jerseyEnvironment.getResourceConfig()).thenReturn(new DropwizardResourceConfig());
        when(environment.jersey()).thenReturn(jerseyEnvironment);
        when(environment.lifecycle()).thenReturn(lifecycleEnvironment);
        when(environment.healthChecks()).thenReturn(healthChecks);
        when(environment.getObjectMapper()).thenReturn(new ObjectMapper());
        AdminEnvironment adminEnvironment = mock(AdminEnvironment.class);
        doNothing().when(adminEnvironment).addTask(any());
        when(environment.admin()).thenReturn(adminEnvironment);

        testingCluster.start();

        val livelinessCheck = LivelinessCheck.builder()
                .unhealthyThreshold(1)
                .initialDelayForMonitor(2)
                .checkInterval(2)
                .stalesnessInterval(2)
                .build();
        serviceDiscoveryConfiguration = ServiceDiscoveryConfiguration.builder()
                .zookeeper(testingCluster.getConnectString())
                .namespace("test")
                .environment("testing")
                .connectionRetryIntervalMillis(5000)
                .publishedHost("TestHost")
                .publishedPort(8021)
                .initialRotationStatus(true)
                .livelinessCheck(livelinessCheck)
                .build();
        bundle.initialize(bootstrap);
        bundle.run(configuration, environment);
        bundle.getServerStatus().markStarted();
        for (LifeCycle lifeCycle : lifecycleEnvironment.getManagedObjects()){
            lifeCycle.start();
        }
        bundle.getHealthMonitors().forEach(IsolatedHealthMonitor::disable);
    }

    @AfterEach
    void tearDown() throws Exception {
        for (LifeCycle lifeCycle: lifecycleEnvironment.getManagedObjects()){
            lifeCycle.stop();
        }
        testingCluster.stop();
    }

    @Test
    void testDiscovery() {
        assertNodePresence(bundle);
        val info = bundle.getServiceDiscoveryClient()
                .getNode()
                .orElse(null);
        Assertions.assertNotNull(info);
        Assertions.assertNotNull(info.getNodeData());
        Assertions.assertEquals("testing", info.getNodeData().getEnvironment());
        Assertions.assertEquals("TestHost", info.getHost());
        Assertions.assertEquals(8021, info.getPort());
        gaugeRatio = "1.0";
        assertNodeAbsence(bundle);
    }
}
