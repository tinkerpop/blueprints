package com.tinkerpop.blueprints.impls.neo4j2;

import java.util.Iterator;
import java.util.List;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.NotInTransactionException;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.index.impl.lucene.LowerCaseKeywordAnalyzer;
import org.neo4j.kernel.ha.HighlyAvailableGraphDatabase;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.TestSuite;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.GraphTest;
import com.tinkerpop.blueprints.impls.neo4j2.index.Neo4j2VertexIndex;
import org.neo4j.kernel.impl.factory.GraphDatabaseFacade;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4j2GraphSpecificTestSuite extends TestSuite {

    public Neo4j2GraphSpecificTestSuite() {
    }

    public Neo4j2GraphSpecificTestSuite(final GraphTest graphTest) {
        super(graphTest);
    }

    public void testLongIdConversions() {
        String id1 = "100";  // good  100
        String id2 = "100.0"; // good 100
        String id3 = "100.1"; // good 100
        String id4 = "one"; // bad

        try {
            Double.valueOf(id1).longValue();
            assertTrue(true);
        } catch (NumberFormatException e) {
            assertFalse(true);
        }
        try {
            Double.valueOf(id2).longValue();
            assertTrue(true);
        } catch (NumberFormatException e) {
            assertFalse(true);
        }
        try {
            Double.valueOf(id3).longValue();
            assertTrue(true);
        } catch (NumberFormatException e) {
            assertFalse(true);
        }
        try {
            Double.valueOf(id4).longValue();
            assertTrue(false);
        } catch (NumberFormatException e) {
            assertFalse(false);
        }
    }

    public void testQueryIndex() throws Exception {
        Neo4j2Graph graph = (Neo4j2Graph) graphTest.generateGraph();
        Index<Vertex> vertexIndex = graph.createIndex("vertices", Vertex.class);
        Index<Edge> edgeIndex = graph.createIndex("edges", Edge.class);

        Vertex a = graph.addVertex(null);
        a.setProperty("name", "marko");
        vertexIndex.put("name", "marko", a);

        Iterator itty = graph.getIndex("vertices", Vertex.class).query("name", "*rko").iterator();
        int counter = 0;
        while (itty.hasNext()) {
            counter++;
            assertEquals(itty.next(), a);
        }
        assertEquals(counter, 1);

        Vertex b = graph.addVertex(null);
        Edge edge = graph.addEdge(null, a, b, "knows");
        edge.setProperty("weight", 0.75);
        edgeIndex.put("weight", 0.75, edge);

        itty = graph.getIndex("edges", Edge.class).query("label", "k?ows").iterator();
        counter = 0;
        while (itty.hasNext()) {
            counter++;
            assertEquals(itty.next(), edge);
        }
        assertEquals(counter, 0);

        itty = graph.getIndex("edges", Edge.class).query("weight", "[0.5 TO 1.0]").iterator();
        counter = 0;
        while (itty.hasNext()) {
            counter++;
            assertEquals(itty.next(), edge);
        }
        assertEquals(counter, 1);
        assertEquals(count(graph.getIndex("edges", Edge.class).query("weight", "[0.1 TO 0.5]")), 0);


        graph.shutdown();
    }

    public void testLuceneQueryIndex() throws Exception {
        Neo4j2Graph graph = (Neo4j2Graph) graphTest.generateGraph();
        Index<Vertex> vertexIndex = graph.createIndex("vertices", Vertex.class);

        Vertex a = graph.addVertex(null);
        vertexIndex.put("name", "marko", a);

        Iterator<Vertex> ittyLuceneQuery = ((Neo4j2VertexIndex) graph.getIndex("vertices", Vertex.class)).query("name:*rko").iterator(); 
        int counter = 0;
        while (ittyLuceneQuery.hasNext()) {
            counter++;
            assertEquals(a, ittyLuceneQuery.next());
        }
        assertEquals(counter, 1);

        vertexIndex.put("name", "marko some_other name", graph.addVertex(null));
        ittyLuceneQuery = ((Neo4j2VertexIndex) graph.getIndex("vertices", Vertex.class)).query("name:*rko*").iterator();

        counter = 0;
        while (ittyLuceneQuery.hasNext()) {
            ittyLuceneQuery.next();
            counter++;
        }
        assertEquals(2, counter);

        graph.shutdown();
    }

    public void testIndexParameters() throws Exception {
        Neo4j2Graph graph = (Neo4j2Graph) graphTest.generateGraph();
        Index<Vertex> index = graph.createIndex("luceneIdx", Vertex.class, new Parameter<String, String>("analyzer", LowerCaseKeywordAnalyzer.class.getName()));
        Vertex a = graph.addVertex(null);
        a.setProperty("name", "marko");
        index.put("name", "marko", a);
        Iterator<?> itty = index.query("name", "*rko").iterator();
        int counter = 0;
        while (itty.hasNext()) {
            counter++;
            assertEquals(itty.next(), a);
        }
        assertEquals(counter, 1);

        itty = index.query("name", "MaRkO").iterator();
        counter = 0;
        while (itty.hasNext()) {
            counter++;
            assertEquals(itty.next(), a);
        }
        assertEquals(counter, 1);

        graph.shutdown();
    }

    public void testAutoStartTxOnVertexIterables() throws Exception {
        Neo4j2Graph graph = (Neo4j2Graph) graphTest.generateGraph();
        Vertex a = graph.addVertex(null);
        Vertex b = graph.addVertex(null);
        graph.addEdge(null, a, b, "testEdge");
        Iterable<Vertex> graphIterable = graph.getVertices();
        Iterable<Vertex> vertexIterable = a.getVertices(Direction.BOTH);
        graph.commit();
        try {
            try {
                assertTrue(graphIterable.iterator().hasNext());
                assertNotNull(graphIterable.iterator().next());
            } catch (NotInTransactionException e) {
                fail("Iterating graph vertex iterable does not auto-start transaction");
            }
            try {
                assertTrue(vertexIterable.iterator().hasNext());
                assertNotNull(vertexIterable.iterator().next());
            } catch (NotInTransactionException e) {
                fail("Iterating vertex iterable does not auto-start transaction");
            }
        } finally {
            graph.shutdown();
        }
    }

    public void testAutoStartTxOnEdgeIterables() throws Exception {
        Neo4j2Graph graph = (Neo4j2Graph) graphTest.generateGraph();
        Vertex a = graph.addVertex(null);
        Vertex b = graph.addVertex(null);
        Neo4j2Edge edge = graph.addEdge(null, a, b, "testEdge");
        edge.setProperty("foo", "bar");
        Iterable<Edge> iterable = graph.getEdges();
        graph.commit();
        try {
            assertTrue(iterable.iterator().hasNext());
            assertNotNull(iterable.iterator().next());
            assertNotNull(graph.getEdges("foo", "bar").iterator().hasNext());
        } catch (NotInTransactionException e) {
            fail("Iterating edge iterable does not auto-start transaction");
        } finally {
            graph.shutdown();
        }
    }

    public void testIteratingDeletedElementsWithoutSCEIT() throws Exception {
        Neo4j2Graph graph = (Neo4j2Graph) graphTest.generateGraph();
        Vertex a = graph.addVertex(null);
        Vertex b = graph.addVertex(null);
        Neo4j2Edge edge = graph.addEdge(null, a, b, "testEdge");
        edge.setProperty("foo", "bar");
        graph.commit();
        List<Vertex> list1 = asList(graph.getVertices());
        assertEquals(2, list1.size());
        assertTrue(asList(graph.getVertices()).contains(a));
        a.remove();
        assertFalse(asList(graph.getVertices()).contains(a));
        assertEquals(1, asList(graph.getVertices()).size());
        graph.commit();
        assertEquals(1, asList(graph.getVertices()).size());
    }

    public void testHaGraph() throws Exception {
        assertTrue(GraphDatabaseFacade.class.isAssignableFrom(HighlyAvailableGraphDatabase.class));

        /*String directory = this.getWorkingDirectory();
        Neo4j2HaGraph graph = new Neo4j2HaGraph(directory);
        graph.shutdown();
        deleteDirectory(new File(directory));*/
    }

    public void testRollbackExceptionOnBeforeTxCommit() throws Exception {
        Neo4j2Graph graph = (Neo4j2Graph) graphTest.generateGraph();
        GraphDatabaseService rawGraph = graph.getRawGraph();
        rawGraph.registerTransactionEventHandler(new TransactionEventHandler<Object>() {
            @Override
            public Object beforeCommit(TransactionData data) throws Exception {
                if (true) {
                    throw new RuntimeException("jippo validation exception");
                }
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void afterCommit(TransactionData data, Object state) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void afterRollback(TransactionData data, Object state) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
        try {
            Vertex vertex = graph.addVertex(null);
            graph.commit();
        } catch (Exception e) {
            graph.rollback();
        }
        assertTrue(!graph.getVertices().iterator().hasNext());
        graph.shutdown();
    }
}
