package com.tinkerpop.blueprints.impls.neo4j2;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.TestSuite;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.GraphTest;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReader;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4j2BenchmarkTestSuite extends TestSuite {

    private static final int TOTAL_RUNS = 10;

    public Neo4j2BenchmarkTestSuite() {
    }

    public Neo4j2BenchmarkTestSuite(final GraphTest graphTest) {
        super(graphTest);
    }

    public void testNeo4jRaw() throws Exception {
        double totalTime = 0.0d;
        Graph graph = graphTest.generateGraph();
        GraphMLReader.inputGraph(graph, GraphMLReader.class.getResourceAsStream("graph-example-2.xml"));
        graph.shutdown();

        for (int i = 0; i < TOTAL_RUNS; i++) {
            graph = graphTest.generateGraph();
            GraphDatabaseService neo4j = ((Neo4j2Graph) graph).getRawGraph();
            int counter = 0;
            this.stopWatch();
            for (final Node node : neo4j.getAllNodes()) {
                counter++;
                for (final Relationship relationship : node.getRelationships(Direction.OUTGOING)) {
                    counter++;
                    final Node node2 = relationship.getEndNode();
                    counter++;
                    for (final Relationship relationship2 : node2.getRelationships(Direction.OUTGOING)) {
                        counter++;
                        final Node node3 = relationship2.getEndNode();
                        counter++;
                        for (final Relationship relationship3 : node3.getRelationships(Direction.OUTGOING)) {
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
        Graph graph = graphTest.generateGraph();
        GraphMLReader.inputGraph(graph, GraphMLReader.class.getResourceAsStream("graph-example-2.xml"));
        graph.shutdown();

        for (int i = 0; i < TOTAL_RUNS; i++) {
            graph = graphTest.generateGraph();
            this.stopWatch();
            int counter = 0;
            for (final Vertex vertex : graph.getVertices()) {
                counter++;
                for (final Edge edge : vertex.getEdges(com.tinkerpop.blueprints.Direction.OUT)) {
                    counter++;
                    final Vertex vertex2 = edge.getVertex(com.tinkerpop.blueprints.Direction.IN);
                    counter++;
                    for (final Edge edge2 : vertex2.getEdges(com.tinkerpop.blueprints.Direction.OUT)) {
                        counter++;
                        final Vertex vertex3 = edge2.getVertex(com.tinkerpop.blueprints.Direction.IN);
                        counter++;
                        for (final Edge edge3 : vertex3.getEdges(com.tinkerpop.blueprints.Direction.OUT)) {
                            counter++;
                            edge3.getVertex(com.tinkerpop.blueprints.Direction.OUT);
                            counter++;
                        }
                    }
                }
            }
            double currentTime = this.stopWatch();
            totalTime = totalTime + currentTime;
            BaseTest.printPerformance(graph.toString(), counter, "Neo4j2Graph elements touched", currentTime);
            graph.shutdown();
        }
        BaseTest.printPerformance("Neo4j2Graph", 1, "Neo4j2Graph experiment average", totalTime / (double) TOTAL_RUNS);
    }


}
