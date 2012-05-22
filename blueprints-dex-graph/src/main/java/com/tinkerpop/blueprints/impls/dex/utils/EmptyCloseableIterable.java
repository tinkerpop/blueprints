package com.tinkerpop.blueprints.impls.dex.utils;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.tinkerpop.blueprints.CloseableIterable;

/**
 * An empty closeable iterable.
 *
 * @param <S>
 */
public class EmptyCloseableIterable<S> implements CloseableIterable<S> {

    @Override
    public Iterator<S> iterator() {
        return ((List<S>)Collections.emptyList()).iterator();
    }

    @Override
    public void close() {
    }
}
