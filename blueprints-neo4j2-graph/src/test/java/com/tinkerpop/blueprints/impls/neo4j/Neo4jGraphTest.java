package com.tinkerpop.blueprints.impls.neo4j;

import com.tinkerpop.blueprints.EdgeTestSuite;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQueryTestSuite;
import com.tinkerpop.blueprints.GraphTestSuite;
import com.tinkerpop.blueprints.IndexTestSuite;
import com.tinkerpop.blueprints.IndexableGraphTestSuite;
import com.tinkerpop.blueprints.KeyIndexableGraphTestSuite;
import com.tinkerpop.blueprints.TestSuite;
import com.tinkerpop.blueprints.TransactionalGraphTestSuite;
import com.tinkerpop.blueprints.VertexQueryTestSuite;
import com.tinkerpop.blueprints.VertexTestSuite;
import com.tinkerpop.blueprints.impls.GraphTest;
import com.tinkerpop.blueprints.util.io.gml.GMLReaderTestSuite;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReaderTestSuite;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONReaderTestSuite;
import org.neo4j.graphdb.Transaction;

import java.io.File;
import java.lang.reflect.Method;
import java.util.*;

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

    public void testNeo4jGraphSpecificTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new Neo4jGraphSpecificTestSuite(this));
        printTestPerformance("Neo4jGraphSpecificTestSuite", this.stopWatch());
    }

    public Graph generateGraph() {
        return generateGraph("graph");
    }

    public Graph generateGraph(final String graphDirectoryName) {
        final String directory = getWorkingDirectory();
        Neo4jGraph graph = new Neo4jTestGraph(directory + "/" + graphDirectoryName);
        graph.setCheckElementsInTransaction(true);

        // for clean shutdown later
        testGraph.set(graph);

        return graph;
    }

    private final static ThreadLocal<Graph> testGraph = new ThreadLocal<Graph>();

    public void doTestSuite(final TestSuite testSuite) throws Exception {
        String directory = this.getWorkingDirectory();
        deleteDirectory(new File(directory));
        int total=0;
        Map<Method,Exception> failures=new LinkedHashMap<Method,Exception>();
        for (Method method : testSuite.getClass().getDeclaredMethods()) {
            if (method.getName().startsWith("test")) {
                System.out.println("Testing " + method.getName() + "...");
                try {
                    total++;
                    method.invoke(testSuite);
                } catch(Exception e) {
                    // report all errors not just the first
                    failures.put(method,e);
                    System.out.println("Error during "+method.getName()+" "+e.getMessage());
                    e.printStackTrace();
                } finally {
                    // clean shutdown w/o leaking resources even in case of AssertionErrors
                    if (testGraph.get()!=null) {
                        testGraph.get().shutdown();
                    }
                    deleteDirectory(new File(directory));
                }
            }
        }
        // fail the suite
        if (!failures.isEmpty()) {
            throw new AssertionError(testSuite.getName()+" failed, total "+ total+ " failures: "+failures.size()+"\n"+
            failures.keySet());
        }
    }

    private String getWorkingDirectory() {
        return this.computeTestDataRoot().getAbsolutePath();
    }

    private static class Neo4jTestGraph extends Neo4jGraph {
        private final static ThreadLocal<Transaction> outerTx = new ThreadLocal<Transaction>();
        private Collection<Transaction> outerTransactions;
        boolean shuttingDown = false;

        public Neo4jTestGraph(String path) {
            super(path);
        }

        @Override
        protected void init() {
            outerTransactions = new HashSet<Transaction>();
            restartTx();
            super.init();
        }

        private void restartTx() {
            Transaction tx = getRawGraph().beginTx();
            outerTx.set(tx);
            outerTransactions.add(tx);
        }

        @Override
        public void shutdown() {
            shuttingDown = true;
            finishOuter(true,false);
            closeOpenTransactions();
            super.shutdown();
            testGraph.remove();
        }

        private void closeOpenTransactions() {
            Iterator<Transaction> it = outerTransactions.iterator();
            while (it.hasNext()) {
                Transaction tx = it.next();
                try {
                    tx.failure();
                } catch(Exception e) {
//                    System.out.println("Error cleaning up outer transaction " + e.getMessage());
                } finally {
                    try {
                        tx.close();
                    } catch(Exception e) {
//                        System.out.println("Error cleaning up outer transaction " + e.getMessage());
                    }
                }
                it.remove();
            }
        }

        private void finishOuter(boolean success, boolean restart) {
            if (outerTx.get() != null) {
                Transaction tx = outerTx.get();
                try {
                    if (success) tx.success();
                    else tx.failure();
                } catch (Exception e) {
//                    System.out.println("Error " + (success ? "succeeding" : "failing") + " outer transaction " + e.getMessage());
                } finally {
                    try {
                        tx.close();
                    } catch (Exception e) {
//                        System.out.println("Error committing " + (success ? "successful" : "failing") + " outer transaction " + e.getMessage());
                    }
                    outerTransactions.remove(tx);
                    outerTx.remove();
                }
            }
            if (restart && !shuttingDown) {
                restartTx();
            }
        }

        @Override
        public void commit() {
            super.commit();
            finishOuter(true,true);
        }

        @Override
        public void rollback() {
            super.rollback();
            finishOuter(false,true);
        }
    }
}
