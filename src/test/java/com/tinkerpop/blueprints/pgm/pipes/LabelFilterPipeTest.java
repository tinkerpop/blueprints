package com.tinkerpop.blueprints.pgm.pipes;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraphFactory;
import com.tinkerpop.blueprints.pgm.pipes.LabelFilterPipe;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class LabelFilterPipeTest extends TestCase {

    public void testFilterLabels() {
        Graph graph = TinkerGraphFactory.createTinkerGraph();
        Vertex marko = graph.getVertex("1");
        LabelFilterPipe lfp = new LabelFilterPipe(Arrays.asList("knows"), false);
        lfp.setStarts(marko.getOutEdges().iterator());
        assertTrue(lfp.hasNext());
        int counter = 0;
        while (lfp.hasNext()) {
            Edge e = lfp.next();
            assertEquals(e.getOutVertex(), marko);
            assertTrue(e.getInVertex().getId().equals("2") || e.getInVertex().getId().equals("4"));
            counter++;
        }
        assertEquals(counter, 2);

        lfp = new LabelFilterPipe(Arrays.asList("knows"), true);
        lfp.setStarts(marko.getOutEdges().iterator());
        assertTrue(lfp.hasNext());
        counter = 0;
        while (lfp.hasNext()) {
            Edge e = lfp.next();
            assertEquals(e.getOutVertex(), marko);
            assertTrue(e.getInVertex().getId().equals("3"));
            counter++;
        }
        assertEquals(counter, 1);

        lfp = new LabelFilterPipe(Arrays.asList("knows", "created"), true);
        lfp.setStarts(marko.getOutEdges().iterator());
        assertFalse(lfp.hasNext());

        lfp = new LabelFilterPipe(Arrays.asList("knows", "created"), false);
        lfp.setStarts(marko.getOutEdges().iterator());
        assertTrue(lfp.hasNext());
        counter = 0;
        while (lfp.hasNext()) {
            Edge e = lfp.next();
            assertEquals(e.getOutVertex(), marko);
            counter++;
        }
        assertEquals(counter, 3);
        try {
            lfp.next();
            assertTrue(false);
        } catch (NoSuchElementException e) {
            assertFalse(false);
        }

    }
}
