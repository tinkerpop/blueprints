package com.tinkerpop.blueprints.util.generators;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

/**
 * Base class for all synthetic network generators.
 * 
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public abstract class AbstractGenerator {

    private final String label;
    private final EdgeAnnotator annotator;

    /**
     * Constructs a new network generator for edges with the given label and annotator.
     *
     * @param label Label for the generated edges
     * @param annotator EdgeAnnotator to use for annotating newly generated edges.
     */
    public AbstractGenerator(String label, EdgeAnnotator annotator) {
        if (label==null || label.isEmpty()) throw new IllegalArgumentException("Label cannot be empty");
        if (annotator==null) throw new NullPointerException();
        this.label=label;
        this.annotator=annotator;
    }

    /**
     * Constructs a new network generator for edges with the given label and an empty annotator.
     *
     * @param label Label for the generated edges
     */
    public AbstractGenerator(String label) {
        this(label,EdgeAnnotator.NONE);
    }

    /**
     * Returns the label for this generator.
     * 
     * @return
     */
    public final String getLabel() {
        return label;
    }

    /**
     * Returns the {@link EdgeAnnotator} for this generator
     * @return
     */
    public final EdgeAnnotator getEdgeAnnotator() {
        return annotator;
    }
    
    protected final Edge addEdge(Graph graph, Vertex out, Vertex in) {
        Edge e = graph.addEdge(null,out,in,label);
        annotator.annotate(e);
        return e;
    }

}
