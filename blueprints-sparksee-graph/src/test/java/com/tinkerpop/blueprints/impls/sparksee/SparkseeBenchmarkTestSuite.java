package com.tinkerpop.blueprints.impls.sparksee;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.TestSuite;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.GraphTest;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReader;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class SparkseeBenchmarkTestSuite extends TestSuite {

    private static final int TOTAL_RUNS = 10;

    public SparkseeBenchmarkTestSuite() {
    }

    public SparkseeBenchmarkTestSuite(final GraphTest graphTest) {
        super(graphTest);
    }

    public void testSparkseeGraph() throws Exception {
        double totalTime = 0.0d;
        Graph graph = graphTest.generateGraph();
        GraphMLReader.inputGraph(graph, GraphMLReader.class.getResourceAsStream("graph-example-2.xml"));
        graph.shutdown();

        for (int i = 0; i < TOTAL_RUNS; i++) {
            graph = graphTest.generateGraph();
            this.stopWatch();
            int counter = 0;
            CloseableIterable<Vertex> vv = (CloseableIterable<Vertex>) graph.getVertices();
            for (final Vertex vertex : vv) {
                counter++;
                CloseableIterable<Edge> ee = (CloseableIterable<Edge>) vertex.getEdges(Direction.OUT);
                for (final Edge edge : ee) {
                    counter++;
                    final Vertex vertex2 = edge.getVertex(Direction.IN);
                    counter++;
                    CloseableIterable<Edge> ee2 = (CloseableIterable<Edge>) vertex2.getEdges(Direction.OUT);
                    for (final Edge edge2 : ee2) {
                        counter++;
                        final Vertex vertex3 = edge2.getVertex(Direction.IN);
                        counter++;
                        CloseableIterable<Edge> ee3 = (CloseableIterable<Edge>) vertex3.getEdges(Direction.OUT);
                        for (final Edge edge3 : ee3) {
                            counter++;
                            edge3.getVertex(Direction.OUT);
                            counter++;
                        }
                        ee3.close();
                    }
                    ee2.close();
                }
                ee.close();
            }
            vv.close();
            double currentTime = this.stopWatch();
            totalTime = totalTime + currentTime;
            BaseTest.printPerformance(graph.toString(), counter, "SparkseeGraph elements touched (run=" + i + ")", currentTime);
            graph.shutdown();
        }
        BaseTest.printPerformance("SparkseeGraph", 1, "SparkseeGraph experiment average", totalTime / (double) TOTAL_RUNS);
    }
}
