package com.tinkerpop.blueprints.pgm.oupls.sail;


import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

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
}
