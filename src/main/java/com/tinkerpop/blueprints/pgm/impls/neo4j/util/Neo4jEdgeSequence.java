package com.tinkerpop.blueprints.pgm.impls.neo4j.util;


import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jEdge;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;
import org.neo4j.graphdb.Relationship;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jEdgeSequence<T extends Edge> implements Iterator<Neo4jEdge>, Iterable<Neo4jEdge> {

    private final Iterator<Relationship> relationships;
    private final Neo4jGraph graph;

    public Neo4jEdgeSequence(final Iterable<Relationship> relationships, final Neo4jGraph graph) {
        this.graph = graph;
        this.relationships = relationships.iterator();
    }

    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public Neo4jEdge next() {
        return new Neo4jEdge(this.relationships.next(), this.graph);
    }

    public boolean hasNext() {
        return this.relationships.hasNext();
    }

    public Iterator<Neo4jEdge> iterator() {
        return this;
    }
}