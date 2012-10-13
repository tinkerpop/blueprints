package com.tinkerpop.blueprints.impls.oraclekv;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.EdgeTestSuite;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.MetaGraph;
import com.tinkerpop.blueprints.GraphTestSuite;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.IndexTestSuite;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.IndexableGraphTestSuite;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.KeyIndexableGraphTestSuite;
import com.tinkerpop.blueprints.QueryTestSuite;
import com.tinkerpop.blueprints.TestSuite;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexTestSuite;
import com.tinkerpop.blueprints.impls.GraphTest;
import junit.framework.Assert;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Dan McClary
 */
public class KVGraphTest extends GraphTest {

	private String graphName = null;
	private String storeName = null;
	private String hostName = null;
	private int hostPort = 5000;
	
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


    public void testEncoding() throws Exception {
        final String doTest = System.getProperty("testKVGraph", "true");
        this.graphName = System.getProperty("graphName");
        this.storeName = System.getProperty("storeName");
        this.hostName = System.getProperty("hostname");
        this.hostPort = new Integer(System.getProperty("hostPort")).intValue();
        
        if (doTest.equals("true")) {
            final Graph g = generateGraph();
            this.resetGraph();

            final Vertex v = g.addVertex(null);
            v.setProperty("test", "déja-vu");

            Assert.assertEquals("déja-vu", g.getVertex(v.getId()).getProperty("test"));
        }
    }

    public void testOuterParens() throws Exception {
        this.graphName = System.getProperty("graphName");
        this.storeName = System.getProperty("storeName");
        this.hostName = System.getProperty("hostname");
        this.hostPort = new Integer(System.getProperty("hostPort")).intValue();
        final String doTest = System.getProperty("testKVGraph", "true");
        if (doTest.equals("true")) {
            final Graph g = generateGraph();
            this.resetGraph();

            final Vertex v = g.addVertex(null);
            v.setProperty("test", "(sometext)");

            Assert.assertEquals("(sometext)", g.getVertex(v.getId()).getProperty("test"));
        }
    }

    /*
    TODO: Create a respective test case that doesn't require underscore prefixed properties
    public void testGraphMLReaderTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new GraphMLReaderTestSuite(this));
        printTestPerformance("GraphMLReaderTestSuite", this.stopWatch());
    }*/


    public Graph generateGraph() {
        return new KVGraph(this.graphName, this.storeName, this.hostName, this.hostPort);
    }

    public Graph generateGraph(final String graphDirectoryName) {
    	return new KVGraph(graphDirectoryName, this.storeName, this.hostName, this.hostPort);
    }

    public void doTestSuite(final TestSuite testSuite) throws Exception {
            // "http://127.0.0.1:8182/graphs/emptygraph"
            String doTest = System.getProperty("testKVGraph", "true");
            if (doTest.equals("true")) {

                this.graphName = System.getProperty("graphName");
                this.storeName = System.getProperty("storeName");
                this.hostName = System.getProperty("hostname");
                this.hostPort = new Integer(System.getProperty("hostPort")).intValue();
            	
                this.resetGraph();
                for (Method method : testSuite.getClass().getDeclaredMethods()) {
                    if (method.getName().startsWith("test")) {
                        System.out.println("Testing " + method.getName() + "...");
                        this.resetGraph();
                        method.invoke(testSuite);
                        this.resetGraph();
                    }
                }
            }
        
    }

    private void resetGraph() {
        final MetaGraph graph = (MetaGraph) this.generateGraph();

        // since we don't have graph.clear() anymore we manually reset the graph.
        Iterator<Vertex> vertexItty = graph.getVertices().iterator();
        List<Vertex> verticesToRemove = new ArrayList<Vertex>();
        while (vertexItty.hasNext()) {
            verticesToRemove.add(vertexItty.next());
        }

        for (Vertex vertexToRemove : verticesToRemove) {
            graph.removeVertex(vertexToRemove);
        }

        // for (String key : graph.getIndexedKeys(Vertex.class)) {
        //             graph.dropKeyIndex(key, Vertex.class);
        //         }

        // for (String key : graph.getIndexedKeys(Edge.class)) {
        //     graph.dropKeyIndex(key, Edge.class);
        // }
        // 
        // for (Index idx : idxGraph.getIndices()) {
        //     idxGraph.dropIndex(idx.getIndexName());
        // }
    }


}