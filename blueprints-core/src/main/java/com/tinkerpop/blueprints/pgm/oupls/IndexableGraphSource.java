package com.tinkerpop.blueprints.pgm.oupls;

import com.tinkerpop.blueprints.pgm.IndexableGraph;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface IndexableGraphSource extends GraphSource {

    public IndexableGraph getGraph();
}
