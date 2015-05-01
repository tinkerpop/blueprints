package com.tinkerpop.blueprints.impls.neo4j2;


import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Vertex;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.IndexHits;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4j2VertexIterable<T extends Vertex> implements CloseableIterable<Neo4j2Vertex> {

    private final Iterable<Node> nodes;
    private final Neo4j2Graph graph;

    /**
     * @deprecated the {@code checkTransaction} parameter is no longer used.
     */
    @Deprecated
    public Neo4j2VertexIterable(final Iterable<Node> nodes, final Neo4j2Graph graph, final boolean checkTransaction) {
        this(nodes, graph);
    }

    public Neo4j2VertexIterable(final Iterable<Node> nodes, final Neo4j2Graph graph) {
        this.nodes = nodes;
        this.graph = graph;
    }

    public Iterator<Neo4j2Vertex> iterator() {
        graph.autoStartTransaction(false);
        return new Iterator<Neo4j2Vertex>() {
            private final Iterator<Node> itty = nodes.iterator();

            public void remove() {
                this.itty.remove();
            }

            public Neo4j2Vertex next() {
                graph.autoStartTransaction(false);
                return new Neo4j2Vertex(this.itty.next(), graph);
            }

            public boolean hasNext() {
                graph.autoStartTransaction(false);
                return this.itty.hasNext();
            }
        };
    }

    public void close() {
        if (this.nodes instanceof IndexHits) {
            ((IndexHits) this.nodes).close();
        }
    }

}