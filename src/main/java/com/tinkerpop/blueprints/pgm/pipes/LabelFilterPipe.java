package com.tinkerpop.blueprints.pgm.pipes;

import com.tinkerpop.blueprints.pgm.Edge;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class LabelFilterPipe implements Pipe<Edge, Edge> {

    private Iterator<Edge> starts;
    private Edge nextLegal;
    private final Collection<String> labels;
    private final boolean filter;

    public LabelFilterPipe(final Collection<String> labels, final boolean filter) {
        this.labels = labels;
        this.filter = filter;
    }


    public void setStarts(Iterator<Edge> starts) {
        this.starts = starts;
        this.setNext();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public Edge next() {
        Edge edge = this.nextLegal;
        this.setNext();
        return edge;
    }

    public boolean hasNext() {
        return null != this.nextLegal;
    }

    private void setNext() {
        while (starts.hasNext()) {
            Edge edge = this.starts.next();
            if (this.filter) {
                if (!labels.contains(edge.getLabel())) {
                    this.nextLegal = edge;
                    return;
                }
            } else {
                if (labels.contains(edge.getLabel())) {
                    this.nextLegal = edge;
                    return;
                }
            }
        }
        this.nextLegal = null;
    }
}
