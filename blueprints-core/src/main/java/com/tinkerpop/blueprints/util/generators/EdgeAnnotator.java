package com.tinkerpop.blueprints.util.generators;

import com.tinkerpop.blueprints.Edge;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public interface EdgeAnnotator {

    public final EdgeAnnotator NONE = new EdgeAnnotator() {
        @Override
        public void annotate(Edge edge) {
            //Do nothing
        }
    };

    public void annotate(Edge edge);

}
