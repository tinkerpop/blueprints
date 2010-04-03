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

    protected Vertex processNextStart() {
        if (this.step.equals(Step.OUT_VERTEX))
            return this.starts.next().getOutVertex();
        else
            return this.starts.next().getInVertex();
    }

}
