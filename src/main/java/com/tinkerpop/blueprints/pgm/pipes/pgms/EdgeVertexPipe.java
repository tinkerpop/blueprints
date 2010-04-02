package com.tinkerpop.blueprints.pgm.pipes.pgms;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.pipes.AbstractPipe;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class EdgeVertexPipe extends AbstractPipe<Edge, Vertex> {

    private final Step step;

    public enum Step {
        IN_VERTEX, OUT_VERTEX
    }

    public EdgeVertexPipe(final Step step) {
        this.step = step;
    }

    protected void setNext() {
        if (this.starts.hasNext()) {
            if (this.step.equals(Step.OUT_VERTEX))
                this.nextEnd = this.starts.next().getOutVertex();
            else
                this.nextEnd = this.starts.next().getInVertex();
        } else {
            this.done = true;
        }
    }

}
