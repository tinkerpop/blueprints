package com.tinkerpop.blueprints.pgm.pipes;

import java.util.*;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class PipeHelper {

    public static <T> List<T> makeList(Iterator<T> iterator) {
        List<T> list = new ArrayList<T>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

    public static <T> Set<T> makeSet(Iterator<T> iterator) {
        Set<T> set = new HashSet<T>();
        while(iterator.hasNext()) {
            set.add(iterator.next());
        }
        return set;
    }

    public static <T> int counter(Iterator<T> iterator) {
        int counter = 0;
        while(iterator.hasNext()) {
            iterator.next();
            counter++;
        }
        return counter;
    }
}
