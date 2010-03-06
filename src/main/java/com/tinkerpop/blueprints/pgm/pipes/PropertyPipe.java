package com.tinkerpop.blueprints.pgm.pipes;

import com.tinkerpop.blueprints.pgm.Element;

import java.util.Iterator;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class PropertyPipe<S extends Element, E> implements Pipe<S, E> {

    private Iterator<S> starts;
    private E nextProperty;
    private final String key;

    public PropertyPipe(final String key) {
        this.key = key;
    }


    public void setStarts(Iterator<S> starts) {
        this.starts = starts;
        this.setNext();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public E next() {
        E object = this.nextProperty;
        this.setNext();
        return object;
    }

    public boolean hasNext() {
        return this.nextProperty != null;
    }

    private void setNext() {
        if (starts.hasNext()) {
            Element element = starts.next();
            E property = (E) element.getProperty(key);
            if (null != property)
                this.nextProperty = property;
            else
                this.setNext();

        } else {
            this.nextProperty = null;
        }
    }
}
