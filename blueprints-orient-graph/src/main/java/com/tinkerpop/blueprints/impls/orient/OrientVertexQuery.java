package com.tinkerpop.blueprints.impls.orient;

import com.tinkerpop.blueprints.util.DefaultVertexQuery;

/**
 * OrientDB implementation for vertex query.
 *
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientVertexQuery extends DefaultVertexQuery {

    public OrientVertexQuery(final OrientVertex vertex) {
        super(vertex);
    }

    @Override
    public long count() {
        if (hasContainers.isEmpty()) {
            // NO CONDITIONS: USE THE FAST COUNT
            long counter = ((OrientVertex) vertex).countEdges(direction, labels);
            if (minimum != 0)
              counter -= minimum;
            if (maximum != Long.MAX_VALUE && counter > maximum)
                return maximum;
            return counter;
        }

        // ITERATE EDGES TO MATCH CONDITIONS
        return super.count();
    }
}
