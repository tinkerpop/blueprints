package com.tinkerpop.blueprints.pgm.impls.named;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Element;

import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class NamedAutomaticIndex<T extends Element> extends NamedIndex<T> implements AutomaticIndex<T> {

    public NamedAutomaticIndex(final AutomaticIndex<T> rawIndex) {
        super(rawIndex);
    }

    public Set<String> getAutoIndexKeys() {
        return ((AutomaticIndex<T>) this.rawIndex).getAutoIndexKeys();
    }

    public Type getIndexType() {
        return Type.AUTOMATIC;
    }
}

