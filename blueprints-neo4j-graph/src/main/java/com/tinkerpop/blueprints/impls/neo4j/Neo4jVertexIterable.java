package com.tinkerpop.blueprints.impls.neo4j;


import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Vertex;
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
    private static final String DUMMY_PROPERTY = "a";

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
                    if (null != this.nextNode) {
                        final Node temp = this.nextNode;
                        this.nextNode = null;
                        return new Neo4jVertex(temp, graph);
                    } else {
                        while (true) {
                            final Node node = this.itty.next();
                            try {
                                node.hasProperty(DUMMY_PROPERTY);
                                return new Neo4jVertex(node, graph);
                            } catch (final IllegalStateException e) {
                                // tried to access a node not available to the transaction
                            }
                        }
                    }
                }
            }

            public boolean hasNext() {
                if (!checkTransaction)
                    return this.itty.hasNext();
                else {
                    if (null != this.nextNode)
                        return true;
                    else {
                        while (this.itty.hasNext()) {
                            final Node node = this.itty.next();
                            try {
                                node.hasProperty(DUMMY_PROPERTY);
                                this.nextNode = node;
                                return true;
                            } catch (final IllegalStateException e) {
                            }
                        }
                        return false;
                    }
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