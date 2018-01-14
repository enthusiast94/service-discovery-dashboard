package com.manasb.servicediscovery.api;

import java.util.Objects;

public class InstancePayload {

    public final String version;

    public InstancePayload() {
        this("");
    }

    public InstancePayload(String version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstancePayload that = (InstancePayload) o;
        return Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {

        return Objects.hash(version);
    }

    @Override
    public String toString() {
        return "InstancePayload{" +
                "version='" + version + '\'' +
                '}';
    }
}
