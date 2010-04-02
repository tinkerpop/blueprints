package com.tinkerpop.blueprints.pgm.pipes.merges;

import com.tinkerpop.blueprints.pgm.pipes.AbstractPipe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
        this.setNext();

    }

    protected void setNext() {
        Iterator<S> starts = this.allStarts.get(this.currentStarts);
        if (starts.hasNext()) {
            this.nextEnd = starts.next();
            this.currentStarts = ++this.currentStarts % this.allStarts.size();
        } else {
            this.allStarts.remove(this.currentStarts);
            int size = allStarts.size();
            if (0 == size)
                this.done = true;
            else {
                if (size == 1)
                    this.currentStarts = 0;
                this.setNext();
            }
        }
    }
}