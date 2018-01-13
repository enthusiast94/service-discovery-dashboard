package com.manasb.servicediscovery.api.domain;

public class Link {

    public final String from;
    public final String to;
    public final Status status;

    public Link(String from, String to, Status status) {
        this.from = from;
        this.to = to;
        this.status = status;
    }

    public enum Status {
        UP, DOWN;
    }
}
