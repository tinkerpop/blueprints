package com.tinkerpop.blueprints.impls.rexster;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.EdgeTestSuite;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQueryTestSuite;
import com.tinkerpop.blueprints.GraphTestSuite;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.IndexTestSuite;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.IndexableGraphTestSuite;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.KeyIndexableGraphTestSuite;
import com.tinkerpop.blueprints.TestSuite;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexQueryTestSuite;
import com.tinkerpop.blueprints.VertexTestSuite;
import com.tinkerpop.blueprints.impls.GraphTest;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class RexsterGraphTest extends GraphTest {

    private String username = null;
    private String password = null;

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

    public void testVertexQueryTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new VertexQueryTestSuite(this));
        printTestPerformance("VertexQueryTestSuite", this.stopWatch());
    }

    public void testGraphQueryTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new GraphQueryTestSuite(this));
        printTestPerformance("GraphQueryTestSuite", this.stopWatch());
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

    /*
    TODO: Create a respective test case that doesn't require underscore prefixed properties
    public void testGraphMLReaderTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new GraphMLReaderTestSuite(this));
        printTestPerformance("GraphMLReaderTestSuite", this.stopWatch());
    }*/


    public Graph generateGraph() {
        return new RexsterGraph(this.getWorkingUri(), this.username, this.password);
    }

    public Graph generateGraph(final String graphDirectoryName) {
        throw new UnsupportedOperationException();
    }

    public void doTestSuite(final TestSuite testSuite) throws Exception {
        // "http://127.0.0.1:8182/graphs/emptygraph"
        String doTest = System.getProperty("testRexsterGraph", "true");
        if (doTest.equals("true")) {

            this.username = System.getProperty("username");
            this.password = System.getProperty("password");

            this.resetGraph();
            for (Method method : testSuite.getClass().getDeclaredMethods()) {
                if (method.getName().startsWith("test")) {
                    System.out.println("Testing " + method.getName() + "...");
                    this.resetGraph();
                    method.invoke(testSuite);
                    this.resetGraph();
                }
            }
        }
    }

    protected void resetGraph() {
        final KeyIndexableGraph graph = (KeyIndexableGraph) this.generateGraph();
        final IndexableGraph idxGraph = (IndexableGraph) graph;

        // since we don't have graph.clear() anymore we manually reset the graph.
        Iterator<Vertex> vertexItty = graph.getVertices().iterator();
        List<Vertex> verticesToRemove = new ArrayList<Vertex>();
        while (vertexItty.hasNext()) {
            verticesToRemove.add(vertexItty.next());
        }

        for (Vertex vertexToRemove : verticesToRemove) {
            graph.removeVertex(vertexToRemove);
        }

        for (String key : graph.getIndexedKeys(Vertex.class)) {
            graph.dropKeyIndex(key, Vertex.class);
        }

        for (String key : graph.getIndexedKeys(Edge.class)) {
            graph.dropKeyIndex(key, Edge.class);
        }

        for (Index idx : idxGraph.getIndices()) {
            idxGraph.dropIndex(idx.getIndexName());
        }
    }

    private String getWorkingUri() {
        return System.getProperty("rexsterGraphURI", "http://127.0.0.1:8182/graphs/emptygraph");
    }
}