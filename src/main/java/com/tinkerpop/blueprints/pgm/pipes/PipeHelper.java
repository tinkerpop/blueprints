package com.tinkerpop.blueprints.pgm.pipes;

import java.util.*;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class PipeHelper {

    public static <T> void fillCollection(Iterator<T> iterator, Collection<T> collection) {
        while (iterator.hasNext()) {
            collection.add(iterator.next());
        }
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
