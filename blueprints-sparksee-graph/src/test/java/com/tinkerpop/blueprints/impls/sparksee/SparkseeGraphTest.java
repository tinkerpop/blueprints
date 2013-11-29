package com.tinkerpop.blueprints.impls.sparksee;

import com.tinkerpop.blueprints.EdgeTestSuite;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQueryTestSuite;
import com.tinkerpop.blueprints.GraphTestSuite;
import com.tinkerpop.blueprints.TestSuite;
import com.tinkerpop.blueprints.VertexQueryTestSuite;
import com.tinkerpop.blueprints.VertexTestSuite;
import com.tinkerpop.blueprints.impls.GraphTest;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReaderTestSuite;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONReaderTestSuite;

import java.lang.reflect.Method;

/**
 * @author <a href="http://www.sparsity-technologies.com">Sparsity
 *         Technologies</a>
 */
public class SparkseeGraphTest extends GraphTest {

    /*public void testSparkseeBenchmarkTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new SparkseeBenchmarkTestSuite(this));
        printTestPerformance("SparkseeBenchmarkTestSuite", this.stopWatch());
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

    /*
    This test does not work because Sparksee properties are restricted to
    the scope of a node/edge type. Thus, when using the KeyIndexableGraph
    APIs it is required to previously set the label where the key property
    is defined, as it is shown in the testKeyIndex below.
    
    public void testKeyIndexableGraphTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new KeyIndexableGraphTestSuite(this));
        printTestPerformance("KeyIndexableGraphTestSuite", this.stopWatch());
    }
    //*/

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

    /*
    the GML Reader won't work with Sparksee because of our test approach.  the test uses the toy
    tinkergraph which has a mix of data types for the "weight" property on the edge...dex does
    not allow an attribute with the same name to have values with different data types so it
    blows up the test.
    public void testGMLReaderTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new GMLReaderTestSuite(this));
        printTestPerformance("GMLReaderTestSuite", this.stopWatch());
    }
    */

    //
    // Sparksee specific test
    //
    public void testSparkseeSpecificTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new SparkseeGraphSpecificTestSuite(this));
        printTestPerformance("SparkseeGraphSpecificTestSuite", this.stopWatch());
    }

    public Graph generateGraph() {
        return generateGraph(false);
    }

    public Graph generateGraph(boolean create) {
        return this.generateGraph(create, "blueprints_sparksee_test.gdb");
    }

    public Graph generateGraph(final String graphDirectoryName) {
        return this.generateGraph(false, graphDirectoryName);
    }

    public Graph generateGraph(boolean create, final String graphDirectoryName) {
        String db = this.computeTestDataRoot() + "/" + graphDirectoryName;

        if (create) {
            deleteDirectory(this.computeTestDataRoot());
        }

        return new SparkseeGraph(db, "./blueprints-sparksee.cfg");
    }

    public void doTestSuite(final TestSuite testSuite) throws Exception {
        deleteDirectory(this.computeTestDataRoot());
        for (Method method : testSuite.getClass().getDeclaredMethods()) {
            if (method.getName().startsWith("test")) {
                System.out.println("Testing " + method.getName() + "...");
                method.invoke(testSuite);
                deleteDirectory(this.computeTestDataRoot());
            }
        }
    }
}
