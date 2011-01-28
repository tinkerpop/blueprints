package com.tinkerpop.blueprints.pgm.impls.neo4j.util;


import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jVertex;
import org.neo4j.graphdb.Node;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jVertexSequence<T extends Vertex> implements Iterator<Neo4jVertex>, Iterable<Neo4jVertex> {

    private final Iterator<Node> nodes;
    private final Neo4jGraph graph;

    public Neo4jVertexSequence(final Iterable<Node> nodes, final Neo4jGraph graph) {
        this.graph = graph;
        this.nodes = nodes.iterator();
    }

    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public Neo4jVertex next() {
        return new Neo4jVertex(this.nodes.next(), this.graph);
    }

    public boolean hasNext() {
        return this.nodes.hasNext();
    }

    public Iterator<Neo4jVertex> iterator() {
        return this;
    }
}