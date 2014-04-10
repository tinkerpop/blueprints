package com.tinkerpop.blueprints.oupls.sail;


import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import org.junit.Test;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class TinkerGraphSailTest extends GraphSailTest {
    @Override
    protected KeyIndexableGraph createGraph() {
        /*
        BNodeImpl b = new BNodeImpl("foo");
        System.out.println(b.toString());
        System.out.println(b.getID());
        System.out.println(b.stringValue());
        System.exit(1);
        */

        return new TinkerGraph();
    }

    @Test
    public void testTmp() throws Exception {
        KeyIndexableGraph g = new TinkerGraph();
        GraphSail sail = new GraphSail(g, "sp,p,c,pc");
    }
}
