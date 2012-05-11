package com.tinkerpop.blueprints.pgm.util.wrappers.bulk;

import com.tinkerpop.blueprints.pgm.EdgeTestSuite;
import com.tinkerpop.blueprints.pgm.Features;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.GraphTestSuite;
import com.tinkerpop.blueprints.pgm.TestSuite;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.VertexTestSuite;
import com.tinkerpop.blueprints.pgm.impls.GraphTest;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.pgm.util.io.graphml.GraphMLReaderTestSuite;
import com.tinkerpop.blueprints.pgm.util.wrappers.batch.BufferGraph;

import java.lang.reflect.Method;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class BufferGraphTest extends GraphTest {

    public void testVertexTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new VertexTestSuite(this));
        printTestPerformance("VertexTestSuite", this.stopWatch());
    }

    public void testEdgeTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new EdgeTestSuite(this));
        printTestPerformance("EdgeTestSuite", this.stopWatch());
    }

    public void testGraphTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new GraphTestSuite(this));
        printTestPerformance("GraphTestSuite", this.stopWatch());
    }

    public void testGraphMLReaderTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new GraphMLReaderTestSuite(this));
        printTestPerformance("GraphMLReaderTestSuite", this.stopWatch());
    }

    public void testMutationCounters() {
        BufferGraph graph = (BufferGraph) this.generateGraph();
        assertEquals(graph.getBufferSize(), 10);
        assertEquals(graph.getMutationCounter(), 0);
        for (int i = 0; i < 9; i++) {
            graph.addVertex(null);
            assertEquals(graph.getBufferSize(), 10);
            assertEquals(graph.getMutationCounter(), i + 1);
        }
        Vertex v = graph.addVertex(null);
        assertEquals(graph.getBufferSize(), 10);
        assertEquals(graph.getMutationCounter(), 0);

        for (int i = 0; i < 9; i++) {
            graph.addEdge(null, v, v, "self-" + i);
            assertEquals(graph.getBufferSize(), 10);
            assertEquals(graph.getMutationCounter(), i + 1);
        }
        graph.addEdge(null, v, v, "knows");
        assertEquals(graph.getBufferSize(), 10);
        assertEquals(graph.getMutationCounter(), 0);

        graph.shutdown();
    }

    public Graph generateGraph() {
        return new BufferGraph<TinkerTransactionalGraph>(new TinkerTransactionalGraph(), 10) {
            public Features getFeatures() {
                final Features features = super.getFeatures();
                features.isPersistent = false;
                return features;
            }
        };
    }

    public void doTestSuite(final TestSuite testSuite) throws Exception {
        for (Method method : testSuite.getClass().getDeclaredMethods()) {
            if (method.getName().startsWith("test")) {
                System.out.println("Testing " + method.getName() + "...");
                method.invoke(testSuite);
            }
        }
    }

    private class TinkerTransactionalGraph extends TinkerGraph implements TransactionalGraph {
        public void startTransaction() {
        }

        public void stopTransaction(Conclusion conclusion) {
        }
    }
}