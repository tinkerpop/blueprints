package com.tinkerpop.blueprints.pgm.impls.neo4j;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.impls.neo4j.util.Neo4jEdgeSequence;
import com.tinkerpop.blueprints.pgm.impls.neo4j.util.Neo4jVertexSequence;
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
    protected org.neo4j.graphdb.index.Index<S> neo4jIndex;

    public Neo4jIndex(final String indexName, Class<T> indexClass, final Neo4jGraph graph) {
        this.indexClass = indexClass;
        this.graph = graph;
        this.indexName = indexName;
        this.generateIndex();
    }

    public Class<T> getIndexClass() {
        return indexClass;
    }

    public String getIndexName() {
        return this.indexName;
    }

    public void put(final String key, final Object value, final T element) {
        this.graph.autoStartTransaction();
        this.neo4jIndex.add((S) element.getRawElement(), key, value);
        this.graph.autoStopTransaction(TransactionalGraph.Conclusion.SUCCESS);
    }

    public Iterable<T> get(final String key, final Object value) {
        //System.out.println("!!!" + this.neo4jIndex.get(key, value).size());
        IndexHits<S> itty = this.neo4jIndex.get(key, value);
        if (this.indexClass.isAssignableFrom(Neo4jVertex.class))
            return new Neo4jVertexSequence((Iterable<Node>) itty, this.graph);
        else
            return new Neo4jEdgeSequence((Iterable<Relationship>) itty, this.graph);
    }

    public void remove(final String key, final Object value, final T element) {
        this.graph.autoStartTransaction();
        this.neo4jIndex.remove((S) element.getRawElement(), key, value);
        this.graph.autoStopTransaction(TransactionalGraph.Conclusion.SUCCESS);
    }

    private void generateIndex() {
        /*Map<String, String> configuration = new HashMap<String, String>();
        configuration.put(Neo4jTokens.PROVIDER, Neo4jTokens.LUCENE);
        configuration.put(Neo4jTokens.TYPE, Neo4jTokens.EXACT);
        if (this instanceof AutomaticIndex) {
            configuration.put(Neo4jTokens.BLUEPRINTS_TYPE, Type.AUTOMATIC.toString());
        } else {
            configuration.put(Neo4jTokens.BLUEPRINTS_TYPE, Type.MANUAL.toString());
        }*/

        if (this.indexClass.isAssignableFrom(Neo4jVertex.class))
            this.neo4jIndex = (org.neo4j.graphdb.index.Index<S>) graph.getRawGraph().index().forNodes(this.indexName);
        else
            this.neo4jIndex = (org.neo4j.graphdb.index.Index<S>) graph.getRawGraph().index().forRelationships(this.indexName);

        if (this instanceof AutomaticIndex) {
            this.getIndexManager().setConfiguration(this.neo4jIndex, Neo4jTokens.BLUEPRINTS_TYPE, Type.AUTOMATIC.toString());
        } else {
            this.getIndexManager().setConfiguration(this.neo4jIndex, Neo4jTokens.BLUEPRINTS_TYPE, Type.MANUAL.toString());
        }

        //System.out.println(this.getIndexManager().getConfiguration(this.neo4jIndex));
        /*if(null != this.neo4jIndex.get("name","marko").getSingle()) {
            System.out.println(this.neo4jIndex.get("name","marko").getSingle().getProperty("name"));
        }*/
    }

    protected IndexManager getIndexManager() {
        return this.graph.getRawGraph().index();
    }
}
