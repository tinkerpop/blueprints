package com.tinkerpop.blueprints.pgm.impls.tg;

import com.tinkerpop.blueprints.pgm.CloseableSequence;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.impls.WrappingCloseableSequence;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class TinkerIndex<T extends Element> implements Index<T>, Serializable {

    private Map<String, Map<Object, Set<T>>> index = new HashMap<String, Map<Object, Set<T>>>();
    private final String indexName;
    private final Class<T> indexClass;

    public TinkerIndex(final String indexName, final Class<T> indexClass) {
        this.indexName = indexName;
        this.indexClass = indexClass;
    }

    public String getIndexName() {
        return this.indexName;
    }

    public Class<T> getIndexClass() {
        return this.indexClass;
    }

    public Type getIndexType() {
        return Type.MANUAL;
    }

    public void put(final String key, final Object value, final T element) {
        Map<Object, Set<T>> keyMap = this.index.get(key);
        if (keyMap == null) {
            keyMap = new HashMap<Object, Set<T>>();
            this.index.put(key, keyMap);
        }
        Set<T> objects = keyMap.get(value);
        if (null == objects) {
            objects = new HashSet<T>();
            keyMap.put(value, objects);
        }
        objects.add(element);

    }

    public CloseableSequence<T> get(final String key, final Object value) {
        Map<Object, Set<T>> keyMap = this.index.get(key);
        if (null == keyMap) {
            return new WrappingCloseableSequence<T>(new HashSet<T>());
        } else {
            Set<T> set = keyMap.get(value);
            if (null == set)
                return new WrappingCloseableSequence<T>(new HashSet<T>());
            else
                return new WrappingCloseableSequence<T>(new LinkedList<T>(set));
        }
    }

    public long count(final String key, final Object value) {
        Map<Object, Set<T>> keyMap = this.index.get(key);
        if (null == keyMap) {
            return 0;
        } else {
            Set<T> set = keyMap.get(value);
            if (null == set)
                return 0;
            else
                return set.size();
        }
    }

    public void remove(final String key, final Object value, final T element) {
        Map<Object, Set<T>> keyMap = this.index.get(key);
        if (null != keyMap) {
            Set<T> objects = keyMap.get(value);
            if (null != objects) {
                objects.remove(element);
                if (objects.size() == 0) {
                    keyMap.remove(value);
                }
            }
        }
    }

    public void removeElement(final T element) {
        if (this.indexClass.isAssignableFrom(element.getClass())) {
            for (Map<Object, Set<T>> map : index.values()) {
                for (Set<T> set : map.values()) {
                    set.remove(element);
                }
            }
        }
    }

    public String toString() {
        return StringFactory.indexString(this);
    }
}
