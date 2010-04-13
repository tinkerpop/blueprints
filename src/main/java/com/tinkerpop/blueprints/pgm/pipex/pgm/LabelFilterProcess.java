package com.tinkerpop.blueprints.pgm.pipex.pgm;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.pipex.Channel;
import com.tinkerpop.blueprints.pgm.pipex.SerialProcess;

import java.util.Collection;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class LabelFilterProcess extends SerialProcess<Edge, Edge> {

    private final Collection<String> labels;
    private final boolean filter;

    public LabelFilterProcess(final Collection<String> labels, final boolean filter) {
        this(labels, filter, null, null);
    }

    public LabelFilterProcess(final Collection<String> labels, final boolean filter, Channel<Edge> inChannel, Channel<Edge> outChannel) {
        super(inChannel, outChannel);
        this.labels = labels;
        this.filter = filter;
    }

    public boolean step() {
        Edge edge = inChannel.read();
        if (null != edge) {
            if (filter) {
                if (!this.labels.contains(edge.getLabel())) {
                    this.outChannel.write(edge);
                }
            } else {
                if (this.labels.contains(edge.getLabel())) {
                    this.outChannel.write(edge);
                }
            }
            return true;
        } else {
            return false;
        }
    }
}
