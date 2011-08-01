package com.tinkerpop.blueprints.pgm.impls.dex;

import com.tinkerpop.blueprints.pgm.EdgeTestSuite;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.GraphTestSuite;
import com.tinkerpop.blueprints.pgm.TestSuite;
import com.tinkerpop.blueprints.pgm.VertexTestSuite;
import com.tinkerpop.blueprints.pgm.impls.GraphTest;
import com.tinkerpop.blueprints.pgm.util.graphml.GraphMLReaderTestSuite;

import java.io.File;
import java.lang.reflect.Method;

/**
 * @author <a href="http://www.sparsity-technologies.com">Sparsity
 *         Technologies</a>
 */
public class DexGraphTest extends GraphTest {

    public DexGraphTest() {
        this.allowsDuplicateEdges = true;
        this.allowsSelfLoops = true;
        this.isPersistent = true;
        this.isRDFModel = false;
        this.supportsVertexIteration = true;
        this.supportsEdgeIteration = true;
        this.supportsVertexIndex = false;
        this.supportsEdgeIndex = false;
        this.ignoresSuppliedIds = true;
        this.supportsTransactions = false;
    }

    /*public void testDexBenchmarkTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new DexBenchmarkTestSuite(this));
        printTestPerformance("DexBenchmarkTestSuite", this.stopWatch());
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

    public void testGraphMLReaderTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new GraphMLReaderTestSuite(this));
        printTestPerformance("GraphMLReaderTestSuite", this.stopWatch());
    }

    /*public void testIndexableGraphTestSuite() throws Exception {
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

    public void testTransactionalGraphTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new TransactionalGraphTestSuite(this));
        printTestPerformance("TransactionalGraphTestSuite", this.stopWatch());
    }*/

    public Graph getGraphInstance() {
        String db = System.getProperty("dexGraphFile");
        if (db == null)
            db = "/tmp/blueprints_test.dex";
        return new DexGraph(db);
    }

    public void doTestSuite(final TestSuite testSuite) throws Exception {
        String doTest = System.getProperty("testDexGraph");
        if (doTest == null || doTest.equals("true")) {
            String db = System.getProperty("dexGraphFile");
            if (db == null)
                db = "/tmp/blueprints_test.dex";
            File fDB = new File(db);
            fDB.delete();
            for (Method method : testSuite.getClass().getDeclaredMethods()) {
                if (method.getName().startsWith("test")) {
                    System.out.println("Testing " + method.getName() + "...");
                    method.invoke(testSuite);
                    fDB.delete();
                }
            }
        }
    }
}
