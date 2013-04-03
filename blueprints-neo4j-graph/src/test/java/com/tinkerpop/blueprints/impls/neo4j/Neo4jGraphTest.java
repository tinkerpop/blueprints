package com.tinkerpop.blueprints.impls.neo4j;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.EdgeTestSuite;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphTestSuite;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.IndexTestSuite;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.IndexableGraphTestSuite;
import com.tinkerpop.blueprints.KeyIndexableGraphTestSuite;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.QueryTestSuite;
import com.tinkerpop.blueprints.TestSuite;
import com.tinkerpop.blueprints.TransactionalGraphTestSuite;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexTestSuite;
import com.tinkerpop.blueprints.impls.GraphTest;
import com.tinkerpop.blueprints.util.io.gml.GMLReaderTestSuite;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReaderTestSuite;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONReaderTestSuite;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.index.impl.lucene.LowerCaseKeywordAnalyzer;
import org.neo4j.kernel.EmbeddedReadOnlyGraphDatabase;
import org.neo4j.kernel.InternalAbstractGraphDatabase;
import org.neo4j.kernel.ha.HighlyAvailableGraphDatabase;
import org.neo4j.tooling.GlobalGraphOperations;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jGraphTest extends GraphTest {

    /*public void testNeo4jBenchmarkTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new Neo4jBenchmarkTestSuite(this));
        printTestPerformance("Neo4jBenchmarkTestSuite", this.stopWatch());
    }*/

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

    public void testQueryTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new QueryTestSuite(this));
        printTestPerformance("QueryTestSuite", this.stopWatch());
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

    public void testTransactionalGraphTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new TransactionalGraphTestSuite(this));
        printTestPerformance("TransactionalGraphTestSuite", this.stopWatch());
    }

    public void testGraphMLReaderTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new GraphMLReaderTestSuite(this));
        printTestPerformance("GraphMLReaderTestSuite", this.stopWatch());
    }

    public void testGraphSONReaderTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new GraphSONReaderTestSuite(this));
        printTestPerformance("GraphSONReaderTestSuite", this.stopWatch());
    }

    public void testGMLReaderTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new GMLReaderTestSuite(this));
        printTestPerformance("GMLReaderTestSuite", this.stopWatch());
    }


    public Graph generateGraph() {
        return generateGraph("graph");
    }

    public Graph generateGraph(final String graphDirectoryName) {
        final String directory = getWorkingDirectory();
        Neo4jGraph graph = new Neo4jGraph(directory + "/" + graphDirectoryName);
        graph.setCheckElementsInTransaction(true);
        return graph;
    }

    public void doTestSuite(final TestSuite testSuite) throws Exception {
        String directory = this.getWorkingDirectory();
        deleteDirectory(new File(directory));
        for (Method method : testSuite.getClass().getDeclaredMethods()) {
            if (method.getName().startsWith("test")) {
                System.out.println("Testing " + method.getName() + "...");
                method.invoke(testSuite);
                deleteDirectory(new File(directory));
            }
        }
    }

    private String getWorkingDirectory() {
        return this.computeTestDataRoot().getAbsolutePath();
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
        String directory = this.getWorkingDirectory();
        deleteDirectory(new File(directory));

        Neo4jGraph graph = new Neo4jGraph(directory);
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
        deleteDirectory(new File(directory));
    }

    public void testIndexParameters() throws Exception {
        String directory = this.getWorkingDirectory();
        deleteDirectory(new File(directory));

        IndexableGraph graph = new Neo4jGraph(directory);
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
        deleteDirectory(new File(this.getWorkingDirectory()));
    }

    public void testReadOnlyGraph() throws Exception {
        String directory = this.getWorkingDirectory();
        deleteDirectory(new File(directory));

        Neo4jGraph graph = new Neo4jGraph(directory);
        assertEquals(graph.getIndexedKeys(Vertex.class).size(), 0);
        assertFalse(graph.getIndexedKeys(Vertex.class).contains("name"));
        graph.createKeyIndex("name", Vertex.class);
        graph.addVertex(null).setProperty("name", "marko");
        graph.addVertex(null).setProperty("name", "matthias");
        assertEquals(graph.getIndexedKeys(Vertex.class).size(), 1);
        assertTrue(graph.getIndexedKeys(Vertex.class).contains("name"));
        graph.shutdown();

        graph = new Neo4jGraph(new EmbeddedReadOnlyGraphDatabase(directory));
        assertEquals(count(graph.getVertices()), 2);
        assertEquals(count(graph.getVertices("name", "marko")), 1);
        assertEquals(count(graph.getVertices("name", "matthias")), 1);
        assertEquals(graph.getIndexedKeys(Vertex.class).size(), 1);
        assertTrue(graph.getIndexedKeys(Vertex.class).contains("name"));
        graph.shutdown();
        deleteDirectory(new File(directory));
    }

    public void testHaGraph() throws Exception {
        assertTrue(InternalAbstractGraphDatabase.class.isAssignableFrom(HighlyAvailableGraphDatabase.class));

        String directory = this.getWorkingDirectory();
        Neo4jHaGraph graph = new Neo4jHaGraph(directory);
        graph.shutdown();
        deleteDirectory(new File(directory));
    }

    public void testRollbackExceptionOnBeforeTxCommit() throws Exception {
        String directory = this.getWorkingDirectory();
        deleteDirectory(new File(directory));

        Neo4jGraph graph = new Neo4jGraph(directory);
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
        deleteDirectory(new File(directory));
    }
}
