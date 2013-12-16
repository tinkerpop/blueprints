package com.tinkerpop.blueprints.util.wrappers.partition;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.EdgeTestSuite;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphTestSuite;
import com.tinkerpop.blueprints.IndexTestSuite;
import com.tinkerpop.blueprints.IndexableGraphTestSuite;
import com.tinkerpop.blueprints.TestSuite;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexTestSuite;
import com.tinkerpop.blueprints.impls.GraphTest;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.io.gml.GMLReaderTestSuite;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReaderTestSuite;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONReaderTestSuite;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class PartitionGraphTest extends GraphTest {

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

    public void testGraphMLReaderTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new GraphMLReaderTestSuite(this));
        printTestPerformance("GraphMLReaderTestSuite", this.stopWatch());
    }

    public void testGMLReaderTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new GMLReaderTestSuite(this));
        printTestPerformance("GMLReaderTestSuite", this.stopWatch());
    }

    public void testGraphSONReaderTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new GraphSONReaderTestSuite(this));
        printTestPerformance("GraphSONReaderTestSuite", this.stopWatch());
    }

    public Graph generateGraph() {
        return generateGraph("");
    }

    public Graph generateGraph(final String graphDirectoryName) {
        return new PartitionIndexableGraph<TinkerGraph>(new TinkerGraph(), "_writeGraph", "writeGraph", new HashSet<String>(Arrays.asList("writeGraph")));
    }


    public void doTestSuite(final TestSuite testSuite) throws Exception {
        for (Method method : testSuite.getClass().getDeclaredMethods()) {
            if (method.getName().startsWith("test")) {
                System.out.println("Testing " + method.getName() + "...");
                method.invoke(testSuite);
            }
        }
    }

    public void testVerticesSeparatedByEdgeInDifferentPartition() {
        TinkerGraph rawGraph = new TinkerGraph();
        PartitionIndexableGraph graph = new PartitionIndexableGraph(rawGraph, "_writeGraph", "p1");
        Vertex inp1 = graph.addVertex("inp1");

        graph.setWritePartition("p2");
        Vertex inp2 = graph.addVertex("inp2");
        inp2.setProperty("key","value");

        graph.setWritePartition("p3");
        graph.addEdge("inp3", inp1, inp2, "links");

        assertNull(graph.getVertex("inp2"));
        graph.addReadPartition("p2");
        graph.addReadPartition("p3");

        assertNotNull(graph.getVertex("inp1"));
        assertNotNull(graph.getVertex("inp2"));
        assertTrue(graph.getVertex("inp1").getEdges(Direction.OUT).iterator().hasNext());

        graph.removeReadPartition("p2");
        assertTrue(graph.getVertex("inp1").getEdges(Direction.OUT).iterator().hasNext());

        // the vertex at the end of this traversal is in the p2 partition which was removed above.  it should return
        // null, throw exception, something....
        assertNull(graph.getVertex("inp1").getEdges(Direction.OUT).iterator().next().getVertex(Direction.IN));
    }

    public void testSpecificBehavior() {
        TinkerGraph rawGraph = new TinkerGraph();
        PartitionIndexableGraph graph = new PartitionIndexableGraph(rawGraph, "_writeGraph", "a");
        assertEquals(graph.getReadPartitions().size(), 1);
        assertTrue(graph.getReadPartitions().contains("a"));
        assertEquals(graph.getWritePartition(), "a");

        Vertex marko = graph.addVertex(null);
        Vertex rawMarko = ((PartitionVertex) marko).getBaseVertex();
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
        Vertex rawPeter = ((PartitionVertex) peter).getBaseVertex();
        assertEquals(peter.getPropertyKeys().size(), 0);
        assertEquals(rawPeter.getPropertyKeys().size(), 1);
        assertNull(peter.getProperty("_writeGraph"));
        assertEquals(rawPeter.getProperty("_writeGraph"), "b");
        assertEquals(((PartitionVertex) peter).getPartition(), "b");
        assertEquals(count(graph.getVertices()), 1);
        assertEquals(graph.getVertices().iterator().next(), marko);
        assertEquals(count(graph.getEdges()), 0);

        graph.removeReadPartition("a");
        assertEquals(graph.getReadPartitions().size(), 0);
        assertEquals(graph.getWritePartition(), "b");
        assertEquals(count(graph.getVertices()), 0);
        assertEquals(count(graph.getEdges()), 0);
        graph.addReadPartition("b");
        assertEquals(graph.getReadPartitions().size(), 1);
        assertTrue(graph.getReadPartitions().contains("b"));
        assertEquals(graph.getWritePartition(), "b");
        assertEquals(count(graph.getVertices()), 1);
        assertEquals(graph.getVertices().iterator().next(), peter);
        assertEquals(count(graph.getEdges()), 0);

        graph.addReadPartition("a");
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
        Edge rawKnows = ((PartitionEdge) knows).getBaseEdge();
        assertEquals(count(graph.getVertices()), 2);
        assertEquals(count(graph.getEdges()), 0);
        graph.addReadPartition("c");
        assertEquals(count(graph.getVertices()), 2);
        assertEquals(count(graph.getEdges()), 1);
        assertEquals(knows.getPropertyKeys().size(), 0);
        assertEquals(rawKnows.getPropertyKeys().size(), 1);
        assertNull(knows.getProperty("_writeGraph"));
        assertEquals(rawKnows.getProperty("_writeGraph"), "c");
        assertEquals(((PartitionEdge) knows).getPartition(), "c");
        assertEquals(graph.getEdges().iterator().next(), knows);

        graph.removeReadPartition("a");
        graph.removeReadPartition("b");
        assertEquals(graph.getReadPartitions().size(), 1);
        assertTrue(graph.getReadPartitions().contains("c"));
        assertEquals(graph.getWritePartition(), "c");
        assertEquals(count(graph.getVertices()), 0);
        assertEquals(count(graph.getEdges()), 1);
        assertNull(knows.getVertex(Direction.IN));
        assertNull(knows.getVertex(Direction.OUT));

        // testing indices
        /*marko.setProperty("name", "marko");
        peter.setProperty("name", "peter");
        assertEquals(count(graph.getIndex(Index.VERTICES, Vertex.class).get("name", "marko")), 0);
        graph.addReadPartition("a");
        assertEquals(count(graph.getIndex(Index.VERTICES, Vertex.class).get("name", "marko")), 1);
        assertEquals(count(graph.getIndex(Index.VERTICES, Vertex.class).get("name", "peter")), 0);
        assertEquals(graph.getIndex(Index.VERTICES, Vertex.class).get("name", "marko").next(), marko);
        graph.removeReadPartition("a");
        graph.addReadPartition("b");
        assertEquals(count(graph.getIndex(Index.VERTICES, Vertex.class).get("name", "peter")), 1);
        assertEquals(count(graph.getIndex(Index.VERTICES, Vertex.class).get("name", "marko")), 0);
        assertEquals(graph.getIndex(Index.VERTICES, Vertex.class).get("name", "peter").next(), peter);
        graph.addReadPartition("a");
        assertEquals(count(graph.getIndex(Index.VERTICES, Vertex.class).get("name", "peter")), 1);
        assertEquals(count(graph.getIndex(Index.VERTICES, Vertex.class).get("name", "marko")), 1);

        assertEquals(count(graph.getIndex(Index.EDGES, Edge.class).get("label", "knows")), 1);
        assertEquals(graph.getIndex(Index.EDGES, Edge.class).get("label", "knows").next(), knows);
        graph.removeReadPartition("c");
        assertEquals(count(graph.getIndex(Index.EDGES, Edge.class).get("label", "knows")), 0);
        */

        graph.shutdown();
    }
}