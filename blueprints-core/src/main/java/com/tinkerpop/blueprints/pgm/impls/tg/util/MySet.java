package com.tinkerpop.blueprints.pgm.impls.tg.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Peter Karich, info@jetsli.de
 */
public class MySet {

    public static <T> Set<T> create() {
        return Collections.newSetFromMap(new ConcurrentHashMap<T, Boolean>());
    }

    public static <T> Set<T> create(int cap) {
        return Collections.newSetFromMap(new ConcurrentHashMap<T, Boolean>(cap));
    }

    public static <T> Set<T> create(Collection<T> coll) {
        Set<T> newSet = create(coll.size());
        newSet.addAll(coll);
        return newSet;
    }
}
