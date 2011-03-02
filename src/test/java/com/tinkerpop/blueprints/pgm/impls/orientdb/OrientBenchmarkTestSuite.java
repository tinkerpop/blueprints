package com.tinkerpop.blueprints.pgm.impls.orientdb;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.record.impl.ODocument;
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
public class OrientBenchmarkTestSuite extends TestSuite {

    private static final int TOTAL_RUNS = 10;

    public OrientBenchmarkTestSuite() {
    }

    public OrientBenchmarkTestSuite(final GraphTest graphTest) {
        super(graphTest);
    }

    public void testOrientRaw() throws Exception {
        double totalTime = 0.0d;
        OrientGraph graph = (OrientGraph) graphTest.getGraphInstance();
        GraphMLReader.inputGraph(graph, GraphMLReader.class.getResourceAsStream("graph-example-2.xml"));
        graph.shutdown();

        for (int i = 0; i < TOTAL_RUNS; i++) {
            graph = (OrientGraph) graphTest.getGraphInstance();
            OGraphDatabase db = graph.getRawGraph();
            this.stopWatch();
            int counter = 0;
            for (final ODocument vertex : db.browseClass(OGraphDatabase.VERTEX_CLASS_NAME)) {
                counter++;
                for (final Object edge : db.getOutEdges(vertex)) {
                    counter++;
                    final ODocument vertex2 = db.getInVertex((ODocument) edge);
                    counter++;
                    for (final Object edge2 : db.getOutEdges(vertex2)) {
                        counter++;
                        final ODocument vertex3 = db.getInVertex((ODocument) edge2);
                        counter++;
                        for (final Object edge3 : db.getOutEdges(vertex3)) {
                            counter++;
                            db.getOutVertex((ODocument) edge3);
                            counter++;
                        }
                    }
                }
            }
            double currentTime = this.stopWatch();
            totalTime = totalTime + currentTime;
            BaseTest.printPerformance(db.toString(), counter, "Orient raw elements touched", currentTime);
            graph.shutdown();
        }
        BaseTest.printPerformance("OrientRaw", 1, "OrientDB Raw experiment average", totalTime / (double) TOTAL_RUNS);
    }

    public void testOrientGraph() throws Exception {
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
            BaseTest.printPerformance(graph.toString(), counter, "OrientGraph elements touched", currentTime);
            graph.shutdown();
        }
        BaseTest.printPerformance("OrientGraph", 1, "OrientGraph experiment average", totalTime / (double) TOTAL_RUNS);
    }


}

