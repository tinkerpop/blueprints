package com.tinkerpop.blueprints.pgm.pipes;

import java.util.Iterator;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class OrderedMergePipe<E> extends AbstractPipe<Iterator<E>, E> {

    protected Iterator<E> nextEnds;

    protected void setNext() {
        if (null != nextEnds && nextEnds.hasNext()) {
            this.nextEnd = this.nextEnds.next();
        } else {
            if ((null == nextEnds || !this.nextEnds.hasNext()) && this.starts.hasNext()) {
                this.nextEnds = this.starts.next();
                this.setNext();
            } else {
                this.done = true;
            }
        }
    }
}
