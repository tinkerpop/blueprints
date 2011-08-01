package com.tinkerpop.blueprints.pgm;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.pgm.impls.GraphTest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

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

    protected static List<String> generateIds(int number) {
        Set<String> ids = new HashSet<String>();
        Random random = new Random();
        while (ids.size() < number) {
            ids.add("" + Math.abs(random.nextInt()));
        }
        return new ArrayList<String>(ids);
    }

    protected String convertId(final String id) {
        if (this.graphTest.isRDFModel) {
            return "blueprints:" + id;
        } else {
            return id;
        }
    }
}
