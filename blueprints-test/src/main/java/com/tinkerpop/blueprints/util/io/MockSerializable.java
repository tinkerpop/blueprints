package com.tinkerpop.blueprints.util.io;

import java.io.Serializable;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class MockSerializable implements Serializable {
    private String testField;

    public String getTestField() {
        return this.testField;
    }

    public void setTestField(String testField) {
        this.testField = testField;
    }

}
