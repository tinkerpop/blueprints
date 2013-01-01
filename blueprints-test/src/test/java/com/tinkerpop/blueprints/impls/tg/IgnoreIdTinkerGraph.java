package com.tinkerpop.blueprints.impls.tg;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.Vertex;

/**
 * This is class is an in-memory variant of TinkerGraph that ignores the supplied ids
 * and instead uses its own internal id scheme.
 * This is meant to be used for testing only.
 *
 * @author Matthias Broecheler (http://www.matthiasb.com)
 */

public class IgnoreIdTinkerGraph extends TinkerGraph {

    public IgnoreIdTinkerGraph() {
        super();
    }

    public IgnoreIdTinkerGraph(String directory) {
        super(directory);
    }

    @Override
    public Features getFeatures() {
        Features f = super.getFeatures().copyFeatures();
        f.ignoresSuppliedIds = true;
        return f;
    }

    @Override
    public Vertex addVertex(Object id) {
        return super.addVertex(null);
    }

    @Override
    public Edge addEdge(Object id, Vertex outVertex, Vertex inVertex, String label) {
        return super.addEdge(null, outVertex, inVertex, label);
    }

}
