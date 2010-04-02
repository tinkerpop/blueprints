package com.tinkerpop.blueprints.pgm.pipes.pgms;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraphFactory;
import com.tinkerpop.blueprints.pgm.pipes.Pipe;
import com.tinkerpop.blueprints.pgm.pipes.pgms.EdgeVertexPipe;
import com.tinkerpop.blueprints.pgm.pipes.Pipeline;
import com.tinkerpop.blueprints.pgm.pipes.pgms.PropertyPipe;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class PropertyPipeTest extends TestCase {

    public void testSingleProperty() {
        Graph graph = TinkerGraphFactory.createTinkerGraph();
        Vertex marko = graph.getVertex("1");
        PropertyPipe<Vertex, String> pp = new PropertyPipe<Vertex, String>("name");
        pp.setStarts(Arrays.asList(marko).iterator());
        assertTrue(pp.hasNext());
        int counter = 0;
        while (pp.hasNext()) {
            String name = pp.next();
            assertEquals(name, "marko");
            counter++;
        }
        assertEquals(counter, 1);
        try {
            pp.next();
            assertTrue(false);
        } catch (NoSuchElementException e) {
            assertFalse(false);
        }
    }

    public void testMultiProperty() {
        Graph graph = TinkerGraphFactory.createTinkerGraph();
        Vertex marko = graph.getVertex("1");
        Pipe evp = new EdgeVertexPipe(EdgeVertexPipe.Step.IN_VERTEX);
        Pipe<Vertex, String> pp = new PropertyPipe<Vertex, String>("name");
        Pipeline<Edge, String> pipeline = new Pipeline<Edge, String>(Arrays.asList(evp, pp));
        pipeline.setStarts(marko.getOutEdges().iterator());
        assertTrue(pipeline.hasNext());
        int counter = 0;
        while (pipeline.hasNext()) {
            String name = pipeline.next();
            assertTrue(name.equals("vadas") || name.equals("josh") || name.equals("lop"));
            counter++;
        }
        assertEquals(counter, 3);
        try {
            pipeline.next();
            assertTrue(false);
        } catch (NoSuchElementException e) {
            assertFalse(false);
        }
    }
}