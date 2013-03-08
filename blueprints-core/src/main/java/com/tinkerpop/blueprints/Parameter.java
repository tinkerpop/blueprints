package com.tinkerpop.blueprints;

import java.util.Map;

/**
 * A Parameter is simply a pair of objects of type K and of type V.
 * This is used in situations where a key/value pair is needed for configuration.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Parameter<K, V> implements Map.Entry<K, V> {

    private final K key;
    private V value;

    public Parameter(final K key, final V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public V setValue(V value) {
        this.value = value;
        return value;
    }

    public boolean equals(Object object) {
        if (object.getClass().equals(Parameter.class)) {
            final Object otherKey = ((Parameter) object).getKey();
            final Object otherValue = ((Parameter) object).getValue();
            if (otherKey == null) {
                if (key != null)
                    return false;
            } else {
                if (!otherKey.equals(key))
                    return false;
            }

            if (otherValue == null) {
                if (value != null)
                    return false;
            } else {
                if (!otherValue.equals(value))
                    return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    public String toString() {
        return "parameter[" + key + "," + value + "]";
    }
}