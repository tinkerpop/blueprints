package com.tinkerpop.blueprints.pgm.pipes;

import com.tinkerpop.blueprints.pgm.pipes.merges.ReadyMergePipe;
import com.tinkerpop.blueprints.pgm.pipes.splits.ReadySplitPipe;

import java.util.Iterator;
import java.util.List;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ParallelPipeline<S, E> extends AbstractPipe<S, E> {

    private final List<Pipe<S, E>> parallelPipes;
    private final ReadyMergePipe<E> readyMergePipe = new ReadyMergePipe<E>();

    public ParallelPipeline(List<Pipe<S, E>> parallelPipes) {
        this.parallelPipes = parallelPipes;
    }

    public void setStarts(Iterator<S> starts) {
        ReadySplitPipe<S> readySplitPipe = new ReadySplitPipe<S>(parallelPipes.size());
        readySplitPipe.setStarts(starts);
        for (int i = 0; i < parallelPipes.size(); i++) {
            parallelPipes.get(i).setStarts(readySplitPipe.getSplit(i));
        }
        this.readyMergePipe.setStarts((Iterator) parallelPipes.iterator());
    }

    public E processNextStart() {
        return this.readyMergePipe.next();

    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
