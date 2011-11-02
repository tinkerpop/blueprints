package com.tinkerpop.blueprints.pgm.impls.neo4j;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.CloseableSequence;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.impls.neo4j.util.Neo4jEdgeSequence;
import com.tinkerpop.blueprints.pgm.impls.neo4j.util.Neo4jVertexSequence;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;


/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jIndex<T extends Neo4jElement, S extends PropertyContainer> implements Index<T> {

    private final Class<T> indexClass;
    protected final Neo4jGraph graph;
    private final String indexName;
    protected org.neo4j.graphdb.index.Index<S> rawIndex;

    public Neo4jIndex(final String indexName, final Class<T> indexClass, final Neo4jGraph graph) {
        this.indexClass = indexClass;
        this.graph = graph;
        this.indexName = indexName;
        this.generateIndex();
    }

    public Type getIndexType() {
        return Type.MANUAL;
    }

    public Class<T> getIndexClass() {
        if (Vertex.class.isAssignableFrom(this.indexClass))
            return (Class) Vertex.class;
        else
            return (Class) Edge.class;
    }

    public String getIndexName() {
        return this.indexName;
    }

    public void put(final String key, final Object value, final T element) {
        try {
            this.graph.autoStartTransaction();
            this.rawIndex.add((S) element.getRawElement(), key, value);
            this.graph.autoStopTransaction(TransactionalGraph.Conclusion.SUCCESS);
        } catch (RuntimeException e) {
            this.graph.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw e;
        } catch (Exception e) {
            this.graph.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public CloseableSequence<T> get(final String key, final Object value) {
        final IndexHits<S> itty;
        if (value instanceof String && ((String) value).startsWith(Neo4jTokens.QUERY_HEADER)) {
            itty = this.rawIndex.query(key, ((String) value).substring(Neo4jTokens.QUERY_HEADER.length()));
        } else {
            itty = this.rawIndex.get(key, value);
        }
        if (this.indexClass.isAssignableFrom(Neo4jVertex.class))
            return new Neo4jVertexSequence((Iterable<Node>) itty, this.graph);
        else
            return new Neo4jEdgeSequence((Iterable<Relationship>) itty, this.graph);
    }

    public long count(final String key, final Object value) {
        return this.rawIndex.get(key, value).size();
    }

    public void remove(final String key, final Object value, final T element) {
        try {
            this.graph.autoStartTransaction();
            this.rawIndex.remove((S) element.getRawElement(), key, value);
            this.graph.autoStopTransaction(TransactionalGraph.Conclusion.SUCCESS);
        } catch (RuntimeException e) {
            this.graph.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw e;
        } catch (Exception e) {
            this.graph.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected void removeBasic(final String key, final Object value, final T element) {
        this.rawIndex.remove((S) element.getRawElement(), key, value);
    }

    protected void putBasic(final String key, final Object value, final T element) {
        this.rawIndex.add((S) element.getRawElement(), key, value);
    }

    private void generateIndex() {
        if (this.indexClass.isAssignableFrom(Neo4jVertex.class))
            this.rawIndex = (org.neo4j.graphdb.index.Index<S>) graph.getRawGraph().index().forNodes(this.indexName);
        else
            this.rawIndex = (org.neo4j.graphdb.index.Index<S>) graph.getRawGraph().index().forRelationships(this.indexName);

        final IndexManager manager = this.getIndexManager();
        final String storedType = manager.getConfiguration(this.rawIndex).get(Neo4jTokens.BLUEPRINTS_TYPE);
        if (null == storedType) {
            if (this instanceof AutomaticIndex) {
                this.getIndexManager().setConfiguration(this.rawIndex, Neo4jTokens.BLUEPRINTS_TYPE, Type.AUTOMATIC.toString());
            } else {
                this.getIndexManager().setConfiguration(this.rawIndex, Neo4jTokens.BLUEPRINTS_TYPE, Type.MANUAL.toString());
            }
        } else if (this.getIndexType() != Type.valueOf(storedType))
            throw new RuntimeException("Stored index is " + storedType + " and is being loaded as a " + this.getIndexType() + " index");
    }

    protected IndexManager getIndexManager() {
        return this.graph.getRawGraph().index();
    }

    public String toString() {
        return StringFactory.indexString(this);
    }
}
