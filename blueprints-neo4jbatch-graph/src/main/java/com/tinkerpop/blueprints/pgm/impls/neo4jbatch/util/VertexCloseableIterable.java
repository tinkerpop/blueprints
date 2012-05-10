package com.tinkerpop.blueprints.pgm.impls.neo4jbatch.util;

import com.tinkerpop.blueprints.pgm.CloseableIterable;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.neo4jbatch.Neo4jBatchGraph;
import com.tinkerpop.blueprints.pgm.impls.neo4jbatch.Neo4jBatchVertex;
import org.neo4j.graphdb.index.IndexHits;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class VertexCloseableIterable implements CloseableIterable<Vertex> {

    private final IndexHits<Long> hits;
    private final Neo4jBatchGraph graph;

    public VertexCloseableIterable(final Neo4jBatchGraph graph, final IndexHits<Long> hits) {
        this.hits = hits;
        this.graph = graph;
    }

    public Iterator<Vertex> iterator() {
        return new VertexIterator();
    }


    public void close() {
        hits.close();
    }

    private class VertexIterator implements Iterator<Vertex> {
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
    }
}