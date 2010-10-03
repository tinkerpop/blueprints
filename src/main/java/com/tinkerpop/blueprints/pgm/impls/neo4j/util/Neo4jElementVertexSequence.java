package com.tinkerpop.blueprints.pgm.impls.neo4j.util;


import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jVertex;
import org.neo4j.graphdb.Node;

import java.util.Iterator;

/**
 * @author pangloss (http://github.com/pangloss)
 */
public class Neo4jElementVertexSequence implements Iterator<Element>, Iterable<Element> {

    private final Iterator<Node> nodes;
    private final Neo4jGraph graph;

    public Neo4jElementVertexSequence(final Iterable<Node> nodes, final Neo4jGraph graph) {
        this.graph = graph;
        this.nodes = nodes.iterator();
    }

    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public Element next() {
        return new Neo4jVertex(this.nodes.next(), this.graph);
    }

    public boolean hasNext() {
        return this.nodes.hasNext();
    }

    public Iterator<Element> iterator() {
        return this;
    }
}
