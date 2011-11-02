package com.tinkerpop.blueprints.pgm.impls.readonly;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ReadOnlyAutomaticIndex<T extends Element> extends ReadOnlyIndex<T> implements AutomaticIndex<T> {

    public ReadOnlyAutomaticIndex(final AutomaticIndex autoIndex) {
        super(autoIndex);
    }

    public Set<String> getAutoIndexKeys() {
        return new HashSet<String>(((AutomaticIndex) this.rawIndex).getAutoIndexKeys());
    }

    public Index.Type getIndexType() {
        return Index.Type.AUTOMATIC;
    }
}
