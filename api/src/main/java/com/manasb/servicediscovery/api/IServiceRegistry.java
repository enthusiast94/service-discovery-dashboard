package com.manasb.servicediscovery.api;

import com.manasb.servicediscovery.api.domain.ServiceConnection;

import java.io.Closeable;

public interface IServiceRegistry<Payload> {

    Closeable registerServiceInstance(String name, String ipAddress, int port, Payload payload) throws Exception;

    ServiceConnection<Payload> getServiceConnection(String sourceServiceName, String destinationServiceName) throws Exception;
}
