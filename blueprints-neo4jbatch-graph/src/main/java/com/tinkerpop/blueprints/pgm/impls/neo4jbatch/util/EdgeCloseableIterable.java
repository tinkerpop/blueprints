package com.tinkerpop.blueprints.pgm.impls.neo4jbatch.util;

import com.tinkerpop.blueprints.pgm.CloseableIterable;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.impls.neo4jbatch.Neo4jBatchEdge;
import com.tinkerpop.blueprints.pgm.impls.neo4jbatch.Neo4jBatchGraph;
import org.neo4j.graphdb.index.IndexHits;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class EdgeCloseableIterable implements CloseableIterable<Edge> {

    private final IndexHits<Long> hits;
    private final Neo4jBatchGraph graph;

    public EdgeCloseableIterable(final Neo4jBatchGraph graph, final IndexHits<Long> hits) {
        this.hits = hits;
        this.graph = graph;
    }

    public Iterator<Edge> iterator() {
        return new EdgeIterator();
    }


    public void close() {
        hits.close();
    }

    private class EdgeIterator implements Iterator<Edge> {
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
    }
}