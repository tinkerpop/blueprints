package com.tinkerpop.blueprints.pgm.pipex.pgm;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.pipex.SerialProcess;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class EdgeVertexProcess extends SerialProcess<Edge, Vertex> {

    private final Step step;

    public enum Step {
        IN_VERTEX, OUT_VERTEX
    }

    public EdgeVertexProcess(final Step step) {
        this.step = step;
    }

    public void step() {
        Edge edge = this.inputChannel.read();
        if (this.step == Step.IN_VERTEX)
            this.outputChannel.write(edge.getInVertex());
        else
            this.outputChannel.write(edge.getOutVertex());
    }

}
