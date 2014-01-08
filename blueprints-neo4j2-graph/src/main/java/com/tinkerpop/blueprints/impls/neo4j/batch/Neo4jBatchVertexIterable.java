package com.tinkerpop.blueprints.impls.neo4j.batch;

import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Vertex;
import org.neo4j.graphdb.index.IndexHits;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
class Neo4jBatchVertexIterable implements CloseableIterable<Vertex> {

    private final IndexHits<Long> hits;
    private final Neo4jBatchGraph graph;

    public Neo4jBatchVertexIterable(final Neo4jBatchGraph graph, final IndexHits<Long> hits) {
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
                return new Neo4jBatchVertex(graph, itty.next());
            }
        };
    }


    public void close() {
        hits.close();
    }
}