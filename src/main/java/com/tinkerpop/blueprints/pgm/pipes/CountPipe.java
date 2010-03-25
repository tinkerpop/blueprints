package com.tinkerpop.blueprints.pgm.pipes;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class CountPipe<S> extends AbstractPipe<S, S> implements SideEffectPipe<S, S, Long> {

    private Long counter = 0l;

    protected void setNext() {
        if (this.starts.hasNext()) {
            this.nextEnd = this.starts.next();
        } else {
            this.nextEnd = null;
        }
    }

    public S next() {
        S end = this.nextEnd;
        this.setNext();
        counter++;
        return end;
    }

    public Long getSideEffect() {
        return this.counter;
    }
}
