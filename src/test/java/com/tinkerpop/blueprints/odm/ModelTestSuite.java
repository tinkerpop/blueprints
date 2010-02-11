package com.tinkerpop.blueprints.odm;

import junit.framework.TestCase;
import com.tinkerpop.blueprints.BaseTest;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ModelTestSuite extends BaseTest {
    protected SuiteConfiguration config;

    public ModelTestSuite() {
    }

    public ModelTestSuite(final SuiteConfiguration config) {
        this.config = config;
    }

    public void testTrue() {
        assertTrue(true);
    }
}
