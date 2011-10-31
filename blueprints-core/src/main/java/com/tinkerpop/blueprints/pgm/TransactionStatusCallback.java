package com.tinkerpop.blueprints.pgm;

public interface TransactionStatusCallback {

    public void success();

    public void failure();

    public void start();
}
