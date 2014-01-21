package com.tinkerpop.blueprints.impls.neo4j2.batch;

import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Edge;
import org.neo4j.graphdb.index.IndexHits;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
class Neo4j2BatchEdgeIterable implements CloseableIterable<Edge> {

    private final IndexHits<Long> hits;
    private final Neo4j2BatchGraph graph;

    public Neo4j2BatchEdgeIterable(final Neo4j2BatchGraph graph, final IndexHits<Long> hits) {
        this.hits = hits;
        this.graph = graph;
    }

    public Iterator<Edge> iterator() {
        return new Iterator<Edge>() {
            private final Iterator<Long> itty = hits.iterator();

            public void remove() {
                itty.remove();
            }

            public boolean hasNext() {
                return hits.hasNext();
            }

            public Edge next() {
                return new Neo4j2BatchEdge(graph, itty.next(), null);
            }
        };
    }


    public void close() {
        hits.close();
    }
}