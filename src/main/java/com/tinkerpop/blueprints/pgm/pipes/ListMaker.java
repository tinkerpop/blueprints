package com.tinkerpop.blueprints.pgm.pipes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ListMaker<T> {

    public List<T> makeList(Iterator<T> iterator) {
        ArrayList<T> list = new ArrayList();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }
}
