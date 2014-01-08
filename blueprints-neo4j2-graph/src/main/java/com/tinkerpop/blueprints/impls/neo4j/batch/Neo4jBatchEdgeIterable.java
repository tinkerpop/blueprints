package com.tinkerpop.blueprints.impls.neo4j.batch;

import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Edge;
import org.neo4j.graphdb.index.IndexHits;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
class Neo4jBatchEdgeIterable implements CloseableIterable<Edge> {

    private final IndexHits<Long> hits;
    private final Neo4jBatchGraph graph;

    public Neo4jBatchEdgeIterable(final Neo4jBatchGraph graph, final IndexHits<Long> hits) {
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
                return new Neo4jBatchEdge(graph, itty.next(), null);
            }
        };
    }


    public void close() {
        hits.close();
    }
}