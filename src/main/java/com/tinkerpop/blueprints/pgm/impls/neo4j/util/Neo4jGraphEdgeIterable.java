package com.tinkerpop.blueprints.pgm.impls.neo4j.util;


import org.neo4j.graphdb.Node;

import java.util.Iterator;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jGraphEdgeIterable implements Iterable<Edge> {

    Iterable<Node> nodes;
    Neo4jGraph graph;

    public Neo4jGraphEdgeIterable(final Iterable<Node> nodes, final Neo4jGraph graph) {
        this.nodes = nodes;
        this.graph = graph;
    }

    public Iterator<Edge> iterator() {
        return new Neo4jGraphEdgeIterator(this.nodes, this.graph);
    }
}
