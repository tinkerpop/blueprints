package com.tinkerpop.blueprints.pgm.pipes.filters;

import com.tinkerpop.blueprints.pgm.pipes.AbstractPipe;

import java.util.Random;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class SampleFilterPipe<S> extends AbstractPipe<S, S> {

    private static final Random RANDOM = new Random();
    private final double bias;

    public SampleFilterPipe(double bias) {
        this.bias = bias;
    }

    protected void setNext() {
        if (this.starts.hasNext()) {
            S temp = this.starts.next();
            if (bias >= RANDOM.nextDouble()) {
                this.nextEnd = temp;
            } else {
                this.setNext();
            }
        } else {
            this.done = true;
        }

    }
}
