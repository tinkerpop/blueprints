package com.tinkerpop.blueprints.pgm;

import com.tinkerpop.blueprints.BaseTest;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class ModelTestSuite extends BaseTest {

    protected SuiteConfiguration config;

    public ModelTestSuite() {
    }

    public ModelTestSuite(SuiteConfiguration config) {
        this.config = config;
    }

    protected String convertId(String id) {
        if (this.config.requiresRDFIds) {
            return "gremlin:" + id;
        } else {
            return id;
        }
    }

    public void testTrue() {
        assertTrue(true);
    }
}
