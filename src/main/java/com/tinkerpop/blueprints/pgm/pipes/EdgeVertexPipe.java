package com.tinkerpop.blueprints.pgm.pipes;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;

import java.util.Iterator;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class EdgeVertexPipe implements Pipe<Edge, Vertex> {

    private Iterator<Edge> starts;
    private Vertex nextVertex;
    private final Step step;

    public enum Step {
        IN_VERTEX, OUT_VERTEX
    }

    public EdgeVertexPipe(final Step step) {
        this.step = step;
    }


    public void setStarts(Iterator<Edge> starts) {
        this.starts = starts;
        this.setNext();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public Vertex next() {
        Vertex vertex = this.nextVertex;
        this.setNext();
        return vertex;
    }

    public boolean hasNext() {
        return this.nextVertex != null;
    }

    private void setNext() {
        if (starts.hasNext()) {
            if (this.step.equals(Step.OUT_VERTEX))
                this.nextVertex = this.starts.next().getOutVertex();
            else
                this.nextVertex = this.starts.next().getInVertex();
        } else {
            this.nextVertex = null;
        }
    }

}
