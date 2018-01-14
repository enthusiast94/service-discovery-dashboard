package com.manasb.servicediscovery.api;

import com.manasb.servicediscovery.api.domain.ServiceConnection;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.TestingServer;
import org.junit.jupiter.api.*;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.*;

class ZookeeperServiceRegistryTest {

    private static TestingServer zooKeeperServer;
    private static CuratorFramework client;

    @BeforeEach
    void setUp() throws Exception {
        zooKeeperServer = new TestingServer(2181);
        zooKeeperServer.start();
        client = CuratorFrameworkFactory.newClient(zooKeeperServer.getConnectString(), new RetryOneTime(1000));
        client.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        zooKeeperServer.close();
        client.close();
    }

    @Test
    void shouldRegisterServiceInstance() throws Exception {
        ZookeeperServiceRegistry<InstancePayload> serviceRegistry = new ZookeeperServiceRegistry<>(client,
                "services", "serviceLinks", InstancePayload.class);
        Closeable closeable = serviceRegistry.registerServiceInstance("OMS", "localhost", 1,
                new InstancePayload("1.2.3"));

        List<String> services = client.getChildren().forPath("/services");
        assertThat(services.size(), is(1));
        assertThat(services.get(0), is("OMS"));

        List<String> instances = client.getChildren().forPath("/services/OMS");
        assertThat(instances.size(), is(1));

        closeable.close();
    }

    @Test
    void shouldGetServiceConnection() throws Exception {
        ZookeeperServiceRegistry<InstancePayload> serviceRegistry = new ZookeeperServiceRegistry<>(client,
                "services", "serviceLinks", InstancePayload.class);
        Closeable closeable = serviceRegistry.registerServiceInstance("OMS", "localhost", 1,
                new InstancePayload("1.2.3"));

        ServiceConnection<InstancePayload> serviceConnection = serviceRegistry.getServiceConnection("test", "OMS");
        assertThat(serviceConnection.serviceInstance.name, is("OMS"));
        assertThat(serviceConnection.serviceInstance.address, is("localhost"));
        assertThat(serviceConnection.serviceInstance.port, is(1));
        assertThat(serviceConnection.serviceInstance.payload.version, is("1.2.3"));
        assertThat(serviceConnection.target.getUri().toString(), is("localhost:1"));

        closeable.close();
    }

    @Test
    void shouldRegisterLinksOnGettingServiceConnection() throws Exception {
        ZookeeperServiceRegistry<InstancePayload> serviceRegistry = new ZookeeperServiceRegistry<>(client,
                "services", "serviceLinks", InstancePayload.class);
        Closeable closeable = serviceRegistry.registerServiceInstance("OMS", "localhost", 1,
                new InstancePayload("1.2.3"));

        ServiceConnection serviceConnection = serviceRegistry.getServiceConnection("test", "OMS");

        List<String> services = client.getChildren().forPath("/serviceLinks");
        assertThat(services.size(), is(1));
        assertThat(services.get(0), is("OMS"));

        List<String> incomingConnections = client.getChildren().forPath("/serviceLinks/OMS");
        assertThat(incomingConnections.size(), is(1));
        assertThat(incomingConnections.get(0), is("test"));

        List<String> incomingConnectionInstances = client.getChildren().forPath("/serviceLinks/OMS/test");
        assertThat(incomingConnectionInstances.size(), is(1));

        closeable.close();
        serviceConnection.close();
    }

    @Test
    void shouldUnregisterLinkWhenConnectionIsClosed() throws Exception {
        ZookeeperServiceRegistry<InstancePayload> serviceRegistry = new ZookeeperServiceRegistry<>(client,
                "services", "serviceLinks", InstancePayload.class);
        Closeable closeable = serviceRegistry.registerServiceInstance("OMS", "localhost", 1,
                new InstancePayload("1.2.3"));

        ServiceConnection serviceConnection = serviceRegistry.getServiceConnection("test", "OMS");

        List<String> incomingConnectionInstances = client.getChildren().forPath("/serviceLinks/OMS/test");
        assertThat(incomingConnectionInstances.size(), is(1));

        serviceConnection.close();

        incomingConnectionInstances = client.getChildren().forPath("/serviceLinks/OMS/test");
        assertThat(incomingConnectionInstances.size(), is(0));

        closeable.close();
    }
}