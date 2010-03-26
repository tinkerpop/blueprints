package com.tinkerpop.blueprints.pgm.pipes;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;

import java.util.Iterator;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class VertexEdgePipe extends AbstractPipe<Vertex, Edge> {

    protected Iterator<Edge> nextEnds;
    private final Step step;

    public enum Step {
        OUT_EDGES, IN_EDGES
    }

    public VertexEdgePipe(final Step step) {
        this.step = step;
    }

    protected void setNext() {
        if (null != nextEnds && nextEnds.hasNext()) {
            this.nextEnd = nextEnds.next();
        } else {
            if (this.starts.hasNext()) {
                if (this.step.equals(Step.OUT_EDGES))
                    nextEnds = this.starts.next().getOutEdges().iterator();
                else
                    nextEnds = this.starts.next().getInEdges().iterator();
                this.setNext();
            } else {
                this.done = true;
            }
        }
    }
}