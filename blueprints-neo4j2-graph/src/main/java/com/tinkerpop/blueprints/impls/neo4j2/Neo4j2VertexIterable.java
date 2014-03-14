package com.tinkerpop.blueprints.impls.neo4j2;


import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Vertex;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.IndexHits;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4j2VertexIterable<T extends Vertex> implements CloseableIterable<Neo4j2Vertex> {

    private final Iterable<Node> nodes;
    private final Neo4j2Graph graph;
    private final boolean checkTransaction;

    public Neo4j2VertexIterable(final Iterable<Node> nodes, final Neo4j2Graph graph, final boolean checkTransaction) {
        this.graph = graph;
        this.nodes = nodes;
        this.checkTransaction = checkTransaction;
    }

    public Neo4j2VertexIterable(final Iterable<Node> nodes, final Neo4j2Graph graph) {
        this(nodes, graph, false);
    }

    public Iterator<Neo4j2Vertex> iterator() {
        return new Iterator<Neo4j2Vertex>() {
            private final Iterator<Node> itty = nodes.iterator();
            private Node nextNode = null;

            public void remove() {
                this.itty.remove();
            }

            public Neo4j2Vertex next() {
                graph.autoStartTransaction(false);
                if (!checkTransaction) {
                    return new Neo4j2Vertex(this.itty.next(), graph);
                } else {
                    if (null != this.nextNode) {
                        final Node temp = this.nextNode;
                        this.nextNode = null;
                        return new Neo4j2Vertex(temp, graph);
                    } else {
                        while (true) {
                            final Node node = this.itty.next();
                            try {
                                if (!graph.nodeIsDeleted(node.getId())) return new Neo4j2Vertex(node, graph);
                            } catch (final IllegalStateException e) {
                                // tried to access a node not available to the transaction
                            }
                        }
                    }
                }
            }

            public boolean hasNext() {
                graph.autoStartTransaction(false);
                if (!checkTransaction)
                    return this.itty.hasNext();
                else {
                    if (null != this.nextNode)
                        return true;
                    else {
                        while (this.itty.hasNext()) {
                            final Node node = this.itty.next();
                            try {
                                if (!graph.nodeIsDeleted(node.getId())){
                                    this.nextNode = node;
                                    return true;
                                }
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