package com.tinkerpop.blueprints.pgm.pipes;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Pipeline<S, E> implements Pipe<S, E> {

    private final List<Pipe> pipes;
    private final Pipe endPipe;

    public Pipeline(final List<Pipe> pipes) {
        this.pipes = pipes;
        this.endPipe = pipes.get(this.pipes.size() - 1);
        if (!this.validateTypes())
            throw new RuntimeException("Invalid pipeline: the E of the previous pipe must be the same type as the S of the next pipe");
    }

    public void setStarts(final Iterator<S> starts) {
        pipes.get(0).setStarts(starts);
        for (int i = 1; i < pipes.size(); i++) {
            pipes.get(i).setStarts(pipes.get(i - 1));
        }
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public boolean hasNext() {
        return endPipe.hasNext();
    }

    private boolean validateTypes() {
        /*if (this.pipes.size() > 1) {
            Type endType = ((ParameterizedType) pipes.get(0).getClass().getGenericSuperclass()).getActualTypeArguments()[1];
            for (int i = 1; i < pipes.size(); i++) {
                Type startType = ((ParameterizedType) pipes.get(i).getClass().getGenericSuperclass()).getActualTypeArguments()[0];
                if (!endType.equals(startType)) {
                    System.out.println(endType + "!!" + startType);
                    return false;
                } else
                    endType = ((ParameterizedType) pipes.get(i).getClass().getGenericSuperclass()).getActualTypeArguments()[1];
            }
        }*/
        return true;
    }

    public E next() {
        return (E) endPipe.next();
    }
}
