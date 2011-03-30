package com.tinkerpop.blueprints.pgm.oupls.sail;

import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;

/**
 * User: josh
 * Date: 1/18/11
 * Time: 10:54 AM
 */
public class TinkerGraphSailTest extends GraphSailTest {
    @Override
    protected IndexableGraph createGraph() {
        return new TinkerGraph();
    }
}
