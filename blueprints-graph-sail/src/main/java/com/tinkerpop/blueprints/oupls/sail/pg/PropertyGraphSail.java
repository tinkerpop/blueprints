package com.tinkerpop.blueprints.oupls.sail.pg;

import com.tinkerpop.blueprints.Graph;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailBase;

/**
 * A Sail implementation which provides an RDF view of any Blueprints graph.
 *
 * PropertyGraphSail can be configured to provide one of two kinds of views: one in which edges are first-class objects,
 * with edge ids, types, labels, heads, and tails each modeled as statements, and another in which each edge is a
 * single statement.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class PropertyGraphSail extends SailBase {

    public static final String EDGE_NS = "http://tinkerpop.com/pgm/edge/";
    public static final String ONTOLOGY_NS = "http://tinkerpop.com/pgm/ontology#";
    public static final String PROPERTY_NS = "http://tinkerpop.com/pgm/property/";
    public static final String RELATION_NS = "http://tinkerpop.com/pgm/relation/";
    public static final String VERTEX_NS = "http://tinkerpop.com/pgm/vertex/";

    public static final URI
            EDGE = new URIImpl(ONTOLOGY_NS + "Edge"),
            HEAD = new URIImpl(ONTOLOGY_NS + "head"),
            ID = new URIImpl(ONTOLOGY_NS + "id"),
            LABEL = new URIImpl(ONTOLOGY_NS + "label"),
            TAIL = new URIImpl(ONTOLOGY_NS + "tail"),
            VERTEX = new URIImpl(ONTOLOGY_NS + "Vertex");

    private final PropertyGraphContext context;

    private boolean firstClassEdges;

    /**
     * Instantiates a Sail based on a given Graph.
     * Graph edges are treated as first-class resources in the RDF graph by default.
     *
     * @param graph the Blueprints Graph to expose as an RDF dataset
     */
    public PropertyGraphSail(final Graph graph) {
        this(graph, true);
    }

    /**
     * Instantiates a Sail based on a given Graph.
     *
     * @param graph           the Blueprints Graph to expose as an RDF dataset
     * @param firstClassEdges whether to treat edges as first-class resources in the RDF graph, or as single statements
     */
    public PropertyGraphSail(final Graph graph,
                             final boolean firstClassEdges) {
        context = new PropertyGraphContext(graph, new PropertyGraphValueFactory());
        this.firstClassEdges = firstClassEdges;
    }

    void setFirstClassEdges(final boolean firstClassEdges) {
        this.firstClassEdges = firstClassEdges;
    }

    public boolean isWritable() throws SailException {
        return false;
    }

    protected void shutDownInternal() throws SailException {
        // Nothing to do.
    }

    public SailConnection getConnectionInternal() throws SailException {
        return new PropertyGraphSailConnection(this, context, firstClassEdges);
    }

    public ValueFactory getValueFactory() {
        return context.valueFactory;
    }

    static class PropertyGraphContext {
        public final Graph graph;

        public final ValueFactory valueFactory;

        //public final Map<String, Index<Vertex>> indices = new HashMap<String, Index<Vertex>>();

        public PropertyGraphContext(final Graph graph,
                                    final ValueFactory valueFactory) {
            this.graph = graph;
            this.valueFactory = valueFactory;
        }
    }
}
