package com.tinkerpop.blueprints.pgm.impls;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.TestSuite;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class GraphTest extends BaseTest {

    public abstract Graph generateGraph();

    public abstract void doTestSuite(final TestSuite testSuite) throws Exception;

}
