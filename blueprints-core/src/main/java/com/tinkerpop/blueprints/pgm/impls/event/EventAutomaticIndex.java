package com.tinkerpop.blueprints.pgm.impls.event;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.impls.event.listener.GraphChangedListener;

import java.util.List;
import java.util.Set;

/**
 * An automatic index that wraps graph elements in the "evented" way. This class does not directly raise graph events,
 * but passes the GraphChangedListener to the edges and vertices returned from indices so that they may raise graph
 * events.
 *
 * @author Stephen Mallette
 */
public class EventAutomaticIndex<T extends Element> extends EventIndex<T> implements AutomaticIndex<T> {

    public EventAutomaticIndex(final AutomaticIndex rawIndex, final List<GraphChangedListener> graphChangedListeners) {
        super(rawIndex, graphChangedListeners);
    }

    public Set<String> getAutoIndexKeys() {
        return ((AutomaticIndex) this.rawIndex).getAutoIndexKeys();
    }

    public Type getIndexType() {
        return Type.AUTOMATIC;
    }
}
