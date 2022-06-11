package edu.upenn.cis.cis455.m2.Session;

import edu.upenn.cis.cis455.m2.interfaces.Session;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SessionImpl extends Session {
    private String id;
    private long creationTime;
    private long lastAccessedTime;
    private boolean valid = true;
    private int maxInactiveInterval = 1000000;
    private Map<String, Object> attributes;

    public SessionImpl(String id) {
        this.id = id;
        this.creationTime = new Date().getTime();
        this.lastAccessedTime = this.creationTime;
        this.attributes = new HashMap<>();
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public long creationTime() {
        return this.creationTime;
    }

    @Override
    public long lastAccessedTime() {
        return this.lastAccessedTime;
    }

    @Override
    public void invalidate() {
        this.valid = false;
    }

    @Override
    public int maxInactiveInterval() {
        return this.maxInactiveInterval;
    }

    @Override
    public void maxInactiveInterval(int interval) {
        this.maxInactiveInterval = interval;
    }

    @Override
    public void access() {
        this.lastAccessedTime = new Date().getTime();
    }

    @Override
    public void attribute(String name, Object value) {
        attributes.put(name, value);
    }

    @Override
    public Object attribute(String name) {
        return attributes.getOrDefault(name, null);
    }

    @Override
    public Set<String> attributes() {
        return attributes.keySet();
    }

    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }
}
