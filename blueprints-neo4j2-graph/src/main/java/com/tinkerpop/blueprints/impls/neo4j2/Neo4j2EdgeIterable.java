package com.tinkerpop.blueprints.impls.neo4j2;


import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Edge;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.IndexHits;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4j2EdgeIterable<T extends Edge> implements CloseableIterable<Neo4j2Edge> {

    private final Iterable<Relationship> relationships;
    private final Neo4j2Graph graph;
    private final boolean checkTransaction;

    public Neo4j2EdgeIterable(final Iterable<Relationship> relationships, final Neo4j2Graph graph, final boolean checkTransaction) {
        this.relationships = relationships;
        this.graph = graph;
        this.checkTransaction = checkTransaction;
    }

    public Neo4j2EdgeIterable(final Iterable<Relationship> relationships, final Neo4j2Graph graph) {
        this(relationships, graph, false);
    }

    public Iterator<Neo4j2Edge> iterator() {
        return new Iterator<Neo4j2Edge>() {
            private final Iterator<Relationship> itty = relationships.iterator();
            private Relationship nextRelationship = null;

            public void remove() {
                graph.autoStartTransaction(true);
                this.itty.remove();
            }

            public Neo4j2Edge next() {
                graph.autoStartTransaction(false);
                if (!checkTransaction) {
                    return new Neo4j2Edge(this.itty.next(), graph);
                } else {
                    if (null != this.nextRelationship) {
                        final Relationship temp = this.nextRelationship;
                        this.nextRelationship = null;
                        return new Neo4j2Edge(temp, graph);
                    } else {
                        while (true) {
                            final Relationship relationship = this.itty.next();
                            try {
                                if (!graph.relationshipIsDeleted(relationship.getId())) {
                                    return new Neo4j2Edge(relationship, graph);
                                }
                            } catch (final IllegalStateException e) {
                                // tried to access a relationship not available to the transaction
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
                    if (null != this.nextRelationship)
                        return true;
                    else {
                        while (this.itty.hasNext()) {
                            final Relationship relationship = this.itty.next();
                            try {
                                if (!graph.relationshipIsDeleted(relationship.getId())) {
                                    this.nextRelationship = relationship;
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
        if (this.relationships instanceof IndexHits) {
            ((IndexHits) this.relationships).close();
        }
    }
}