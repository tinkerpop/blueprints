package com.tinkerpop.blueprints.pgm.pipes;

import java.util.Iterator;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface Pipe<S,E> extends Iterator<E> {

    public void setStarts(Iterator<S> starts);
}
