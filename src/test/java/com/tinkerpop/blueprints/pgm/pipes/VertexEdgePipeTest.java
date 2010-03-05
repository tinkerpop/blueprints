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
public class VertexEdgePipeTest extends TestCase {

    public void testOutGoingFilter() {
        Graph graph = TinkerGraphFactory.createTinkerGraph();
        Vertex marko = graph.getVertex("1");
        VertexEdgePipe vsf = new VertexEdgePipe(VertexEdgePipe.Step.OUT_EDGES);
        vsf.setStarts(Arrays.asList(marko).iterator());
        assertTrue(vsf.hasNext());
        int counter = 0;
        while (vsf.hasNext()) {
            Edge e = vsf.next();
            assertEquals(e.getOutVertex(), marko);
            assertTrue(e.getInVertex().getId().equals("2") || e.getInVertex().getId().equals("3") || e.getInVertex().getId().equals("4"));
            counter++;
        }
        assertEquals(counter, 3);

        Vertex josh = graph.getVertex("4");
        vsf = new VertexEdgePipe(VertexEdgePipe.Step.OUT_EDGES);
        vsf.setStarts(Arrays.asList(josh).iterator());
        assertTrue(vsf.hasNext());
        counter = 0;
        while (vsf.hasNext()) {
            Edge e = vsf.next();
            assertEquals(e.getOutVertex(), josh);
            assertTrue(e.getInVertex().getId().equals("5") || e.getInVertex().getId().equals("3"));
            counter++;
        }
        assertEquals(counter, 2);

        Vertex lop = graph.getVertex("3");
        vsf = new VertexEdgePipe(VertexEdgePipe.Step.OUT_EDGES);
        vsf.setStarts(Arrays.asList(lop).iterator());
        assertFalse(vsf.hasNext());
        counter = 0;
        while (vsf.hasNext()) {
            counter++;
        }
        assertEquals(counter, 0);
    }
}
