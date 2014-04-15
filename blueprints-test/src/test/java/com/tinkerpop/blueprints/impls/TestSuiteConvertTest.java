package com.tinkerpop.blueprints.impls;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.EdgeTestSuite;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphTestSuite;
import com.tinkerpop.blueprints.IndexTestSuite;
import com.tinkerpop.blueprints.IndexableGraphTestSuite;
import com.tinkerpop.blueprints.KeyIndexableGraphTestSuite;
import com.tinkerpop.blueprints.TestSuite;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexTestSuite;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

import java.lang.reflect.Method;
import java.util.Random;

/**
 * Tests that the test suites use the GraphTest convertId and convertLabel
 * methods appropriately.
 *
 * @author Christofer Hedbrandh (http://www.knewton.com)
 */
public class TestSuiteConvertTest extends GraphTest {

    private static final String ID_PREFIX = "id:";
    private static final String LABEL_PREFIX = "label:";


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

    public void testKeyIndexableGraphTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new KeyIndexableGraphTestSuite(this));
        printTestPerformance("KeyIndexableGraphTestSuite", this.stopWatch());
    }

    public void testIndexableGraphTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new IndexableGraphTestSuite(this));
        printTestPerformance("IndexableGraphTestSuite", this.stopWatch());
    }

    public void testIndexTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new IndexTestSuite(this));
        printTestPerformance("IndexTestSuite", this.stopWatch());
    }

    public Graph generateGraph() {
        return generateGraph("");
    }

    public Graph generateGraph(final String graphDirectoryName) {
        return new TypeSensitiveTestGraph();
    }

    public void doTestSuite(final TestSuite testSuite) throws Exception {
        for (Method method : testSuite.getClass().getDeclaredMethods()) {
            if (method.getName().startsWith("test")) {
                System.out.println("Testing " + method.getName() + "...");
                method.invoke(testSuite);
            }
        }
    }

    @Override
    public Object convertId(final Object id) {
        return ID_PREFIX + id.toString();
    }

    @Override
    public String convertLabel(final String label) {
        return LABEL_PREFIX + label;
    }

    /**
     * Extension of TinkerGraph that only allows vertex IDs and edge labels
     * with some prefix. If provided vertex IDs and labels are not on the
     * required format, an IllegalArgumentException is thrown.
     */
    private static class TypeSensitiveTestGraph extends TinkerGraph {

        private static final Random random = new Random();

        @Override
        public Vertex addVertex(Object id) {
            if (id == null) {
                id = ID_PREFIX + random.nextLong();
            }
            verifyIdType(id);
            return super.addVertex(id);
        }

        @Override
        public Vertex getVertex(final Object id) {
            verifyIdType(id);
            return super.getVertex(id);
        }

        @Override
        public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
            verifyLabelType(label);
            return super.addEdge(id, outVertex, inVertex, label);
        }

        private static void verifyIdType(Object id) {
            if (id != null && !id.toString().startsWith(ID_PREFIX)) {
                throw new IllegalArgumentException("ID must start with " + ID_PREFIX);
            }
        }

        private static void verifyLabelType(String label) {
            if (label != null && !label.startsWith(LABEL_PREFIX)) {
                throw new IllegalArgumentException("Label must start with " + LABEL_PREFIX);
            }
        }
    }
}
