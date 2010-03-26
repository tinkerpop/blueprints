package com.tinkerpop.blueprints.pgm.pipes;

import com.tinkerpop.blueprints.pgm.Edge;

import java.util.Collection;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class LabelFilterPipe extends AbstractFilterPipe<Edge, Edge, String> {

    private final Collection<String> labels;
    private final boolean filter;

    public LabelFilterPipe(final Collection<String> labels, final boolean filter) {
        this.labels = labels;
        this.filter = filter;
    }

    protected void setNext() {
        while (this.starts.hasNext()) {
            Edge edge = this.starts.next();
            if (this.filter) {
                if (!this.doesContain(this.labels, edge.getLabel())) {
                    this.nextEnd = edge;
                    return;
                }
            } else {
                if (this.doesContain(this.labels, edge.getLabel())) {
                    this.nextEnd = edge;
                    return;
                }
            }
        }
        this.done = true;
    }
}
