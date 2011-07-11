package com.tinkerpop.blueprints.pgm.oupls.sail;

import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class TinkerGraphSailTest extends GraphSailTest {
    @Override
    protected IndexableGraph createGraph() {
        return new TinkerGraph();
    }
}
