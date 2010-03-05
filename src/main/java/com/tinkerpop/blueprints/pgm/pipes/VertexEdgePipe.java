package com.tinkerpop.blueprints.pgm.pipes;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;

import java.util.Iterator;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class VertexEdgePipe implements Pipe<Vertex, Edge> {

    private Iterator<Vertex> starts;
    private Iterator<Edge> nextEdges;
    private Edge nextEdge;
    private final Step step;

    public enum Step {
        OUT_EDGES, IN_EDGES
    }

    public VertexEdgePipe(final Step step) {
        this.step = step;
    }

    public void setStarts(Iterator<Vertex> starts) {
        this.starts = starts;
        this.setNext();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public Edge next() {
        Edge edge = this.nextEdge;
        this.setNext();
        return edge;
    }

    public boolean hasNext() {
        return this.nextEdge != null;
    }

    private void setNext() {
        if (null != nextEdges && nextEdges.hasNext()) {
            nextEdge = nextEdges.next();
        } else {
            if (starts.hasNext()) {
                if (this.step.equals(Step.OUT_EDGES))
                    nextEdges = starts.next().getOutEdges().iterator();
                else
                    nextEdges = starts.next().getInEdges().iterator();
                this.setNext();
            } else {
                this.nextEdge = null;
            }
        }
    }
}