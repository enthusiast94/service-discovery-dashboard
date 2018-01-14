package com.manasb.servicediscovery.api.domain;

import java.util.Objects;

public class ServiceInstance<Payload> {

    public final String name;
    public final String address;
    public final int port;
    public final long registrationTimeUtc;
    public final Payload payload;

    public ServiceInstance(String name, String address, int port, long registrationTimeUtc, Payload payload) {
        this.name = name;
        this.address = address;
        this.port = port;
        this.registrationTimeUtc = registrationTimeUtc;
        this.payload = payload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceInstance that = (ServiceInstance) o;
        return port == that.port &&
                registrationTimeUtc == that.registrationTimeUtc &&
                Objects.equals(name, that.name) &&
                Objects.equals(address, that.address) &&
                Objects.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, address, port, registrationTimeUtc, payload);
    }

    @Override
    public String toString() {
        return "ServiceInstance{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", port=" + port +
                ", registrationTimeUtc=" + registrationTimeUtc +
                ", payload=" + payload +
                '}';
    }
}
