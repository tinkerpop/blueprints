package com.tinkerpop.blueprints.pgm.util.wrappers.partition;

import com.tinkerpop.blueprints.pgm.AutomaticIndexTestSuite;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.EdgeTestSuite;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.GraphTestSuite;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexTestSuite;
import com.tinkerpop.blueprints.pgm.IndexableGraphTestSuite;
import com.tinkerpop.blueprints.pgm.TestSuite;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.VertexTestSuite;
import com.tinkerpop.blueprints.pgm.impls.GraphTest;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.pgm.util.io.graphml.GraphMLReaderTestSuite;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class PartitionGraphTest extends GraphTest {

    public PartitionGraphTest() {
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
        return new PartitionIndexableGraph(new TinkerGraph(), "_writeGraph", "writeGraph", new HashSet<String>(Arrays.asList("writeGraph")));
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
        PartitionIndexableGraph graph = new PartitionIndexableGraph(rawGraph, "_writeGraph", "a");
        assertEquals(graph.getReadPartitions().size(), 1);
        assertTrue(graph.getReadPartitions().contains("a"));
        assertEquals(graph.getWritePartition(), "a");

        Vertex marko = graph.addVertex(null);
        Vertex rawMarko = ((PartitionVertex) marko).getRawVertex();
        assertEquals(marko.getPropertyKeys().size(), 0);
        assertEquals(rawMarko.getPropertyKeys().size(), 1);
        assertNull(marko.getProperty("_writeGraph"));
        assertEquals(rawMarko.getProperty("_writeGraph"), "a");
        assertEquals(((PartitionVertex) marko).getPartition(), "a");
        assertEquals(count(graph.getVertices()), 1);
        assertEquals(graph.getVertices().iterator().next(), marko);
        assertEquals(count(graph.getEdges()), 0);

        graph.setWritePartition("b");
        assertEquals(graph.getReadPartitions().size(), 1);
        assertTrue(graph.getReadPartitions().contains("a"));
        assertEquals(graph.getWritePartition(), "b");
        Vertex peter = graph.addVertex(null);
        Vertex rawPeter = ((PartitionVertex) peter).getRawVertex();
        assertEquals(peter.getPropertyKeys().size(), 0);
        assertEquals(rawPeter.getPropertyKeys().size(), 1);
        assertNull(peter.getProperty("_writeGraph"));
        assertEquals(rawPeter.getProperty("_writeGraph"), "b");
        assertEquals(((PartitionVertex) peter).getPartition(), "b");
        assertEquals(count(graph.getVertices()), 1);
        assertEquals(graph.getVertices().iterator().next(), marko);
        assertEquals(count(graph.getEdges()), 0);

        graph.removeReadGraph("a");
        assertEquals(graph.getReadPartitions().size(), 0);
        assertEquals(graph.getWritePartition(), "b");
        assertEquals(count(graph.getVertices()), 0);
        assertEquals(count(graph.getEdges()), 0);
        graph.addReadGraph("b");
        assertEquals(graph.getReadPartitions().size(), 1);
        assertTrue(graph.getReadPartitions().contains("b"));
        assertEquals(graph.getWritePartition(), "b");
        assertEquals(count(graph.getVertices()), 1);
        assertEquals(graph.getVertices().iterator().next(), peter);
        assertEquals(count(graph.getEdges()), 0);

        graph.addReadGraph("a");
        assertEquals(graph.getReadPartitions().size(), 2);
        assertTrue(graph.getReadPartitions().contains("a"));
        assertTrue(graph.getReadPartitions().contains("b"));
        assertEquals(graph.getWritePartition(), "b");
        assertEquals(count(graph.getVertices()), 2);
        assertEquals(count(graph.getEdges()), 0);

        graph.setWritePartition("c");
        assertEquals(graph.getReadPartitions().size(), 2);
        assertTrue(graph.getReadPartitions().contains("a"));
        assertTrue(graph.getReadPartitions().contains("b"));
        assertEquals(graph.getWritePartition(), "c");
        Edge knows = graph.addEdge(null, marko, peter, "knows");
        Edge rawKnows = ((PartitionEdge) knows).getRawEdge();
        assertEquals(count(graph.getVertices()), 2);
        assertEquals(count(graph.getEdges()), 0);
        graph.addReadGraph("c");
        assertEquals(count(graph.getVertices()), 2);
        assertEquals(count(graph.getEdges()), 1);
        assertEquals(knows.getPropertyKeys().size(), 0);
        assertEquals(rawKnows.getPropertyKeys().size(), 1);
        assertNull(knows.getProperty("_writeGraph"));
        assertEquals(rawKnows.getProperty("_writeGraph"), "c");
        assertEquals(((PartitionEdge) knows).getPartition(), "c");
        assertEquals(graph.getEdges().iterator().next(), knows);

        graph.removeReadGraph("a");
        graph.removeReadGraph("b");
        assertEquals(graph.getReadPartitions().size(), 1);
        assertTrue(graph.getReadPartitions().contains("c"));
        assertEquals(graph.getWritePartition(), "c");
        assertEquals(count(graph.getVertices()), 0);
        assertEquals(count(graph.getEdges()), 1);
        assertEquals(knows.getInVertex(), peter);
        assertEquals(knows.getOutVertex(), marko);

        // testing indices
        marko.setProperty("name", "marko");
        peter.setProperty("name", "peter");
        assertEquals(count(graph.getIndex(Index.VERTICES, Vertex.class).get("name", "marko")), 0);
        graph.addReadGraph("a");
        assertEquals(count(graph.getIndex(Index.VERTICES, Vertex.class).get("name", "marko")), 1);
        assertEquals(count(graph.getIndex(Index.VERTICES, Vertex.class).get("name", "peter")), 0);
        assertEquals(graph.getIndex(Index.VERTICES, Vertex.class).get("name", "marko").next(), marko);
        graph.removeReadGraph("a");
        graph.addReadGraph("b");
        assertEquals(count(graph.getIndex(Index.VERTICES, Vertex.class).get("name", "peter")), 1);
        assertEquals(count(graph.getIndex(Index.VERTICES, Vertex.class).get("name", "marko")), 0);
        assertEquals(graph.getIndex(Index.VERTICES, Vertex.class).get("name", "peter").next(), peter);
        graph.addReadGraph("a");
        assertEquals(count(graph.getIndex(Index.VERTICES, Vertex.class).get("name", "peter")), 1);
        assertEquals(count(graph.getIndex(Index.VERTICES, Vertex.class).get("name", "marko")), 1);

        assertEquals(count(graph.getIndex(Index.EDGES, Edge.class).get("label", "knows")), 1);
        assertEquals(graph.getIndex(Index.EDGES, Edge.class).get("label", "knows").next(), knows);
        graph.removeReadGraph("c");
        assertEquals(count(graph.getIndex(Index.EDGES, Edge.class).get("label", "knows")), 0);

        graph.shutdown();
    }
}