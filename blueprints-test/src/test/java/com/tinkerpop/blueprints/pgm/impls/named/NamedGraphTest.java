package com.tinkerpop.blueprints.pgm.impls.named;

import com.tinkerpop.blueprints.pgm.AutomaticIndexTestSuite;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.EdgeTestSuite;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.GraphTestSuite;
import com.tinkerpop.blueprints.pgm.IndexTestSuite;
import com.tinkerpop.blueprints.pgm.IndexableGraphTestSuite;
import com.tinkerpop.blueprints.pgm.TestSuite;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.VertexTestSuite;
import com.tinkerpop.blueprints.pgm.impls.GraphTest;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.pgm.util.graphml.GraphMLReaderTestSuite;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class NamedGraphTest extends GraphTest {

    public NamedGraphTest() {
        this.allowsDuplicateEdges = true;
        this.allowsSelfLoops = true;
        this.ignoresSuppliedIds = false;
        this.isPersistent = false;
        this.isRDFModel = false;
        this.supportsVertexIteration = true;
        this.supportsEdgeIteration = true;
        this.supportsVertexIndex = true;
        this.supportsEdgeIndex = true;
        this.supportsTransactions = false;
    }

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

    public void testAutomaticIndexTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new AutomaticIndexTestSuite(this));
        printTestPerformance("AutomaticIndexTestSuite", this.stopWatch());
    }

    public void testGraphMLReaderTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new GraphMLReaderTestSuite(this));
        printTestPerformance("GraphMLReaderTestSuite", this.stopWatch());
    }

    public Graph getGraphInstance() {
        return new NamedIndexableGraph(new TinkerGraph(), "_writeGraph", "writeGraph", new HashSet<String>(Arrays.asList("writeGraph")));
    }


    public void doTestSuite(final TestSuite testSuite) throws Exception {
        for (Method method : testSuite.getClass().getDeclaredMethods()) {
            if (method.getName().startsWith("test")) {
                System.out.println("Testing " + method.getName() + "...");
                method.invoke(testSuite);
            }
        }
    }

    public void testSpecificBehavior() {
        TinkerGraph rawGraph = new TinkerGraph();
        NamedGraph graph = new NamedGraph(rawGraph, "_writeGraph", "a");
        Vertex marko = graph.addVertex(null);
        Vertex rawMarko = ((NamedVertex) marko).getRawVertex();
        assertEquals(marko.getPropertyKeys().size(), 0);
        assertEquals(rawMarko.getPropertyKeys().size(), 1);
        assertNull(marko.getProperty("_writeGraph"));
        assertEquals(rawMarko.getProperty("_writeGraph"), "a");
        assertEquals(((NamedVertex) marko).getWriteGraph(), "a");
        assertEquals(count(graph.getVertices()), 1);
        assertEquals(graph.getVertices().iterator().next(), marko);
        assertEquals(count(graph.getEdges()), 0);

        graph.setWriteGraph("b");
        Vertex peter = graph.addVertex(null);
        Vertex rawPeter = ((NamedVertex) peter).getRawVertex();
        assertEquals(peter.getPropertyKeys().size(), 0);
        assertEquals(rawPeter.getPropertyKeys().size(), 1);
        assertNull(peter.getProperty("_writeGraph"));
        assertEquals(rawPeter.getProperty("_writeGraph"), "b");
        assertEquals(((NamedVertex) peter).getWriteGraph(), "b");
        assertEquals(count(graph.getVertices()), 1);
        assertEquals(graph.getVertices().iterator().next(), marko);
        assertEquals(count(graph.getEdges()), 0);

        graph.removeReadGraph("a");
        assertEquals(count(graph.getVertices()), 0);
        assertEquals(count(graph.getEdges()), 0);
        graph.addReadGraph("b");
        assertEquals(count(graph.getVertices()), 1);
        assertEquals(graph.getVertices().iterator().next(), peter);
        assertEquals(count(graph.getEdges()), 0);

        graph.addReadGraph("a");
        assertEquals(count(graph.getVertices()), 2);
        assertEquals(count(graph.getEdges()), 0);

        graph.setWriteGraph("c");
        Edge knows = graph.addEdge(null, marko, peter, "knows");
        Edge rawKnows = ((NamedEdge) knows).getRawEdge();
        assertEquals(count(graph.getVertices()), 2);
        assertEquals(count(graph.getEdges()), 0);
        graph.addReadGraph("c");
        assertEquals(count(graph.getVertices()), 2);
        assertEquals(count(graph.getEdges()), 1);
        assertEquals(knows.getPropertyKeys().size(), 0);
        assertEquals(rawKnows.getPropertyKeys().size(), 1);
        assertNull(knows.getProperty("_writeGraph"));
        assertEquals(rawKnows.getProperty("_writeGraph"), "c");
        assertEquals(((NamedEdge) knows).getWriteGraph(), "c");
        assertEquals(graph.getEdges().iterator().next(), knows);

        graph.removeReadGraph("a");
        graph.removeReadGraph("b");
        assertEquals(count(graph.getVertices()), 0);
        assertEquals(count(graph.getEdges()), 1);
        assertEquals(knows.getInVertex(), peter);
        assertEquals(knows.getOutVertex(), marko);
        
        graph.shutdown();
    }
}