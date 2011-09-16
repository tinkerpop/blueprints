package com.tinkerpop.blueprints.pgm.impls.neo4jbatch.util;

import com.tinkerpop.blueprints.pgm.CloseableSequence;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.impls.neo4jbatch.Neo4jBatchGraph;
import org.neo4j.graphdb.index.IndexHits;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class ElementCloseableSequence<T extends Element> implements CloseableSequence<T> {

    protected final IndexHits<Long> hits;
    protected final Neo4jBatchGraph graph;

    public ElementCloseableSequence(final Neo4jBatchGraph graph, final IndexHits<Long> hits) {
        this.hits = hits;
        this.graph = graph;
    }

    public Iterator<T> iterator() {
        return this;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public boolean hasNext() {
        return this.hits.hasNext();
    }

    public abstract T next();

    public void close() {
        this.hits.close();
    }
}