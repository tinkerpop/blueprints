package com.tinkerpop.blueprints.pgm.impls.wrapped;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Element;

import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class WrappedAutomaticIndex<T extends Element> extends WrappedIndex<T> implements AutomaticIndex<T> {

    public WrappedAutomaticIndex(final AutomaticIndex<T> rawIndex) {
        super(rawIndex);
    }

    public Set<String> getAutoIndexKeys() {
        return ((AutomaticIndex<T>) this.rawIndex).getAutoIndexKeys();
    }

    public Type getIndexType() {
        return Type.AUTOMATIC;
    }
}

