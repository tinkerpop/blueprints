package com.tinkerpop.blueprints.pgm;

import com.tinkerpop.blueprints.BaseTest;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class IndexableGraphTestSuite extends ModelTestSuite {

    public IndexableGraphTestSuite() {
    }

    public IndexableGraphTestSuite(final SuiteConfiguration config) {
        super(config);
    }

    public void testNoManualIndicesOnConstruction(final IndexableGraph graph) {
        int count = 0;
        this.stopWatch();
        for (Index index : graph.getIndices()) {
            count++;
            assertTrue(index instanceof AutomaticIndex);
        }
        BaseTest.printPerformance(graph.toString(), count, "indices iterated through", this.stopWatch());
    }

}
