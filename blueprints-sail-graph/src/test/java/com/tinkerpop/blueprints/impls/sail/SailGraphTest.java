package com.tinkerpop.blueprints.impls.sail;

import com.tinkerpop.blueprints.EdgeTestSuite;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQueryTestSuite;
import com.tinkerpop.blueprints.GraphTestSuite;
import com.tinkerpop.blueprints.TestSuite;
import com.tinkerpop.blueprints.TransactionalGraphTestSuite;
import com.tinkerpop.blueprints.VertexQueryTestSuite;
import com.tinkerpop.blueprints.VertexTestSuite;
import com.tinkerpop.blueprints.impls.GraphTest;
import org.openrdf.sail.memory.MemoryStore;

import java.lang.reflect.Method;


/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class SailGraphTest extends GraphTest {

    public void testVertexTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new VertexTestSuite(this));
        printTestPerformance("VertexTestSuite", this.stopWatch());
    }

    public void testEdgeSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new EdgeTestSuite(this));
        printTestPerformance("EdgeTestSuite", this.stopWatch());
    }

    public void testGraphSuite() throws Exception {
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

    public void testTransactionalGraphTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new TransactionalGraphTestSuite(this));
        printTestPerformance("TransactionalGraphTestSuite", this.stopWatch());
    }

    public void testSailGraphSpecificTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new SailGraphSpecificTestSuite(this));
        printTestPerformance("SailGraphSpecificTestSuite", this.stopWatch());
    }

    public Graph generateGraph() {
        return new SailGraph(new MemoryStore());
    }

    public Graph generateGraph(final String graphDirectoryName) {
        return new SailGraph(new MemoryStore());
    }


    public void doTestSuite(final TestSuite testSuite) throws Exception {
        for (Method method : testSuite.getClass().getDeclaredMethods()) {
            if (method.getName().startsWith("test")) {
                System.out.println("Testing " + method.getName() + "...");
                Graph graph = this.generateGraph();
                method.invoke(testSuite);
                graph.shutdown();
            }
        }
    }

    public Object convertId(final Object id) {
        return "urn:com.tinkerpop.blueprints:" + id;
    }

    public String convertLabel(final String label) {
        return "urn:com.tinkerpop.blueprints:" + label;
    }
}
