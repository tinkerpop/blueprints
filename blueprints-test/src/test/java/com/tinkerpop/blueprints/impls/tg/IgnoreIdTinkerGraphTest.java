package com.tinkerpop.blueprints.impls.tg;

import com.tinkerpop.blueprints.*;
import com.tinkerpop.blueprints.impls.GraphTest;
import com.tinkerpop.blueprints.util.io.gml.GMLReaderTestSuite;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReaderTestSuite;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONReaderTestSuite;

import java.io.File;
import java.lang.reflect.Method;

/**
 * Tests IgnoreIdTinkerGraph using the standard test suite.
 *
 * @author Matthias Broecheler (http://www.matthiasb.com)
 */
public class IgnoreIdTinkerGraphTest extends TinkerGraphTest {

    @Override
    public Graph generateGraph() {
        return new IgnoreIdTinkerGraph(getDirectory());
    }

}
