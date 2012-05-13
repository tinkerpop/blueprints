package com.tinkerpop.blueprints;

import com.tinkerpop.blueprints.impls.GraphTest;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class TestSuite extends BaseTest {

    protected GraphTest graphTest;

    public TestSuite() {
    }

    public TestSuite(final GraphTest graphTest) {
        this.graphTest = graphTest;
    }

    protected String convertId(final Graph graph, final String id) {
        if (graph.getFeatures().isRDFModel) {
            return "blueprints:" + id;
        } else {
            return id;
        }
    }
}
