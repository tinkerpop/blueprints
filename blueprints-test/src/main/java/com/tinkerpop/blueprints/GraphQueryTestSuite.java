package com.tinkerpop.blueprints;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tinkerpop.blueprints.impls.GraphTest;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class GraphQueryTestSuite extends TestSuite {

    public GraphQueryTestSuite() {
    }

    public GraphQueryTestSuite(final GraphTest graphTest) {
        super(graphTest);
    }

    public void testGraphQueryForVertices() {
        Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsVertexIndex && graph instanceof KeyIndexableGraph) {
            ((KeyIndexableGraph) graph).createKeyIndex("name", Vertex.class);
        }
        if (graph.getFeatures().supportsVertexProperties) {
            Vertex vertex = graph.addVertex(null);
            vertex.setProperty("name", "marko");
            vertex.setProperty("age", 33);
            vertex = graph.addVertex(null);
            vertex.setProperty("name", "matthias");
            vertex.setProperty("age", 28);
            graph.addVertex(null);

            Iterable<Vertex> vertices = graph.query().vertices();
            assertEquals(count(vertices), 3);
            assertEquals(count(vertices), 3);
            Set<String> names = new HashSet<String>();
            for (Vertex v : vertices) {
                names.add((String) v.getProperty("name"));
            }
            assertEquals(names.size(), 3);
            assertTrue(names.contains("marko"));
            assertTrue(names.contains(null));
            assertTrue(names.contains("matthias"));

            assertEquals(count(graph.query().limit(0).vertices()), 0);
            assertEquals(count(graph.query().limit(1).vertices()), 1);
            assertEquals(count(graph.query().limit(2).vertices()), 2);
            assertEquals(count(graph.query().limit(3).vertices()), 3);
            assertEquals(count(graph.query().limit(4).vertices()), 3);

            vertices = graph.query().has("name", "marko").vertices();
            assertEquals(count(vertices), 1);
            // TODO: Neo4j's global iterators are inconsistent with its transactions
            // assertEquals(vertices.iterator().next().getProperty("name"), "marko");

            vertices = graph.query().has("age", Compare.GREATER_THAN_EQUAL, 29).vertices();
            assertEquals(count(vertices), 1);
            assertEquals(vertices.iterator().next().getProperty("name"), "marko");
            assertEquals(vertices.iterator().next().getProperty("age"), 33);

            vertices = graph.query().has("age", Compare.GREATER_THAN_EQUAL, 28).vertices();
            assertEquals(count(vertices), 2);
            names = new HashSet<String>();
            for (Vertex v : vertices) {
                names.add((String) v.getProperty("name"));
            }
            assertEquals(names.size(), 2);
            assertTrue(names.contains("marko"));
            assertTrue(names.contains("matthias"));

            vertices = graph.query().interval("age", 28, 33).vertices();
            assertEquals(count(vertices), 1);
            assertEquals(vertices.iterator().next().getProperty("name"), "matthias");

            assertEquals(count(graph.query().hasNot("age").vertices()), 1);
            assertEquals(count(graph.query().has("age", 28).has("name", "matthias").vertices()), 1);
            assertEquals(count(graph.query().has("age", 28).has("name", "matthias").has("name", "matthias").vertices()), 1);
            assertEquals(count(graph.query().interval("age", 28, 32).has("name", "marko").vertices()), 0);
        }
        graph.shutdown();
    }

    public void testGraphQueryForEdges() {
        Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsEdgeIndex && graph instanceof KeyIndexableGraph) {
            ((KeyIndexableGraph) graph).createKeyIndex("type", Edge.class);
        }
        if (graph.getFeatures().supportsEdgeProperties && graph.getFeatures().supportsVertexProperties) {
            Vertex marko = graph.addVertex(null);
            marko.setProperty("name", "marko");
            Vertex matthias = graph.addVertex(null);
            matthias.setProperty("name", "matthias");
            Vertex stephen = graph.addVertex(null);
            stephen.setProperty("name", "stephen");

            Edge edge = marko.addEdge("knows", stephen);
            edge.setProperty("type", "tinkerpop");
            edge.setProperty("weight", 1.0);
            edge = marko.addEdge("knows", matthias);
            edge.setProperty("type", "aurelius");

            assertEquals(count(graph.query().edges()), 2);
            assertEquals(count(graph.query().limit(0).edges()), 0);
            assertEquals(count(graph.query().limit(1).edges()), 1);
            assertEquals(count(graph.query().limit(2).edges()), 2);
            assertEquals(count(graph.query().limit(3).edges()), 2);

            assertEquals(count(graph.query().has("type", "tinkerpop").has("type", "tinkerpop").edges()), 1);
            assertEquals(graph.query().has("type", "tinkerpop").edges().iterator().next().getProperty("weight"), 1.0);
            assertEquals(count(graph.query().has("type", "aurelius").edges()), 1);
            assertEquals(graph.query().has("type", "aurelius").edges().iterator().next().getPropertyKeys().size(), 1);
            assertEquals(count(graph.query().hasNot("weight").edges()), 1);
            assertEquals(graph.query().hasNot("weight").edges().iterator().next().getProperty("type"), "aurelius");

            assertEquals(count(graph.query().has("weight", 1.0).edges()), 1);
            assertEquals(graph.query().has("weight", 1.0).edges().iterator().next().getProperty("type"), "tinkerpop");
            assertEquals(count(graph.query().has("weight", 1.0).has("type", "tinkerpop").edges()), 1);
            assertEquals(graph.query().has("weight", 1.0).has("type", "tinkerpop").edges().iterator().next().getProperty("type"), "tinkerpop");
            assertEquals(count(graph.query().has("weight", 1.0).has("type", "aurelius").edges()), 0);

            assertEquals(graph.query().interval("weight", 0.0, 1.1).edges().iterator().next().getProperty("type"), "tinkerpop");
            assertEquals(count(graph.query().interval("weight", 0.0, 1.0).edges()), 0);
        }
        graph.shutdown();
    }

    public void testGraphQueryForHasOR() {
        Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsEdgeIndex && graph instanceof KeyIndexableGraph) {
            ((KeyIndexableGraph) graph).createKeyIndex("type", Edge.class);
        }
        if (graph.getFeatures().supportsEdgeIteration  && graph.getFeatures().supportsEdgeProperties && graph.getFeatures().supportsVertexProperties) {
            Vertex marko = graph.addVertex(null);
            marko.setProperty("name", "marko");
            Vertex matthias = graph.addVertex(null);
            matthias.setProperty("name", "matthias");
            Vertex stephen = graph.addVertex(null);
            stephen.setProperty("name", "stephen");

            Edge edge = marko.addEdge("knows", stephen);
            edge.setProperty("type", "tinkerpop");
            edge.setProperty("weight", 1.0);
            edge = marko.addEdge("knows", matthias);
            edge.setProperty("type", "aurelius");

            assertEquals(count(graph.query().has("type", Contains.IN, Arrays.asList("tinkerpop", "aurelius")).edges()), 2);
            assertEquals(count(graph.query().has("type", Contains.IN, Arrays.asList("tinkerpop", "aurelius")).has("type", "tinkerpop").edges()), 1);
            assertEquals(count(graph.query().has("type", Contains.IN, Arrays.asList("tinkerpop", "aurelius")).has("type", "tinkerpop").has("type", "aurelius").edges()), 0);
            assertEquals(graph.query().has("weight").edges().iterator().next().getProperty("type"), "tinkerpop");
            assertEquals(graph.query().has("weight").edges().iterator().next().getProperty("weight"), 1.0);
            assertEquals(graph.query().hasNot("weight").edges().iterator().next().getProperty("type"), "aurelius");
            assertNull(graph.query().hasNot("weight").edges().iterator().next().getProperty("weight"));

            List result = asList(graph.query().has("name", Contains.IN, Arrays.asList("marko", "stephen")).vertices());
            assertEquals(result.size(), 2);
            assertTrue(result.contains(marko));
            assertTrue(result.contains(stephen));
            result = asList(graph.query().has("name", Contains.IN, Arrays.asList("marko", "stephen", "matthias", "josh", "peter")).vertices());
            assertEquals(result.size(), 3);
            assertTrue(result.contains(marko));
            assertTrue(result.contains(stephen));
            assertTrue(result.contains(matthias));
            result = asList(graph.query().has("name").vertices());
            assertEquals(result.size(), 3);
            assertTrue(result.contains(marko));
            assertTrue(result.contains(stephen));
            assertTrue(result.contains(matthias));
            result = asList(graph.query().hasNot("name").vertices());
            assertEquals(result.size(), 0);
            result = asList(graph.query().hasNot("blah").vertices());
            assertEquals(result.size(), 3);
            assertTrue(result.contains(marko));
            assertTrue(result.contains(stephen));
            assertTrue(result.contains(matthias));
            result = asList(graph.query().has("name", Contains.NOT_IN, Arrays.asList("bill", "sam")).vertices());
            assertEquals(result.size(), 3);
            assertTrue(result.contains(marko));
            assertTrue(result.contains(stephen));
            assertTrue(result.contains(matthias));
            result = asList(graph.query().has("name", Contains.IN, Arrays.asList("bill", "matthias", "stephen", "marko")).vertices());
            assertEquals(result.size(), 3);
            assertTrue(result.contains(marko));
            assertTrue(result.contains(stephen));
            assertTrue(result.contains(matthias));
        }
        graph.shutdown();
    }
}