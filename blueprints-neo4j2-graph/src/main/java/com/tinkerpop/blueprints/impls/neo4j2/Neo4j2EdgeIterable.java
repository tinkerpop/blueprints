package com.tinkerpop.blueprints.impls.neo4j2;


import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Edge;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.IndexHits;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4j2EdgeIterable<T extends Edge> implements CloseableIterable<Neo4j2Edge> {

    private final Iterable<Relationship> relationships;
    private final Neo4j2Graph graph;

    /**
     * @deprecated the {@code checkTransaction} parameter is no longer used.
     */
    @Deprecated
    public Neo4j2EdgeIterable(final Iterable<Relationship> relationships, final Neo4j2Graph graph, final boolean checkTransaction) {
        this(relationships, graph);
    }

    public Neo4j2EdgeIterable(final Iterable<Relationship> relationships, final Neo4j2Graph graph) {
        this.relationships = relationships;
        this.graph = graph;
    }

    public Iterator<Neo4j2Edge> iterator() {
        graph.autoStartTransaction(true);
        return new Iterator<Neo4j2Edge>() {
            private final Iterator<Relationship> itty = relationships.iterator();

            public void remove() {
                graph.autoStartTransaction(true);
                this.itty.remove();
            }

            public Neo4j2Edge next() {
                graph.autoStartTransaction(false);
                return new Neo4j2Edge(this.itty.next(), graph);
            }

            public boolean hasNext() {
                graph.autoStartTransaction(false);
                return this.itty.hasNext();
            }
        };
    }

    public void close() {
        if (this.relationships instanceof IndexHits) {
            ((IndexHits) this.relationships).close();
        }
    }
}