package com.tinkerpop.blueprints.pgm.pipes;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraphFactory;
import junit.framework.TestCase;

import java.util.Arrays;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class PropertyFilterPipeTest extends TestCase {

    public void testPropertyFilter() {
        Graph graph = TinkerGraphFactory.createTinkerGraph();
        Vertex marko = graph.getVertex("1");
        Pipe<Vertex, Edge> pipe1 = new VertexEdgePipe(VertexEdgePipe.Step.OUT_EDGES);
        Pipe<Edge, Vertex> pipe2 = new EdgeVertexPipe(EdgeVertexPipe.Step.IN_VERTEX);
        Pipe pipe3 = new PropertyFilterPipe<Vertex, String>("lang", Arrays.asList("java"), false);
        Pipeline<Vertex, Vertex> pipeline = new Pipeline<Vertex, Vertex>(Arrays.asList(pipe1, pipe2, pipe3));
        pipeline.setStarts(Arrays.asList(marko).iterator());
        assertTrue(pipeline.hasNext());
        int counter = 0;
        while (pipeline.hasNext()) {
            counter++;
            Vertex vertex = pipeline.next();
            assertEquals(vertex.getId(), "3");
            assertEquals(vertex.getProperty("lang"), "java");
            assertEquals(vertex.getProperty("name"), "lop");
        }
        assertEquals(counter, 1);

    }

    public void testPropertyFilter2() {
        Graph graph = TinkerGraphFactory.createTinkerGraph();
        Vertex marko = graph.getVertex("1");
        Pipe<Vertex, Edge> pipe1 = new VertexEdgePipe(VertexEdgePipe.Step.OUT_EDGES);
        Pipe<Edge, Vertex> pipe2 = new EdgeVertexPipe(EdgeVertexPipe.Step.IN_VERTEX);
        Pipe pipe3 = new PropertyFilterPipe<Vertex, String>("lang", Arrays.asList("java"), true);
        Pipeline<Vertex, Vertex> pipeline = new Pipeline<Vertex, Vertex>(Arrays.asList(pipe1, pipe2, pipe3));
        pipeline.setStarts(Arrays.asList(marko).iterator());
        assertTrue(pipeline.hasNext());
        int counter = 0;
        while (pipeline.hasNext()) {
            counter++;
            Vertex vertex = pipeline.next();
            assertTrue(vertex.getId().equals("2") || vertex.getId().equals("4"));
            assertTrue(vertex.getProperty("name").equals("josh") || vertex.getProperty("name").equals("vadas"));
            assertTrue(vertex.getProperty("age").equals(27) || vertex.getProperty("age").equals(32));
        }
        assertEquals(counter, 2);

    }
}
