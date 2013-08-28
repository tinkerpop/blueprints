package com.tinkerpop.blueprints.oupls.sail;


import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class TinkerGraphSailTest extends GraphSailTest {
    @Override
    protected Graph createGraph() {
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
