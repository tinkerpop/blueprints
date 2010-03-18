package com.tinkerpop.blueprints.pgm.pipes;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class CountPipe<S> extends AbstractPipe<S, Integer> {

    private boolean done = false;

    protected void setNext() {
        if (!done) {
            int counter = 0;
            while (this.starts.hasNext()) {
                this.starts.next();
                counter++;
            }
            this.nextEnd = counter;
            done = true;
        } else {
            this.nextEnd = null;
        }
    }
}
