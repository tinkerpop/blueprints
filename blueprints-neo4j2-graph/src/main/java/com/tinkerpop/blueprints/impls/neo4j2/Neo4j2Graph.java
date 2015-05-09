package com.tinkerpop.blueprints.impls.neo4j2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationConverter;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.TransactionFailureException;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.AutoIndexer;
import org.neo4j.graphdb.index.RelationshipIndex;
import org.neo4j.helpers.Settings;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.kernel.impl.core.NodeManager;
import org.neo4j.tooling.GlobalGraphOperations;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.MetaGraph;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.neo4j2.index.Neo4j2EdgeIndex;
import com.tinkerpop.blueprints.impls.neo4j2.index.Neo4j2VertexIndex;
import com.tinkerpop.blueprints.impls.neo4j2.iterate.Neo4j2EdgeIterable;
import com.tinkerpop.blueprints.impls.neo4j2.iterate.Neo4j2VertexIterable;
import com.tinkerpop.blueprints.util.DefaultGraphQuery;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import com.tinkerpop.blueprints.util.KeyIndexableGraphHelper;
import com.tinkerpop.blueprints.util.PropertyFilteredIterable;
import com.tinkerpop.blueprints.util.StringFactory;

/**
 * A Blueprints implementation of the graph database Neo4j (http://neo4j.org)
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4j2Graph implements TransactionalGraph, IndexableGraph, KeyIndexableGraph, MetaGraph<GraphDatabaseService> {
	
    private static final Logger logger = Logger.getLogger(Neo4j2Graph.class.getName());
    
    public static GraphDatabaseBuilder createGraphDatabaseBuilder(String directory, Map<String, String> configuration){
        GraphDatabaseBuilder builder = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(directory);
        if (null != configuration){
                for(String key: configuration.keySet()){
                        builder.setConfig(Settings.setting(key, Settings.STRING, (String) null), configuration.get(key));
                }
        }
        return builder;
    }
    
    //=========================================================================
    // Element wrapper
    
    public static interface ElementWrapper<T extends Element, S extends PropertyContainer> {
    	public T wrap(S rawElement);
    }
    
    public static interface VertexWrapper<V extends Vertex> extends ElementWrapper<V, Node>{
    }
    
    public static interface EdgeWrapper<E extends Edge>  extends ElementWrapper<E, Relationship>{
    }
    
    
    private static VertexWrapper<Neo4j2Vertex> createDefaultVertexWrapper(final Neo4j2Graph graph){
    	return new VertexWrapper<Neo4j2Vertex>() {
    		@Override
			public Neo4j2Vertex wrap(Node rawVertex) {
				return new Neo4j2Vertex(rawVertex, graph);
			}
		};
    }
    
    private static EdgeWrapper<Neo4j2Edge> createDefaultEdgeWrapper(final Neo4j2Graph graph){
    	return new EdgeWrapper<Neo4j2Edge>() {
    		@Override
			public Neo4j2Edge wrap(Relationship rawEdge) {
				return new Neo4j2Edge(rawEdge, graph);
			}
		};
    }
    
    
  //=========================================================================

    private GraphDatabaseService rawGraph;
    private Neo4j2GraphInternalIndexKeys indexKeys;
    private VertexWrapper<? extends Vertex> vertexWrapper;
    private EdgeWrapper<? extends Edge> edgeWrapper;

    protected final ThreadLocal<Transaction> tx = new ThreadLocal<Transaction>() {
        protected Transaction initialValue() {
            return null;
        }
    };

    private static final Features FEATURES = new Features();

    static {

        FEATURES.supportsSerializableObjectProperty = false;
        FEATURES.supportsBooleanProperty = true;
        FEATURES.supportsDoubleProperty = true;
        FEATURES.supportsFloatProperty = true;
        FEATURES.supportsIntegerProperty = true;
        FEATURES.supportsPrimitiveArrayProperty = true;
        FEATURES.supportsUniformListProperty = true;
        FEATURES.supportsMixedListProperty = false;
        FEATURES.supportsLongProperty = true;
        FEATURES.supportsMapProperty = false;
        FEATURES.supportsStringProperty = true;

        FEATURES.supportsDuplicateEdges = true;
        FEATURES.supportsSelfLoops = true;
        FEATURES.isPersistent = true;
        FEATURES.isWrapper = false;
        FEATURES.supportsVertexIteration = true;
        FEATURES.supportsEdgeIteration = true;
        FEATURES.supportsVertexIndex = true;
        FEATURES.supportsEdgeIndex = true;
        FEATURES.ignoresSuppliedIds = true;
        FEATURES.supportsTransactions = true;
        FEATURES.supportsIndices = true;
        FEATURES.supportsKeyIndices = true;
        FEATURES.supportsVertexKeyIndex = true;
        FEATURES.supportsEdgeKeyIndex = true;
        FEATURES.supportsEdgeRetrieval = true;
        FEATURES.supportsVertexProperties = true;
        FEATURES.supportsEdgeProperties = true;
        FEATURES.supportsThreadedTransactions = false;
        FEATURES.supportsThreadIsolatedTransactions = true;
    }

    /**
     * @deprecated since Blueprints 2.7.0/Neo4j 2.2.x this method is
     * no longer required since Neo4j indexes no longer return
     * deleted elements. It will always return false.
     */
    @Deprecated
    protected boolean checkElementsInTransaction() {
        return false;
    }

    /**
     * @deprecated since Blueprints 2.7.0/Neo4j 2.2.x this method is
     * no longer required - Neo4j indexes no longer return deleted elements.
     * 
     * This method is now a no-op.
     */
    @Deprecated
    public void setCheckElementsInTransaction(final boolean checkElementsInTransaction) {
    }

    
    public Neo4j2Graph(final String directory) {
        this(directory, null);
    }
    
    public Neo4j2Graph(final Configuration configuration) {
        this(configuration.getString("blueprints.neo4j.directory", null),
                ConfigurationConverter.getMap(configuration.subset("blueprints.neo4j.conf")));
    }
    
    public Neo4j2Graph(final String directory, final Map<String, String> configuration) {
    	this(createGraphDatabaseBuilder(directory, configuration).newGraphDatabase());
    }

    public Neo4j2Graph(final GraphDatabaseService rawGraph) {
    	try{
    		this.rawGraph = rawGraph;
    		this.indexKeys = new Neo4j2GraphInternalIndexKeys(this.rawGraph);
    		this.vertexWrapper = createDefaultVertexWrapper(this);
    		this.edgeWrapper = createDefaultEdgeWrapper(this);
            init();
    	} catch (Exception e) {
          if (this.rawGraph != null)
              this.rawGraph.shutdown();
          throw new RuntimeException(e.getMessage(), e);
      } 
    }


    public VertexWrapper<? extends Vertex> getVertexWrapper() {
		return vertexWrapper;
	}
    
    public EdgeWrapper<? extends Edge> getEdgeWrapper() {
		return edgeWrapper;
	}
    
    public void setVertexWrapper(VertexWrapper<? extends Vertex> vertexWrapper) {
		this.vertexWrapper = vertexWrapper;
	}
    
    public void setEdgeWrapper(EdgeWrapper<? extends Edge> edgeWrapper) {
		this.edgeWrapper = edgeWrapper;
	}
    
    protected void init() {
        this.loadKeyIndices();
        this.commit();
        
    }

    private void loadKeyIndices() {
        this.autoStartTransaction(true);
        for (final String key : this.indexKeys.getKeys(Vertex.class)) {
            this.createKeyIndex(key, Vertex.class);
        }
        for (final String key : this.indexKeys.getKeys(Edge.class)) {
            this.createKeyIndex(key, Edge.class);
        }
        this.commit();
    }
    
    /**
     * Helper method, here only to support the existing methods that pass that Class<T> as an argument.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	private <T extends Element> Index<T> _createIndex(final String indexName, final Class<T> indexClass, final Parameter... indexParameters) {
    	if (Vertex.class.isAssignableFrom(indexClass)) {
        	return (Index<T>) new Neo4j2VertexIndex(indexName, this, indexParameters);
        } else {
        	return (Index<T>) new Neo4j2EdgeIndex(indexName, this, indexParameters);
        }
    }
    	

    @SuppressWarnings("rawtypes")
	@Override
    public synchronized <T extends Element> Index<T> createIndex(final String indexName, final Class<T> indexClass, final Parameter... indexParameters) {
        this.autoStartTransaction(true);
        if (this.rawGraph.index().existsForNodes(indexName) || this.rawGraph.index().existsForRelationships(indexName)) {
            throw ExceptionFactory.indexAlreadyExists(indexName);
        }
        return _createIndex(indexName, indexClass, indexParameters);
    }

    public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
        this.autoStartTransaction(false);
        if (Vertex.class.isAssignableFrom(indexClass)) {
            if (this.rawGraph.index().existsForNodes(indexName)) {
                return _createIndex(indexName, indexClass);
            } else if (this.rawGraph.index().existsForRelationships(indexName)) {
                throw ExceptionFactory.indexDoesNotSupportClass(indexName, indexClass);
            } else {
                return null;
            }
        } else if (Edge.class.isAssignableFrom(indexClass)) {
            if (this.rawGraph.index().existsForRelationships(indexName)) {
            	return _createIndex(indexName, indexClass);
            } else if (this.rawGraph.index().existsForNodes(indexName)) {
                throw ExceptionFactory.indexDoesNotSupportClass(indexName, indexClass);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Note that this method will force a successful closing of the current
     * thread's transaction. As such, once the index is dropped, the operation
     * is committed.
     *
     * @param indexName the name of the index to drop
     */
    public synchronized void dropIndex(final String indexName) {
        this.autoStartTransaction(true);
        if (this.rawGraph.index().existsForNodes(indexName)) {
            org.neo4j.graphdb.index.Index<Node> nodeIndex = this.rawGraph.index().forNodes(indexName);
            if (nodeIndex.isWriteable()) {
                nodeIndex.delete();
            }
        } else if (this.rawGraph.index().existsForRelationships(indexName)) {
            RelationshipIndex relationshipIndex = this.rawGraph.index().forRelationships(indexName);
            if (relationshipIndex.isWriteable()) {
                relationshipIndex.delete();
            }
        }
        this.commit();
    }

    public Iterable<Index<? extends Element>> getIndices() {
        this.autoStartTransaction(false);
        final List<Index<? extends Element>> indices = new ArrayList<Index<? extends Element>>();
        for (final String name : this.rawGraph.index().nodeIndexNames()) {
            if (!name.equals(Neo4j2Tokens.NODE_AUTO_INDEX))
            	indices.add(new Neo4j2VertexIndex(name, this));
        }
        for (final String name : this.rawGraph.index().relationshipIndexNames()) {
            if (!name.equals(Neo4j2Tokens.RELATIONSHIP_AUTO_INDEX))
            	indices.add(new Neo4j2EdgeIndex(name, this));
        }
        return indices;
    }

    public Neo4j2Vertex addVertex(final Object id) {
        this.autoStartTransaction(true);
        return new Neo4j2Vertex(this.rawGraph.createNode(), this);
    }

    public Neo4j2Vertex getVertex(final Object id) {
        this.autoStartTransaction(false);

        if (null == id)
            throw ExceptionFactory.vertexIdCanNotBeNull();

        try {
            final Long longId;
            if (id instanceof Long)
                longId = (Long) id;
            else if (id instanceof Number)
                longId = ((Number) id).longValue();
            else
                longId = Double.valueOf(id.toString()).longValue();
            return new Neo4j2Vertex(this.rawGraph.getNodeById(longId), this);
        } catch (NotFoundException e) {
            return null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * @return all the vertices in the graph
     */
    public Iterable<Vertex> getVertices() {
        this.autoStartTransaction(false);
        return new Neo4j2VertexIterable(GlobalGraphOperations.at(rawGraph).getAllNodes(), this);
    }
    
    
    public Iterable<Vertex> getVertices(final String label) {
    	Iterable<Node> nodes = new Iterable<Node>() {
			@Override
			public Iterator<Node> iterator() {
				return rawGraph.findNodes(DynamicLabel.label(label));
			}
		};
		return new Neo4j2VertexIterable(nodes , this);
    }

    public Iterable<Vertex> getVertices(final String key, final Object value) {
        this.autoStartTransaction(false);
        final AutoIndexer<?> indexer = this.rawGraph.index().getNodeAutoIndexer();
        if (indexer.isEnabled() && indexer.getAutoIndexedProperties().contains(key))
            return new Neo4j2VertexIterable(this.rawGraph.index().getNodeAutoIndexer().getAutoIndex().get(key, value), this);
        else
            return new PropertyFilteredIterable<Vertex>(key, value, this.getVertices());
    }

    /**
     * @return all the edges in the graph
     */
    public Iterable<Edge> getEdges() {
        this.autoStartTransaction(false);
        return new Neo4j2EdgeIterable(GlobalGraphOperations.at(rawGraph).getAllRelationships(), this);
    }

    public Iterable<Edge> getEdges(final String key, final Object value) {
        this.autoStartTransaction(false);
        final AutoIndexer<?> indexer = this.rawGraph.index().getRelationshipAutoIndexer();
        if (indexer.isEnabled() && indexer.getAutoIndexedProperties().contains(key))
            return new Neo4j2EdgeIterable(this.rawGraph.index().getRelationshipAutoIndexer().getAutoIndex().get(key, value), this);
        else
            return new PropertyFilteredIterable<Edge>(key, value, this.getEdges());
    }
    
    
    @Override
	public <T extends Element> Set<String> getIndexedKeys(Class<T> elementClass) {
    	this.autoStartTransaction(false);
    	AutoIndexer<?> indexer = getAutoIndexer(elementClass);
    	if(indexer.isEnabled()){
    		return indexer.getAutoIndexedProperties();
    	} else {
    		return Collections.emptySet();
    	}
	}
    
    @Override
    public <T extends Element> void dropKeyIndex(final String key, final Class<T> elementClass) {
        this.autoStartTransaction(true);
        AutoIndexer<?> autoIndexer = getAutoIndexer(elementClass);
        if (!autoIndexer.isEnabled()){
        	return;
        }
        autoIndexer.stopAutoIndexingProperty(key);
        this.indexKeys.removeKey(key, elementClass);
    }

    @SuppressWarnings("rawtypes")
	@Override
    public <T extends Element> void createKeyIndex(final String key, final Class<T> elementClass, final Parameter... indexParameters) {
    	this.autoStartTransaction(true);
        AutoIndexer<?> indexer = getAutoIndexer(elementClass);
        
        if(indexer.isEnabled() && indexer.getAutoIndexedProperties().contains(key)){
        	return;
        }
        
        if (! indexer.isEnabled()){
            indexer.setEnabled(true);
        }
        indexer.startAutoIndexingProperty(key);
    	Iterable<? extends Element> elements = Vertex.class.isAssignableFrom(elementClass) ? this.getVertices() : this.getEdges();
        KeyIndexableGraphHelper.reIndexElements(this, elements, new HashSet<String>(Arrays.asList(key)));
        this.autoStartTransaction(true);
        this.indexKeys.addKey(key, elementClass);
    }
    
    private <T extends Element> AutoIndexer<?> getAutoIndexer(final Class<T> elementClass){
    	if (elementClass == null){
    		throw ExceptionFactory.classForElementCannotBeNull();
    	} else if (Vertex.class.isAssignableFrom(elementClass)) {
    		return this.rawGraph.index().getNodeAutoIndexer();
    	} else if (Edge.class.isAssignableFrom(elementClass)) {
    		return this.rawGraph.index().getRelationshipAutoIndexer();
    	} else {
    		throw ExceptionFactory.classIsNotIndexable(elementClass);
    	}
    }

    public void removeVertex(final Vertex vertex) {
        this.autoStartTransaction(true);

        try {
            final Node node = ((Neo4j2Vertex) vertex).getRawElement();
            for (final Relationship relationship : node.getRelationships(org.neo4j.graphdb.Direction.BOTH)) {
                relationship.delete();
            }
            node.delete();
        } catch (NotFoundException nfe) {
            throw ExceptionFactory.vertexWithIdDoesNotExist(vertex.getId());
        } catch (IllegalStateException ise) {
            // wrap the neo4j exception so that the message is consistent in blueprints.
            throw ExceptionFactory.vertexWithIdDoesNotExist(vertex.getId());
        }
    }

    public Neo4j2Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        if (label == null)
            throw ExceptionFactory.edgeLabelCanNotBeNull();

        this.autoStartTransaction(true);
        return new Neo4j2Edge(((Neo4j2Vertex) outVertex).getRawElement().createRelationshipTo(((Neo4j2Vertex) inVertex).getRawElement(),
                DynamicRelationshipType.withName(label)), this);
    }

    public Neo4j2Edge getEdge(final Object id) {
        if (null == id)
            throw ExceptionFactory.edgeIdCanNotBeNull();

        this.autoStartTransaction(false);
        try {
            final Long longId;
            if (id instanceof Long)
                longId = (Long) id;
            else
                longId = Double.valueOf(id.toString()).longValue();
            return new Neo4j2Edge(this.rawGraph.getRelationshipById(longId), this);
        } catch (NotFoundException e) {
            return null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void removeEdge(final Edge edge) {
        this.autoStartTransaction(true);
        ((Relationship) ((Neo4j2Edge) edge).getRawElement()).delete();
    }

    
    @SuppressWarnings("deprecation")
	@Override
    public void stopTransaction(Conclusion conclusion) {
        if (Conclusion.SUCCESS == conclusion)
            commit();
        else
            rollback();
    }

    public void commit() {
        if (null == tx.get()) {
            return;
        }

        try {
            tx.get().success();
        } finally {
            tx.get().close();
            tx.remove();
        }
    }

    public void rollback() {
        if (null == tx.get()) {
            return;
        }

        try {
            tx.get().failure();
        } finally {
            tx.get().close();
            tx.remove();
        }
    }

    public void shutdown() {
        try {
            this.commit();
        } catch (TransactionFailureException e) {
            logger.warning("Failure on shutdown "+e.getMessage());
            // TODO: inspect why certain transactions fail
        }
        this.rawGraph.shutdown();
    }

    // The forWrite flag is true when the autoStartTransaction method is
    // called before any operation which will modify the graph in any way. It
    // is not used in this simple implementation but is required in subclasses
    // which enforce transaction rules. Now that Neo4j reads also require a
    // transaction to be open it is otherwise impossible to tell the difference
    // between the beginning of a write operation and the beginning of a read
    // operation.
    public void autoStartTransaction(boolean forWrite) {
        if (tx.get() == null)
            tx.set(this.rawGraph.beginTx());
    }

    public GraphDatabaseService getRawGraph() {
        return this.rawGraph;
    }

    public Features getFeatures() {
        return FEATURES;
    }

    public String toString() {
        return StringFactory.graphString(this, this.rawGraph.toString());
    }

    public GraphQuery query() {
        return new DefaultGraphQuery(this);
    }

    public Iterator<Map<String,Object>> query(String query, Map<String,Object> params) {
        return rawGraph.execute(query,params==null ? Collections.<String,Object>emptyMap() : params);
    }
    
    
    /**
     * A class that encapsulates some deprecated method calls, 
     * and other "hackish" bits, leftover from previous implementation. 
     * 
     * @author Joey Freund
     */
    private class Neo4j2GraphInternalIndexKeys {

    	private GraphDatabaseService rawGraph;
    	
    	public Neo4j2GraphInternalIndexKeys(GraphDatabaseService rawGraph) {
    		this.rawGraph = rawGraph;
    	}
    	
    	private <T extends Element> String getIndexKeysPropertyName(final Class<T> elementClass){
        	return elementClass.getSimpleName() + ":indexed_keys";
        }
    	
    	public <T extends Element> void addKey(String key, Class<T> elementClass){
    		Set<String> keys = getKeys(elementClass);
    		keys.add(key);
    		setKeys(elementClass, keys);
    	}

    	public <T extends Element> void removeKey(String key, Class<T> elementClass){
    		try {
    			Set<String> keys = getKeys(elementClass);
    			keys.remove(key);
    			setKeys(elementClass, keys);
    		} catch (Exception e) {
    			// no indexed_keys kernel data property
    		}
    	}

    	public <T extends Element> Set<String> getKeys(Class<T> elementClass){
    		try {
    			PropertyContainer pc = tryToGetGraphProperties();
    			final String[] keys = (String[]) pc.getProperty(getIndexKeysPropertyName(elementClass));
    			return new HashSet<String>(Arrays.asList(keys));
    		} catch (Exception e) {
    			return new HashSet<String>();
    		}
    	}
    	
    	private <T extends Element> void setKeys(Class<T> elementClass, Set<String> keys){
    		PropertyContainer pc = getGraphProperties();
    		pc.setProperty(getIndexKeysPropertyName(elementClass), keys.toArray(new String[keys.size()]));
    	}
    	
    	private PropertyContainer getGraphProperties() {
    		if (rawGraph instanceof GraphDatabaseAPI) {
    			return ((GraphDatabaseAPI) this.rawGraph).getDependencyResolver().resolveDependency(NodeManager.class).newGraphProperties();
            } else {
            	logNotGraphDatabaseAPI();
                throw new UnsupportedOperationException("Cannot get graph properties of a non-GraphDatabaseAPI graph");
            }
        }
    	
    	private PropertyContainer tryToGetGraphProperties() {
    		try{
    			return getGraphProperties();		
    		} catch (UnsupportedOperationException e){
    			return null;
    		}
    	}
    	
    	private void logNotGraphDatabaseAPI() {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, "Indices are not available on non-GraphDatabaseAPI instances" +
                        " Current graph class is " + rawGraph.getClass().getName());
            }
        }
    	
    }

}
