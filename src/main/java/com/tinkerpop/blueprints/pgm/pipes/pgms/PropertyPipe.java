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

    protected void setNext() {
        if (this.starts.hasNext()) {
            Element element = this.starts.next();
            E property = (E) element.getProperty(this.key);
            if (null != property)
                this.nextEnd = property;
            else
                this.setNext();
        } else {
            this.done = true;
        }
    }
}
