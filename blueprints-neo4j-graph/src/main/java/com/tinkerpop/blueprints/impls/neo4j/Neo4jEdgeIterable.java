package com.tinkerpop.blueprints.impls.neo4j;


import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Edge;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.IndexHits;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jEdgeIterable<T extends Edge> implements CloseableIterable<Neo4jEdge> {

    private final Iterable<Relationship> relationships;
    private final Neo4jGraph graph;
    private final boolean checkTransaction;
    private static final String DUMMY_PROPERTY = "a";

    public Neo4jEdgeIterable(final Iterable<Relationship> relationships, final Neo4jGraph graph, final boolean checkTransaction) {
        this.relationships = relationships;
        this.graph = graph;
        this.checkTransaction = checkTransaction;
    }

    public Neo4jEdgeIterable(final Iterable<Relationship> relationships, final Neo4jGraph graph) {
        this(relationships, graph, false);
    }

    public Iterator<Neo4jEdge> iterator() {
        return new Iterator<Neo4jEdge>() {
            private final Iterator<Relationship> itty = relationships.iterator();
            private Relationship nextRelationship = null;

            public void remove() {
                this.itty.remove();
            }

            public Neo4jEdge next() {
                if (!checkTransaction) {
                    return new Neo4jEdge(this.itty.next(), graph);
                } else {
                    if (null != this.nextRelationship) {
                        final Relationship temp = this.nextRelationship;
                        this.nextRelationship = null;
                        return new Neo4jEdge(temp, graph);
                    } else {
                        while (true) {
                            final Relationship relationship = this.itty.next();
                            try {
                                relationship.hasProperty(DUMMY_PROPERTY);
                                return new Neo4jEdge(relationship, graph);
                            } catch (final IllegalStateException e) {
                                // tried to access a relationship not available to the transaction
                            }
                        }
                    }
                }
            }

            public boolean hasNext() {
                if (!checkTransaction)
                    return this.itty.hasNext();
                else {
                    if (null != this.nextRelationship)
                        return true;
                    else {
                        while (this.itty.hasNext()) {
                            final Relationship relationship = this.itty.next();
                            try {
                                relationship.hasProperty(DUMMY_PROPERTY);
                                this.nextRelationship = relationship;
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
        if (this.relationships instanceof IndexHits) {
            ((IndexHits) this.relationships).close();
        }
    }
}