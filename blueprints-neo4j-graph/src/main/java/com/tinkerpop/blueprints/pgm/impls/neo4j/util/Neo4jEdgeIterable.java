package com.tinkerpop.blueprints.pgm.impls.neo4j.util;


import com.tinkerpop.blueprints.pgm.CloseableIterable;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jEdge;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.IndexHits;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jEdgeIterable<T extends Edge> implements CloseableIterable<Neo4jEdge> {

    private final Iterable<Relationship> relationships;
    private final Neo4jGraph graph;

    public Neo4jEdgeIterable(final Iterable<Relationship> relationships, final Neo4jGraph graph) {
        this.relationships = relationships;
        this.graph = graph;
    }

    public Iterator<Neo4jEdge> iterator() {
        return new Neo4jEdgeIterator();
    }

    public void close() {
        if (this.relationships instanceof IndexHits) {
            ((IndexHits) this.relationships).close();
        }
    }

    private class Neo4jEdgeIterator implements Iterator<Neo4jEdge> {

        private final Iterator<Relationship> itty = relationships.iterator();

        public void remove() {
            this.itty.remove();
        }

        public Neo4jEdge next() {
            return new Neo4jEdge(this.itty.next(), graph);
        }

        public boolean hasNext() {
            return this.itty.hasNext();
        }

    }
}