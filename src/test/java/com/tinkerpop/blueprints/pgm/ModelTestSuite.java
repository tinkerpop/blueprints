package com.tinkerpop.blueprints.pgm;

import com.tinkerpop.blueprints.BaseTest;

import java.util.*;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class ModelTestSuite extends BaseTest {

    protected SuiteConfiguration config;

    public ModelTestSuite() {
    }

    public ModelTestSuite(final SuiteConfiguration config) {
        this.config = config;
    }

    protected static List<String> generateIds(int number) {
        Set<String> ids = new HashSet<String>();
        Random random = new Random();
        while (ids.size() < number) {
            ids.add("" + Math.abs(random.nextInt()));
        }
        return new ArrayList<String>(ids);
    }

    protected String convertId(final String id) {
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
