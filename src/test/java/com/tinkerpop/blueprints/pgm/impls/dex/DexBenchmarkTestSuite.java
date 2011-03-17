package com.tinkerpop.blueprints.pgm.impls.dex;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.TestSuite;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.GraphTest;
import com.tinkerpop.blueprints.pgm.util.graphml.GraphMLReader;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class DexBenchmarkTestSuite extends TestSuite {

    private static final int TOTAL_RUNS = 10;

    public DexBenchmarkTestSuite() {
    }

    public DexBenchmarkTestSuite(final GraphTest graphTest) {
        super(graphTest);
    }

    /*public void testPlay() throws Exception {
        Graph graph = graphTest.getGraphInstance();
        Edge edge = graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), "created");
        edge.setProperty("weight", 0.4);
        edge = graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), "knows");
        edge.setProperty("weight", 0.1);
        edge = graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), "knows");
        edge.setProperty("weight", 0.2);
        edge = graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), "knows");
        edge.setProperty("weight", 0.8);
        edge = graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), "knows");
        edge.setProperty("weight", 0.43245);
        graph.shutdown();
    }*/

    /*public void testDexGraph() throws Exception {
        double totalTime = 0.0d;
        Graph graph = graphTest.getGraphInstance();
        GraphMLReader.inputGraph(graph, GraphMLReader.class.getResourceAsStream("graph-example-2.xml"));
        graph.shutdown();

        for (int i = 0; i < TOTAL_RUNS; i++) {
            graph = graphTest.getGraphInstance();
            this.stopWatch();
            int counter = 0;
            for (final Vertex vertex : graph.getVertices()) {
                counter++;
                for (final Edge edge : vertex.getOutEdges()) {
                    counter++;
                    final Vertex vertex2 = edge.getInVertex();
                    counter++;
                    for (final Edge edge2 : vertex2.getOutEdges()) {
                        counter++;
                        final Vertex vertex3 = edge2.getInVertex();
                        counter++;
                        for (final Edge edge3 : vertex3.getOutEdges()) {
                            counter++;
                            edge3.getOutVertex();
                            counter++;
                        }
                    }
                }
            }
            double currentTime = this.stopWatch();
            totalTime = totalTime + currentTime;
            BaseTest.printPerformance(graph.toString(), counter, "Neo4jGraph elements touched", currentTime);
            graph.shutdown();
        }
        BaseTest.printPerformance("Neo4jGraph", 1, "Neo4jGraph experiment average", totalTime / (double) TOTAL_RUNS);
    }*/
}
