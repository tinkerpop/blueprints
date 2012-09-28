package com.tinkerpop.blueprints.util.generators;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public abstract class AbstractGenerator {

    private final String label;
    private final EdgeAnnotator annotator;

    public AbstractGenerator(String label, EdgeAnnotator annotator) {
        if (label==null || label.isEmpty()) throw new IllegalArgumentException("Label cannot be empty");
        if (annotator==null) throw new NullPointerException();
        this.label=label;
        this.annotator=annotator;
    }

    public AbstractGenerator(String label) {
        this(label,EdgeAnnotator.NONE);
    }
    
    public final String getLabel() {
        return label;
    }

    public final EdgeAnnotator getEdgeAnnotator() {
        return annotator;
    }
    
    protected final Edge addEdge(Graph graph, Vertex out, Vertex in) {
        Edge e = graph.addEdge(null,out,in,label);
        annotator.annotate(e);
        return e;
    }

}
