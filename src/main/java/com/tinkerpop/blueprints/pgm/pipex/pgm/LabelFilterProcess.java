package com.tinkerpop.blueprints.pgm.pipex.pgm;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.pipex.SerialProcess;

import java.util.Collection;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class LabelFilterProcess extends SerialProcess<Edge, Edge> {

    private final Collection<String> labels;
    private final boolean filter;

    public LabelFilterProcess(final Collection<String> labels, final boolean filter) {
        this.labels = labels;
        this.filter = filter;
    }

    public void step() {
        Edge edge = inputChannel.read();
        if (filter) {
            if (!this.labels.contains(edge.getLabel())) {
                this.outputChannel.write(edge);
            }
        } else {
            if (this.labels.contains(edge.getLabel())) {
                this.outputChannel.write(edge);
            }
        }
    }
}
