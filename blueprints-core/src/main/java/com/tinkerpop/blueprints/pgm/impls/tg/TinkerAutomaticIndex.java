package com.tinkerpop.blueprints.pgm.impls.tg;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class TinkerAutomaticIndex<T extends TinkerElement> extends TinkerIndex<T> implements AutomaticIndex<T>, Serializable {

    Set<String> autoIndexKeys;

    public TinkerAutomaticIndex(final String name, final Class<T> indexClass, final Set<String> keys) {
        super(name, indexClass);
        if (keys == null)
            this.autoIndexKeys = null;
        else {
            this.autoIndexKeys = new HashSet<String>();
            this.autoIndexKeys.addAll(keys);
        }
    }

    public Type getIndexType() {
        return Type.AUTOMATIC;
    }

    public Set<String> getAutoIndexKeys() {
        if (this.autoIndexKeys == null)
            return null;
        else
            return new HashSet<String>(this.autoIndexKeys);
    }

    protected void autoUpdate(final String key, final Object newValue, final Object oldValue, final T element) {
        if (this.getIndexClass().isAssignableFrom(element.getClass()) && (this.autoIndexKeys == null || this.autoIndexKeys.contains(key))) {
            if (oldValue != null)
                this.remove(key, oldValue, element);
            this.put(key, newValue, element);
        }
    }

    protected void autoRemove(final String key, final Object oldValue, final T element) {
        if (this.getIndexClass().isAssignableFrom(element.getClass()) && (this.autoIndexKeys == null || this.autoIndexKeys.contains(key))) {
            this.remove(key, oldValue, element);
        }
    }
}
