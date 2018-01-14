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

public class ZookeeperServiceRegistry<Payload> implements IServiceRegistry<Payload> {

    private static final Logger log = LoggerFactory.getLogger(ZookeeperServiceRegistry.class);

    private final CuratorFramework client;
    private final String servicesBasePath;
    private final String serviceLinksBasePath;
    private final ServiceDiscovery<Payload> allServiceDiscovery;
    private final Class<Payload> payloadClass;

    public ZookeeperServiceRegistry(CuratorFramework client,
                                    String servicesBasePath,
                                    String serviceLinksBasePath,
                                    Class<Payload> payloadClass) {
        this.client = client;
        this.servicesBasePath = servicesBasePath;
        this.serviceLinksBasePath = serviceLinksBasePath;
        this.payloadClass = payloadClass;

        allServiceDiscovery = createServiceDiscovery();
    }

    @Override
    public Closeable registerServiceInstance(String name, String address, int port, Payload payload) throws Exception {
        ServiceDiscovery<Payload> serviceDiscovery = createServiceDiscovery();
        serviceDiscovery.start();
        serviceDiscovery.registerService(ServiceInstance.<Payload>builder()
                .name(name)
                .address(address)
                .port(port)
                .payload(payload)
                .build());

        return serviceDiscovery;
    }

    @Override
    public ServiceConnection<Payload> getServiceConnection(String sourceServiceName, String destinationServiceName) throws Exception {
        try (ServiceProvider<Payload> serviceProvider = allServiceDiscovery
                .serviceProviderBuilder()
                .serviceName(destinationServiceName)
                .build()) {
            serviceProvider.start();

            ServiceInstance<Payload> zkInstance = serviceProvider.getInstance();
            com.manasb.servicediscovery.api.domain.ServiceInstance<Payload> instance = new com.manasb.servicediscovery.api.domain.ServiceInstance<>(
                    zkInstance.getName(),
                    zkInstance.getAddress(),
                    zkInstance.getPort(),
                    zkInstance.getRegistrationTimeUTC(),
                    zkInstance.getPayload()
            );
            WebTarget target = ClientBuilder.newClient()
                    .target(instance.address + ":" + instance.port);

            ServiceConnection<Payload> serviceConnection = new ServiceConnection<>(instance, target);

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

    private ServiceDiscovery<Payload> createServiceDiscovery() {
        return ServiceDiscoveryBuilder.builder(payloadClass)
                .client(client)
                .basePath(servicesBasePath)
                .build();
    }
}
