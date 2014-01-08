package com.tinkerpop.blueprints.impls.neo4j;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.TestSuite;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.GraphTest;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.index.impl.lucene.LowerCaseKeywordAnalyzer;
import org.neo4j.kernel.InternalAbstractGraphDatabase;
import org.neo4j.kernel.ha.HighlyAvailableGraphDatabase;
import org.neo4j.tooling.GlobalGraphOperations;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jGraphSpecificTestSuite extends TestSuite {

    public Neo4jGraphSpecificTestSuite() {
    }

    public Neo4jGraphSpecificTestSuite(final GraphTest graphTest) {
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
        Neo4jGraph graph = (Neo4jGraph) graphTest.generateGraph();
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

    public void testIndexParameters() throws Exception {
        Neo4jGraph graph = (Neo4jGraph) graphTest.generateGraph();
        Index<Vertex> index = graph.createIndex("luceneIdx", Vertex.class, new Parameter<String, String>("analyzer", LowerCaseKeywordAnalyzer.class.getName()));
        Vertex a = graph.addVertex(null);
        a.setProperty("name", "marko");
        index.put("name", "marko", a);
        Iterator itty = index.query("name", "*rko").iterator();
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

    public void testHaGraph() throws Exception {
        assertTrue(InternalAbstractGraphDatabase.class.isAssignableFrom(HighlyAvailableGraphDatabase.class));

        /*String directory = this.getWorkingDirectory();
        Neo4jHaGraph graph = new Neo4jHaGraph(directory);
        graph.shutdown();
        deleteDirectory(new File(directory));*/
    }

    public void testRollbackExceptionOnBeforeTxCommit() throws Exception {
        Neo4jGraph graph = (Neo4jGraph) graphTest.generateGraph();
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
        assertTrue(!GlobalGraphOperations.at(rawGraph).getAllNodes().iterator().hasNext());
        graph.shutdown();
    }
}