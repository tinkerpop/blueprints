package com.tinkerpop.blueprints;

import com.tinkerpop.blueprints.impls.GraphTest;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class IndexTestSuite extends TestSuite {

    public IndexTestSuite() {
    }

    public IndexTestSuite(final GraphTest graphTest) {
        super(graphTest);
    }

    public void testPutGetRemoveVertex() {
        IndexableGraph graph = (IndexableGraph) graphTest.generateGraph();
        if (graph.getFeatures().supportsVertexIndex) {
            this.stopWatch();
            Index<Vertex> index = graph.createIndex("basic", Vertex.class);
            printPerformance(graph.toString(), 1, "manual index created", this.stopWatch());
            Vertex v1 = graph.addVertex(null);
            Vertex v2 = graph.addVertex(null);
            if (graph.getFeatures().supportsVertexIteration)
                assertEquals(count(graph.getVertices()), 2);

            this.stopWatch();
            index.put("dog", "puppy", v1);
            index.put("dog", "mama", v2);
            printPerformance(graph.toString(), 2, "vertices manually index", this.stopWatch());
            assertEquals(v1, index.get("dog", "puppy").iterator().next());
            assertEquals(v2, index.get("dog", "mama").iterator().next());
            assertEquals(1, index.count("dog", "puppy"));

            v1.removeProperty("dog");
            assertEquals(v1, index.get("dog", "puppy").iterator().next());
            assertEquals(v2, index.get("dog", "mama").iterator().next());

            this.stopWatch();
            graph.removeVertex(v1);
            printPerformance(graph.toString(), 1, "vertex removed and automatically removed from index", this.stopWatch());
            assertEquals(count(index.get("dog", "puppy")), 0);
            assertEquals(v2, index.get("dog", "mama").iterator().next());
            if (graph.getFeatures().supportsVertexIteration)
                assertEquals(count(graph.getVertices()), 1);

            v2.setProperty("dog", "mama2");
            assertEquals(v2, index.get("dog", "mama").iterator().next());
            this.stopWatch();
            graph.removeVertex(v2);
            printPerformance(graph.toString(), 1, "vertex removed and automatically removed from index", this.stopWatch());
            assertEquals(count(index.get("dog", "puppy")), 0);
            assertEquals(count(index.get("dog", "mama")), 0);
            if (graph.getFeatures().supportsVertexIteration)
                assertEquals(count(graph.getVertices()), 0);
        }
        graph.shutdown();
    }

    public void testIndexCount() {
        IndexableGraph graph = (IndexableGraph) graphTest.generateGraph();
        if (graph.getFeatures().supportsVertexIndex) {

            Index<Vertex> index = graph.createIndex("basic", Vertex.class);
            for (int i = 0; i < 10; i++) {
                Vertex v = graph.addVertex(null);
                index.put("dog", "puppy", v);
            }
            assertEquals(10, index.count("dog", "puppy"));
            Vertex v = index.get("dog", "puppy").iterator().next();
            graph.removeVertex(v);
            index.remove("dog", "puppy", v);
            assertEquals(9, index.count("dog", "puppy"));

        }
        graph.shutdown();
    }

    public void testPutGetRemoveEdge() {
        IndexableGraph graph = (IndexableGraph) graphTest.generateGraph();
        if (graph.getFeatures().supportsEdgeIndex) {
            this.stopWatch();
            Index<Edge> index = graph.createIndex("basic", Edge.class);
            printPerformance(graph.toString(), 1, "manual index created", this.stopWatch());
            Vertex v1 = graph.addVertex(null);
            Vertex v2 = graph.addVertex(null);
            Edge e1 = graph.addEdge(null, v1, v2, graphTest.convertLabel("test1"));
            Edge e2 = graph.addEdge(null, v1, v2, graphTest.convertLabel("test2"));
            if (graph.getFeatures().supportsEdgeIteration)
                assertEquals(count(graph.getEdges()), 2);

            this.stopWatch();
            index.put("dog", "puppy", e1);
            index.put("dog", "mama", e2);
            printPerformance(graph.toString(), 2, "edges manually index", this.stopWatch());
            assertEquals(e1, index.get("dog", "puppy").iterator().next());
            assertEquals(e2, index.get("dog", "mama").iterator().next());

            v1.removeProperty("dog");
            assertEquals(e1, index.get("dog", "puppy").iterator().next());
            assertEquals(e2, index.get("dog", "mama").iterator().next());

            this.stopWatch();
            graph.removeEdge(e1);
            printPerformance(graph.toString(), 1, "edge removed and automatically removed from index", this.stopWatch());
            assertEquals(count(index.get("dog", "puppy")), 0);
            assertEquals(e2, index.get("dog", "mama").iterator().next());
            if (graph.getFeatures().supportsEdgeIteration)
                assertEquals(count(graph.getEdges()), 1);

            v2.setProperty("dog", "mama2");
            assertEquals(e2, index.get("dog", "mama").iterator().next());
            this.stopWatch();
            graph.removeEdge(e2);
            printPerformance(graph.toString(), 1, "edge removed and automatically removed from index", this.stopWatch());
            assertEquals(count(index.get("dog", "puppy")), 0);
            assertEquals(count(index.get("dog", "mama")), 0);
            if (graph.getFeatures().supportsEdgeIteration)
                assertEquals(count(graph.getEdges()), 0);
        }
        graph.shutdown();
    }

    public void testCloseableSequence() {
        IndexableGraph graph = (IndexableGraph) graphTest.generateGraph();
        if (graph.getFeatures().supportsVertexIndex) {

            Index<Vertex> index = graph.createIndex("basic", Vertex.class);
            for (int i = 0; i < 10; i++) {
                Vertex v = graph.addVertex(null);
                index.put("dog", "puppy", v);
            }
            CloseableIterable<Vertex> hits = index.get("dog", "puppy");
            int counter = 0;
            for (Vertex v : hits) {
                counter++;

            }
            assertEquals(counter, 10);
            hits.close(); // no exception should be thrown

        }
        graph.shutdown();
    }
}
