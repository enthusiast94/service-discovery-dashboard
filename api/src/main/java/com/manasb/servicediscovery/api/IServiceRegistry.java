package com.manasb.servicediscovery.api;

import com.manasb.servicediscovery.api.domain.ServiceConnection;

import java.io.Closeable;

public interface IServiceRegistry {

    Closeable registerServiceInstance(String name, String ipAddress, int port) throws Exception;

    ServiceConnection getServiceConnection(String sourceServiceName, String destinationServiceName) throws Exception;
}
