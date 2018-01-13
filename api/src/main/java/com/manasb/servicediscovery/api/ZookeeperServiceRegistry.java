package com.manasb.servicediscovery.api;

import com.manasb.servicediscovery.api.domain.ServiceConnection;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProvider;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.Closeable;
import java.util.UUID;

public class ZookeeperServiceRegistry implements IServiceRegistry {

    private static final Logger log = LoggerFactory.getLogger(ZookeeperServiceRegistry.class);

    private final CuratorFramework client;
    private final String servicesBasePath;
    private final String serviceLinksBasePath;
    private final ServiceDiscovery<Void> allServiceDiscovery;

    public ZookeeperServiceRegistry(CuratorFramework client, String servicesBasePath, String serviceLinksBasePath) {
        this.client = client;
        this.servicesBasePath = servicesBasePath;
        this.serviceLinksBasePath = serviceLinksBasePath;

        allServiceDiscovery = createServiceDiscovery();
    }

    @Override
    public Closeable registerServiceInstance(String name, String address, int port) throws Exception {
        ServiceDiscovery<Void> serviceDiscovery = createServiceDiscovery();
        serviceDiscovery.registerService(ServiceInstance.<Void>builder()
                .name(name)
                .address(address)
                .port(port)
                .build());
        serviceDiscovery.start();

        return serviceDiscovery;
    }

    @Override
    public ServiceConnection getServiceConnection(String sourceServiceName, String destinationServiceName) throws Exception {
        try (ServiceProvider<Void> serviceProvider = allServiceDiscovery
                .serviceProviderBuilder()
                .serviceName(destinationServiceName)
                .build()) {
            serviceProvider.start();

            ServiceInstance<Void> zkInstance = serviceProvider.getInstance();
            com.manasb.servicediscovery.api.domain.ServiceInstance instance = com.manasb.servicediscovery.api.domain.ServiceInstance.builder()
                    .name(zkInstance.getName())
                    .address(zkInstance.getAddress())
                    .port(zkInstance.getPort())
                    .registrationTimeUtc(zkInstance.getRegistrationTimeUTC())
                    .build();
            WebTarget target = ClientBuilder.newClient()
                    .target(instance.address + ":" + instance.port);

            ServiceConnection serviceConnection = new ServiceConnection(instance, target);

            String linkPath = createServiceLink(sourceServiceName, destinationServiceName);

            serviceConnection.onClose(() -> {
                try {
                    client.delete().forPath(linkPath);
                } catch (Exception e) {
                    log.error("Failed to delete zk node with path [{}]", linkPath, e);
                }
            });

            return serviceConnection;
        }
    }

    private String createServiceLink(String sourceServiceName, String destinationServiceName) throws Exception {
        String path = "/" + serviceLinksBasePath + "/" + destinationServiceName+ "/" + sourceServiceName + "/" + UUID.randomUUID();
        client.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .forPath(path);

        return path;
    }

    private ServiceDiscovery<Void> createServiceDiscovery() {
        return ServiceDiscoveryBuilder.builder(Void.class)
                .client(client)
                .basePath(servicesBasePath)
                .build();
    }
}
