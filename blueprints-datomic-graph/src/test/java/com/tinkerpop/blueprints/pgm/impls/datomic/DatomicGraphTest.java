package com.tinkerpop.blueprints.pgm.impls.datomic;

import com.tinkerpop.blueprints.pgm.*;
import com.tinkerpop.blueprints.pgm.impls.GraphTest;
import com.tinkerpop.blueprints.pgm.util.io.graphml.GraphMLReaderTestSuite;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * @author Davy Suvee (http://datablend.be)
 */
public class DatomicGraphTest extends GraphTest {

    public DatomicGraphTest() {
        this.allowsDuplicateEdges = true;
        this.allowsSelfLoops = true;
        this.isPersistent = false;
        this.isRDFModel = false;
        this.supportsVertexIteration = true;
        this.supportsEdgeIteration = true;
        this.supportsVertexIndex = true;
        this.supportsEdgeIndex = true;
        this.ignoresSuppliedIds = true;
        this.supportsTransactions = false;

        this.allowSerializableObjectProperty = false;
        this.allowBooleanProperty = true;
        this.allowDoubleProperty = true;
        this.allowFloatProperty = true;
        this.allowIntegerProperty = true;
        this.allowPrimitiveArrayProperty = false;
        this.allowUniformListProperty = false;
        this.allowMixedListProperty = false;
        this.allowLongProperty = true;
        this.allowMapProperty = false;
        this.allowStringProperty = true;

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

    //public void testIndexableGraphTestSuite() throws Exception {
    //    this.stopWatch();
    //    doTestSuite(new IndexableGraphTestSuite(this));
    //    printTestPerformance("IndexableGraphTestSuite", this.stopWatch());
    //}

    //public void testIndexTestSuite() throws Exception {
    //    this.stopWatch();
    //    doTestSuite(new IndexTestSuite(this));
    //    printTestPerformance("IndexTestSuite", this.stopWatch());
    //}

    public void testAutomaticIndexTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new AutomaticIndexTestSuite(this));
        printTestPerformance("AutomaticIndexTestSuite", this.stopWatch());
    }

    //public void testTransactionalGraphTestSuite() throws Exception {
    //    this.stopWatch();
    //    doTestSuite(new TransactionalGraphTestSuite(this));
    //    printTestPerformance("TransactionalGraphTestSuite", this.stopWatch());
    //}

    public void testGraphMLReaderTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new GraphMLReaderTestSuite(this));
        printTestPerformance("GraphMLReaderTestSuite", this.stopWatch());
    }

    public Graph getGraphInstance() {
        return new DatomicGraph("datomic:mem://tinkerpop" + UUID.randomUUID());
    }

    public void doTestSuite(final TestSuite testSuite) throws Exception {
        for (Method method : testSuite.getClass().getDeclaredMethods()) {
            if (method.getName().startsWith("test")) {
                System.out.println("Testing " + method.getName() + "...");
                method.invoke(testSuite);
            }
        }
    }

}
