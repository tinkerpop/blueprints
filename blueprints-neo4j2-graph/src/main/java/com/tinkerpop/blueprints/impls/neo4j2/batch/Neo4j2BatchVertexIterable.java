package com.tinkerpop.blueprints.impls.neo4j2.batch;

import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Vertex;
import org.neo4j.graphdb.index.IndexHits;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
class Neo4j2BatchVertexIterable implements CloseableIterable<Vertex> {

    private final IndexHits<Long> hits;
    private final Neo4j2BatchGraph graph;

    public Neo4j2BatchVertexIterable(final Neo4j2BatchGraph graph, final IndexHits<Long> hits) {
        this.hits = hits;
        this.graph = graph;
    }

    public Iterator<Vertex> iterator() {
        return new Iterator<Vertex>() {
            private final Iterator<Long> itty = hits.iterator();

            public void remove() {
                itty.remove();
            }

            public boolean hasNext() {
                return hits.hasNext();
            }

            public Vertex next() {
                return new Neo4j2BatchVertex(graph, itty.next());
            }
        };
    }


    public void close() {
        hits.close();
    }
}