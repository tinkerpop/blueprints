package com.tinkerpop.blueprints.pgm.impls;

import java.util.Map;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Parameter<K, V> implements Map.Entry<K, V>  {

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
        return (object.getClass().equals(Parameter.class) && ((Parameter) object).getKey().equals(this.key) && ((Parameter) object).getValue().equals(this.value));
    }

    public String toString() {
        return "parameter[" + key.toString() + "," + value.toString() + "]";
    }
}
