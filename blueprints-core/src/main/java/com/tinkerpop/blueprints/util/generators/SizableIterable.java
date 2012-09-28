package com.tinkerpop.blueprints.util.generators;

import java.util.Collection;
import java.util.Iterator;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public class SizableIterable<T> implements Iterable<T> {

    private final Iterable<T> iterable;
    private final int size;
    
    public SizableIterable(Iterable<T> iterable, int size) {
        if (iterable==null) throw new NullPointerException();
        if (size<0) throw new IllegalArgumentException("Size must be positive");
        this.iterable=iterable;
        this.size=size;
    }
    
    public int size() {
        return size;
    }

    @Override
    public Iterator<T> iterator() {
        return iterable.iterator();
    }
    
    public static final<T> int sizeOf(Iterable<T> iterable) {
        if (iterable instanceof Collection) return ((Collection)iterable).size();
        else if (iterable instanceof SizableIterable) return ((SizableIterable)iterable).size();
        else {
            int size = 0;
            for (T obj : iterable) size++;
            return size;
        }
    }
    
}
