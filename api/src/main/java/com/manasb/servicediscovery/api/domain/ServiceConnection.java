package com.manasb.servicediscovery.api.domain;

import javax.ws.rs.client.WebTarget;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

public class ServiceConnection implements Closeable {

    public final ServiceInstance serviceInstance;
    public final WebTarget target;
    private final List<Runnable> closeSubscribers = new ArrayList<>();

    public ServiceConnection(ServiceInstance serviceInstance, WebTarget target) {
        this.serviceInstance = serviceInstance;
        this.target = target;
    }

    public void onClose(Runnable runnable) {
        closeSubscribers.add(runnable);
    }

    @Override
    public void close() {
        closeSubscribers.forEach(Runnable::run);
    }
}
