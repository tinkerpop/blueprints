package com.tinkerpop.blueprints.pgm.impls.neo4j;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.TestSuite;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.GraphTest;
import com.tinkerpop.blueprints.pgm.util.graphml.GraphMLReader;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jBenchmarkTestSuite extends TestSuite {

    private static final int TOTAL_RUNS = 10;

    public Neo4jBenchmarkTestSuite() {
    }

    public Neo4jBenchmarkTestSuite(final GraphTest graphTest) {
        super(graphTest);
    }

    public void testNeo4jRaw() throws Exception {
        double totalTime = 0.0d;
        Graph graph = graphTest.getGraphInstance();
        GraphMLReader.inputGraph(graph, GraphMLReader.class.getResourceAsStream("graph-example-2.xml"));
        graph.shutdown();

        for (int i = 0; i < TOTAL_RUNS; i++) {
            graph = graphTest.getGraphInstance();
            GraphDatabaseService neo4j = ((Neo4jGraph) graph).getRawGraph();
            int counter = 0;
            this.stopWatch();
            for (Node node : neo4j.getAllNodes()) {
                counter++;
                for (Relationship relationship : node.getRelationships(Direction.OUTGOING)) {
                    counter++;
                    Node node2 = relationship.getEndNode();
                    counter++;
                    for (Relationship relationship2 : node2.getRelationships(Direction.OUTGOING)) {
                        counter++;
                        Node node3 = relationship2.getEndNode();
                        counter++;
                        for (Relationship relationship3 : node3.getRelationships(Direction.OUTGOING)) {
                            counter++;
                            relationship3.getEndNode();
                            counter++;
                        }
                    }
                }
            }
            double currentTime = this.stopWatch();
            totalTime = totalTime + currentTime;
            BaseTest.printPerformance(neo4j.toString(), counter, "Neo4j raw elements touched", currentTime);
            graph.shutdown();
        }
        BaseTest.printPerformance("Neo4jRaw", 1, "Neo4j Raw experiment average", totalTime / (double) TOTAL_RUNS);
    }

    public void testNeo4jGraph() throws Exception {
        double totalTime = 0.0d;
        Graph graph = graphTest.getGraphInstance();
        GraphMLReader.inputGraph(graph, GraphMLReader.class.getResourceAsStream("graph-example-2.xml"));
        graph.shutdown();

        for (int i = 0; i < TOTAL_RUNS; i++) {
            graph = graphTest.getGraphInstance();
            this.stopWatch();
            int counter = 0;
            for (Vertex vertex : graph.getVertices()) {
                counter++;
                for (Edge edge : vertex.getOutEdges()) {
                    counter++;
                    Vertex vertex2 = edge.getInVertex();
                    counter++;
                    for (Edge edge2 : vertex2.getOutEdges()) {
                        counter++;
                        Vertex vertex3 = edge2.getInVertex();
                        counter++;
                        for (Edge edge3 : vertex3.getOutEdges()) {
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
    }


}
