package com.tinkerpop.blueprints.pgm.pipex.pgm;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.pipex.SerialProcess;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class VertexEdgeProcess extends SerialProcess<Vertex, Edge> {

    private final Step step;

    public enum Step {
        OUT_EDGES, IN_EDGES
    }

    public VertexEdgeProcess(final Step step) {
        this.step = step;
    }

    public void step() {
        Vertex vertex = inputChannel.read();
        Iterable<Edge> edges;
        if (this.step == Step.OUT_EDGES) {
            edges = vertex.getOutEdges();
        } else {
            edges = vertex.getInEdges();
        }
        for (Edge edge : edges) {
            this.outputChannel.write(edge);
        }
    }
}
