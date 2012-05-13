package com.tinkerpop.blueprints.pgm.impls.neo4j;


import com.tinkerpop.blueprints.pgm.CloseableIterable;
import com.tinkerpop.blueprints.pgm.Vertex;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.IndexHits;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jVertexIterable<T extends Vertex> implements CloseableIterable<Neo4jVertex> {

    private final Iterable<Node> nodes;
    private final Neo4jGraph graph;
    private final boolean checkTransaction;
    private final String DUMMY = "a";

    public Neo4jVertexIterable(final Iterable<Node> nodes, final Neo4jGraph graph, final boolean checkTransaction) {
        this.graph = graph;
        this.nodes = nodes;
        this.checkTransaction = checkTransaction;
    }

    public Neo4jVertexIterable(final Iterable<Node> nodes, final Neo4jGraph graph) {
        this(nodes, graph, false);
    }

    public Iterator<Neo4jVertex> iterator() {
        return new Iterator<Neo4jVertex>() {
            private final Iterator<Node> itty = nodes.iterator();
            private Node nextNode = null;

            public void remove() {
                this.itty.remove();
            }

            public Neo4jVertex next() {
                if (!checkTransaction) {
                    return new Neo4jVertex(this.itty.next(), graph);
                } else {
                    if (null != this.nextNode)
                        return new Neo4jVertex(this.nextNode, graph);
                    while (true) {
                        final Node node = this.itty.next();
                        try {
                            node.hasProperty(DUMMY);
                            return new Neo4jVertex(node, graph);
                        } catch (final IllegalStateException e) {
                            // tried to access a relationship not available to the transaction
                        }
                    }
                }
            }

            public boolean hasNext() {
                if (!checkTransaction)
                    return this.itty.hasNext();
                else {
                    while (this.itty.hasNext()) {
                        final Node node = this.itty.next();
                        try {
                            node.hasProperty(DUMMY);
                            this.nextNode = node;
                            return true;
                        } catch (final IllegalStateException e) {
                        }
                    }
                    return false;
                }
            }
        };
    }

    public void close() {
        if (this.nodes instanceof IndexHits) {
            ((IndexHits) this.nodes).close();
        }
    }

}