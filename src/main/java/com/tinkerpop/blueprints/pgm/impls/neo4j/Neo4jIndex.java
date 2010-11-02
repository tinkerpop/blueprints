package com.tinkerpop.blueprints.pgm.impls.neo4j;

import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.impls.neo4j.util.Neo4jEdgeSequence;
import com.tinkerpop.blueprints.pgm.impls.neo4j.util.Neo4jVertexSequence;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.IndexHits;


/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jIndex<T extends Neo4jElement, S extends PropertyContainer> implements Index<T> {

    private final Class<T> indexClass;
    private final Neo4jGraph graph;
    private final String indexName;

    public Neo4jIndex(final String indexName, Class<T> indexClass, final Neo4jGraph graph) {
        this.indexClass = indexClass;
        this.graph = graph;
        this.indexName = indexName;
    }

    public Class<T> getIndexClass() {
        return indexClass;
    }

    public String getIndexName() {
        return this.indexName;
    }

    public void put(final String key, final Object value, final T element) {
        this.graph.autoStartTransaction();
        this.generateIndex().add((S) element.getRawElement(), key, value);
        this.graph.autoStopTransaction(TransactionalGraph.Conclusion.SUCCESS);
    }

    public Iterable<T> get(final String key, final Object value) {
        IndexHits<S> itty = this.generateIndex().get(key, value);
        if (this.indexClass.isAssignableFrom(Neo4jVertex.class))
            return new Neo4jVertexSequence((Iterable<Node>) itty, this.graph);
        else
            return new Neo4jEdgeSequence((Iterable<Relationship>) itty, this.graph);
    }

    public void remove(final String key, final Object value, final T element) {
        this.graph.autoStartTransaction();
        this.generateIndex().remove((S) element.getRawElement(), key, value);
        this.graph.autoStopTransaction(TransactionalGraph.Conclusion.SUCCESS);
    }

    private org.neo4j.graphdb.index.Index<S> generateIndex() {
        if (this.indexClass.isAssignableFrom(Neo4jVertex.class))
            return (org.neo4j.graphdb.index.Index<S>) graph.getRawGraph().index().forNodes(this.indexName);
        else
            return (org.neo4j.graphdb.index.Index<S>) graph.getRawGraph().index().forRelationships(this.indexName);
    }
}
