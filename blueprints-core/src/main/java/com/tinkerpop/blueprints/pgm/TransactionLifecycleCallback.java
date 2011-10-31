package com.tinkerpop.blueprints.pgm;

public interface TransactionLifecycleCallback {

    public void success();

    public void failure();

    public void start();
}
