package com.tinkerpop.blueprints.pgm.impls.neo4j;

import com.tinkerpop.blueprints.pgm.CloseableIterable;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.Parameter;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.util.StringFactory;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;

import java.util.HashMap;
import java.util.Map;


/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jIndex<T extends Neo4jElement, S extends PropertyContainer> implements Index<T> {

    private final Class<T> indexClass;
    protected final Neo4jGraph graph;
    private final String indexName;
    protected org.neo4j.graphdb.index.Index<S> rawIndex;

    public Neo4jIndex(final String indexName, final Class<T> indexClass, final Neo4jGraph graph, final Parameter... indexParameters) {
        this.indexClass = indexClass;
        this.graph = graph;
        this.indexName = indexName;
        this.generateIndex(indexParameters);
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
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public CloseableIterable<T> get(final String key, final Object value) {
        final IndexHits<S> itty = this.rawIndex.get(key, value);
        if (this.indexClass.isAssignableFrom(Neo4jVertex.class))
            return new Neo4jVertexIterable((Iterable<Node>) itty, this.graph, true);
        else
            return new Neo4jEdgeIterable((Iterable<Relationship>) itty, this.graph, true);
    }

    public CloseableIterable<T> query(final String key, final Object query) {
        final IndexHits<S> itty = this.rawIndex.query(key, query);
        if (this.indexClass.isAssignableFrom(Neo4jVertex.class))
            return new Neo4jVertexIterable((Iterable<Node>) itty, this.graph, true);
        else
            return new Neo4jEdgeIterable((Iterable<Relationship>) itty, this.graph, true);
    }

    public long count(final String key, final Object value) {
        /*final IndexHits hits = this.rawIndex.get(key, value);
        final long count = hits.size();
        hits.close();
        return count;*/

        long count = 0;
        for (T t : this.get(key, value)) {
            count++;
        }
        return count;
    }

    public void remove(final String key, final Object value, final T element) {
        try {
            this.graph.autoStartTransaction();
            this.rawIndex.remove((S) element.getRawElement(), key, value);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void generateIndex(final Parameter<Object, Object>... indexParameters) {
        final IndexManager manager = this.graph.getRawGraph().index();
        if (Vertex.class.isAssignableFrom(this.indexClass)) {
            if (indexParameters.length > 0)
                this.rawIndex = (org.neo4j.graphdb.index.Index<S>) manager.forNodes(this.indexName, generateParameterMap(indexParameters));
            else
                this.rawIndex = (org.neo4j.graphdb.index.Index<S>) manager.forNodes(this.indexName);
        } else {
            if (indexParameters.length > 0)
                this.rawIndex = (org.neo4j.graphdb.index.Index<S>) manager.forRelationships(this.indexName, generateParameterMap(indexParameters));
            else
                this.rawIndex = (org.neo4j.graphdb.index.Index<S>) manager.forRelationships(this.indexName);
        }
    }

    public String toString() {
        return StringFactory.indexString(this);
    }

    private static Map<String, String> generateParameterMap(final Parameter<Object, Object>... indexParameters) {
        final Map<String, String> map = new HashMap<String, String>();
        for (final Parameter<Object, Object> parameter : indexParameters) {
            map.put(parameter.getKey().toString(), parameter.getValue().toString());
        }
        return map;
    }
}
