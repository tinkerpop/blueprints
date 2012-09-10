package com.tinkerpop.blueprints.impls;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.TestSuite;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class GraphTest extends BaseTest {

    public abstract Graph generateGraph();

    public abstract Graph generateGraph(final String graphDirectoryName);

    public abstract void doTestSuite(final TestSuite testSuite) throws Exception;

}
