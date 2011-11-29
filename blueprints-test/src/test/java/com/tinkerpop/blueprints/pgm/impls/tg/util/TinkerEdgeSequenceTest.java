package com.tinkerpop.blueprints.pgm.impls.tg.util;

import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Peter Karich, info@jetsli.de
 */
public class TinkerEdgeSequenceTest {
    
    @Test public void testSingleElement() {
        List<Edge> list = new LinkedList<Edge>();
        assertFalse(new TinkerEdgeSequence(list, "test").hasNext());
        
        TinkerGraph g = new TinkerGraph();
        Vertex v1 = g.addVertex("timetabling");
        Vertex v2 = g.addVertex("pannous");
        list = new LinkedList<Edge>();
        list.add(g.addEdge(null, v1, v2, "followers"));
        TinkerEdgeSequence iter = new TinkerEdgeSequence(list, "followers");
        assertTrue(iter.hasNext());
        iter.next();
        assertFalse(iter.hasNext());
    }
}
