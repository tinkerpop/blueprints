package com.tinkerpop.blueprints.pgm.pipes.pgms;

import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.pipes.AbstractPipe;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class PropertyPipe<S extends Element, E> extends AbstractPipe<S, E> {

    private final String key;

    public PropertyPipe(final String key) {
        this.key = key;
    }

    protected E processNextStart() {
        return (E) this.starts.next().getProperty(this.key);
    }
}
