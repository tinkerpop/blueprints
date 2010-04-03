package com.tinkerpop.blueprints.pgm.pipes.merges;

import com.tinkerpop.blueprints.pgm.pipes.AbstractPipe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class RobinMergePipe<S> extends AbstractPipe<Iterator<S>, S> {

    private List<Iterator<S>> allStarts = new ArrayList<Iterator<S>>();
    private int currentStarts = 0;

    public void setStarts(final Iterator<Iterator<S>> starts) {
        this.starts = starts;
        while (this.starts.hasNext()) {
            allStarts.add(this.starts.next());
        }

    }

    protected S processNextStart() {
        if (this.allStarts.size() > 0) {
            Iterator<S> starts = this.allStarts.get(this.currentStarts);
            if (starts.hasNext()) {
                this.currentStarts = ++this.currentStarts % this.allStarts.size();
                return starts.next();
            } else {
                this.allStarts.remove(this.currentStarts);
                if (this.allStarts.size() == 1)
                    this.currentStarts = 0;
                return this.processNextStart();
            }
        } else {
            throw new NoSuchElementException();
        }
    }
}