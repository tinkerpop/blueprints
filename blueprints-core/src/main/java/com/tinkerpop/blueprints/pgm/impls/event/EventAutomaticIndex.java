package com.tinkerpop.blueprints.pgm.impls.event;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.impls.event.listener.GraphChangedListener;

import java.util.List;
import java.util.Set;

/**
 * An automatic index that wraps graph elements in the "evented" way.
 */
public class EventAutomaticIndex<T extends Element> extends EventIndex<T> implements AutomaticIndex<T> {

    public EventAutomaticIndex(final AutomaticIndex autoIndex, final List<GraphChangedListener> graphChangedListeners) {
        super(autoIndex, graphChangedListeners);
    }

    public Set<String> getAutoIndexKeys() {
        return ((AutomaticIndex) this.index).getAutoIndexKeys();
    }

    public Type getIndexType() {
        return Type.AUTOMATIC;
    }
}
