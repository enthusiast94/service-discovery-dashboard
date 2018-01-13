package com.manasb.servicediscovery.api.domain;

import java.util.Objects;

public class ServiceInstance {

    public final String name;
    public final String address;
    public final int port;
    public final long registrationTimeUtc;

    private ServiceInstance(String name, String address, int port, long registrationTimeUtc) {
        this.name = name;
        this.address = address;
        this.port = port;
        this.registrationTimeUtc = registrationTimeUtc;
    }

    public static NameStep builder() {
        return new Builder();
    }

    public interface NameStep {
        AddressStep name(String name);
    }

    public interface AddressStep {
        PortStep address(String address);
    }

    public interface PortStep {
        Builder port(int port);
    }

    public static class Builder implements NameStep, AddressStep, PortStep {

        private String name;
        private String address;
        private int port;
        private long registrationTimeUtc;

        private Builder() {}

        @Override
        public AddressStep name(String name) {
            this.name = name;
            return this;
        }

        @Override
        public PortStep address(String address) {
            this.address = address;
            return this;
        }

        @Override
        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder registrationTimeUtc(long time) {
            this.registrationTimeUtc = time;
            return this;
        }

        public ServiceInstance build() {
            return new ServiceInstance(name, address, port, registrationTimeUtc);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceInstance that = (ServiceInstance) o;
        return port == that.port &&
                registrationTimeUtc == that.registrationTimeUtc &&
                Objects.equals(name, that.name) &&
                Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, address, port, registrationTimeUtc);
    }

    @Override
    public String toString() {
        return "ServiceInstance{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", port=" + port +
                ", registrationTimeUtc=" + registrationTimeUtc +
                '}';
    }
}
