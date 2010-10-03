package com.tinkerpop.blueprints.pgm.impls.neo4j.util;


import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jEdge;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;
import org.neo4j.graphdb.Relationship;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jEdgeSequence implements Iterator<Edge>, Iterable<Edge> {

    private final Iterator<Relationship> relationships;
    private final Neo4jGraph graph;

    public Neo4jEdgeSequence(final Iterable<Relationship> relationships, final Neo4jGraph graph) {
        this.graph = graph;
        this.relationships = relationships.iterator();
    }

    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public Edge next() {
        return new Neo4jEdge(this.relationships.next(), this.graph);
    }

    public boolean hasNext() {
        return this.relationships.hasNext();
    }

    public Iterator<Edge> iterator() {
        return this;
    }
}