package com.tinkerpop.blueprints.util.wrappers.batch;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Query;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import com.tinkerpop.blueprints.util.StringFactory;
import com.tinkerpop.blueprints.util.wrappers.WrapperGraph;
import com.tinkerpop.blueprints.util.wrappers.batch.cache.LongIDVertexCache;
import com.tinkerpop.blueprints.util.wrappers.batch.cache.ObjectIDVertexCache;
import com.tinkerpop.blueprints.util.wrappers.batch.cache.StringIDVertexCache;
import com.tinkerpop.blueprints.util.wrappers.batch.cache.URLCompression;
import com.tinkerpop.blueprints.util.wrappers.batch.cache.VertexCache;
import com.tinkerpop.blueprints.util.wrappers.id.IdGraph;

import java.util.Iterator;
import java.util.Set;

/**
 * BatchGraph is a wrapper that enables batch loading of a large number of edges and vertices by chunking the entire
 * load into smaller batches and maintaining a memory-efficient vertex cache so that the entire transactional state can
 * be flushed after each chunk is loaded.
 * <br />
 * BatchGraph is ONLY meant for loading data and does not support any retrieval or removal operations.
 * That is, BatchGraph only supports the following methods:
 * - {@link #addVertex(Object)} for adding vertices
 * - {@link #addEdge(Object, com.tinkerpop.blueprints.Vertex, com.tinkerpop.blueprints.Vertex, String)} for adding edges
 * - {@link #getVertex(Object)} to be used when adding edges
 * - Property getter, setter and removal methods for vertices and edges.
 * <br />
 * An important limitation of BatchGraph is that edge properties can only be set immediately after the edge has been added.
 * If other vertices or edges have been created in the meantime, setting, getting or removing properties will throw
 * exceptions. This is done to avoid caching of edges which would require a great amount of memory.
 * <br />
 * BatchGraph wraps {@link TransactionalGraph}. To wrap arbitrary graphs, use {@link #wrap(com.tinkerpop.blueprints.Graph)}
 * which will additionally wrap non-transactional.
 * <br />
 * BatchGraph can also automatically set the provided element ids as properties on the respective element. Use
 * {@link #setVertexIdKey(String)} and {@link #setEdgeIdKey(String)} to set the keys for the vertex and edge properties
 * respectively. This allows to make the loaded baseGraph compatible for later wrapping with {@link IdGraph}.
 *
 * @author Matthias Broecheler (http://www.matthiasb.com)
 */

public class BatchGraph<T extends TransactionalGraph> implements TransactionalGraph, WrapperGraph<T> {

    /**
     * Default buffer size
     */
    public static final long DEFAULT_BUFFER_SIZE = 100000;

    /**
     * Type of vertex ids expected by BatchGraph. The default is IdType.OBJECT.
     * Use the IdType that best matches the used vertex id types in order to save memory.
     */
    public static enum IdType {

        OBJECT, NUMBER, STRING, URL;

        private VertexCache getVertexCache(Graph g) {
            switch (this) {
                case OBJECT:
                    return new ObjectIDVertexCache(g);
                case NUMBER:
                    return new LongIDVertexCache(g);
                case STRING:
                    return new StringIDVertexCache(g);
                case URL:
                    return new StringIDVertexCache(g, new URLCompression());
                default:
                    throw new IllegalArgumentException("Unrecognized ID type: " + this);
            }
        }

    }

    private final T baseGraph;

    private String vertexIdKey = null;
    private String edgeIdKey = null;
    private boolean loadingFromScratch = true;

    private final VertexCache cache;

    private long bufferSize = DEFAULT_BUFFER_SIZE;
    private long remainingBufferSize;

    private BatchEdge currentEdge = null;
    private Edge currentEdgeCached = null;

    /**
     * Constructs a BatchGraph wrapping the provided baseGraph, using the specified buffer size and expecting vertex ids of
     * the specified IdType. Supplying vertex ids which do not match this type will throw exceptions.
     *
     * @param graph      Graph to be wrapped
     * @param type       Type of vertex id expected. This information is used to optimize the vertex cache memory footprint.
     * @param bufferSize Defines the number of vertices and edges loaded before starting a new transaction. The larger this value, the more memory is required but the faster the loading process.
     */
    public BatchGraph(final T graph, final IdType type, final long bufferSize) {
        if (graph == null) throw new IllegalArgumentException("Graph may not be null");
        if (type == null) throw new IllegalArgumentException("Type may not be null");
        if (bufferSize <= 0) throw new IllegalArgumentException("BufferSize must be positive");
        this.baseGraph = graph;
        this.bufferSize = bufferSize;

        vertexIdKey = null;
        edgeIdKey = null;

        cache = type.getVertexCache(this.baseGraph);

        remainingBufferSize = this.bufferSize;
    }

    /**
     * Constructs a BatchGraph wrapping the provided baseGraph.
     *
     * @param graph Graph to be wrapped
     */
    public BatchGraph(final T graph) {
        this(graph, IdType.OBJECT, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Constructs a BatchGraph wrapping the provided baseGraph. Immediately returns the baseGraph if its a BatchGraph
     * and wraps non-transactional graphs in an additional {@link WritethroughGraph}.
     *
     * @param graph Graph to be wrapped
     */
    public static BatchGraph wrap(final Graph graph) {
        if (graph instanceof BatchGraph) return (BatchGraph) graph;
        else if (graph instanceof TransactionalGraph) return new BatchGraph((TransactionalGraph) graph);
        else return new BatchGraph(new WritethroughGraph(graph));
    }

    /**
     * Constructs a BatchGraph wrapping the provided baseGraph. Immediately returns the baseGraph if its a BatchGraph
     * and wraps non-transactional graphs in an additional {@link WritethroughGraph}.
     *
     * @param graph  Graph to be wrapped
     * @param buffer Size of the buffer
     */
    public static BatchGraph wrap(final Graph graph, final long buffer) {
        if (graph instanceof BatchGraph) return (BatchGraph) graph;
        else if (graph instanceof TransactionalGraph)
            return new BatchGraph((TransactionalGraph) graph, IdType.OBJECT, buffer);
        else return new BatchGraph(new WritethroughGraph(graph), IdType.OBJECT, buffer);
    }

    /**
     * Sets the key to be used when setting the vertex id as a property on the respective vertex.
     * If the key is null, then no property will be set.
     * If the loaded baseGraph should later be wrapped with {@link IdGraph} use IdGraph.ID.
     *
     * @param key Key to be used.
     */
    public void setVertexIdKey(final String key) {
        if (!loadingFromScratch && key==null && baseGraph.getFeatures().ignoresSuppliedIds) throw new IllegalStateException("Cannot set vertex id key to null when not loading from scratch while ids are ignored.");
        this.vertexIdKey = key;
    }

    /**
     * Returns the key used to set the id on the vertices or null if such has not been set
     * via {@link #setVertexIdKey(String)}
     * 
     * @return The key used to set the id on the vertices or null if such has not been set
     * via {@link #setVertexIdKey(String)}
     */
    public String getVertexIdKey() {
        return vertexIdKey;
    }

    /**
     * Sets the key to be used when setting the edge id as a property on the respective edge.
     * If the key is null, then no property will be set.
     * If the loaded baseGraph should later be wrapped with {@link IdGraph} use IdGraph.ID.
     *
     * @param key Key to be used.
     */
    public void setEdgeIdKey(final String key) {
        this.edgeIdKey = key;
    }

    /**
     * Returns the key used to set the id on the edges or null if such has not been set
     * via {@link #setEdgeIdKey(String)}
     *
     * @return The key used to set the id on the edges or null if such has not been set
     * via {@link #setEdgeIdKey(String)}
     */
    public String getEdgeIdKey() {
        return edgeIdKey;
    }

    /**
     * Sets whether the graph loaded through this instance of {@link BatchGraph} is loaded from scratch
     * (i.e. the wrapped graph is initially empty) or whether graph is loaded incrementally into an
     * existing graph.
     *
     * In the former case, BatchGraph does not need to check for the existence of vertices with the wrapped
     * graph but only needs to consult its own cache which can be significantly faster. In the latter case,
     * the cache is checked first but an additional check against the wrapped graph may be necessary if
     * the vertex does not exist.
     *
     * By default, BatchGraph assumes that the data is loaded from scratch.
     * 
     * When setting loading from scratch to false, a vertex id key must be specified first using
     * {@link #setVertexIdKey(String)} - otherwise an exception is thrown.
     *
     * @param fromScratch
     */
    public void setLoadingFromScratch(boolean fromScratch) {
        if (fromScratch==false && vertexIdKey==null && baseGraph.getFeatures().ignoresSuppliedIds) throw new IllegalStateException("Vertex id key is required to query existing vertices in wrapped graph.");
        loadingFromScratch=fromScratch;
    }

    /** Whether this BatchGraph is loading data from scratch or incrementally into an existing graph.
     *
     * By default, this returns true.
     *
     * @return Whether this BatchGraph is loading data from scratch or incrementally into an existing graph.
     *
     * @see #setLoadingFromScratch(boolean)
     */
    public boolean isLoadingFromScratch() {
        return loadingFromScratch;
    }

    private void nextElement() {
        currentEdge = null;
        currentEdgeCached = null;
        if (remainingBufferSize <= 0) {
            baseGraph.stopTransaction(Conclusion.SUCCESS);
            cache.newTransaction();
            remainingBufferSize = bufferSize;
        }
        remainingBufferSize--;
    }

    /**
     * Should only be invoked after loading is complete. Stopping the transaction before will cause the loading to fail.
     * Only Conclusion.SUCCESS is accepted.
     *
     * @param conclusion whether or not the current transaction was successful
     */
    @Override
    public void stopTransaction(final Conclusion conclusion) {
        if (conclusion != Conclusion.SUCCESS) throw new IllegalArgumentException("Cannot abort batch loading");
        currentEdge = null;
        currentEdgeCached = null;
        remainingBufferSize = 0;
        baseGraph.stopTransaction(Conclusion.SUCCESS);
    }

    @Override
    public void shutdown() {
        baseGraph.stopTransaction(Conclusion.SUCCESS);
        baseGraph.shutdown();
        currentEdge = null;
        currentEdgeCached = null;
    }

    @Override
    public T getBaseGraph() {
        return baseGraph;
    }

    @Override
    public Features getFeatures() {
        Features features = baseGraph.getFeatures().copyFeatures();
        features.ignoresSuppliedIds = false;
        features.isWrapper = true;
        features.supportsEdgeIteration = false;
        features.supportsThreadedTransactions = false;
        features.supportsVertexIteration = false;
        return features;
    }

    private Vertex retrieveFromCache(final Object externalID) {
        Object internal = cache.getEntry(externalID);
        if (internal instanceof Vertex) {
            return (Vertex) internal;
        } else if (internal != null) { //its an internal id
            Vertex v = baseGraph.getVertex(internal);
            cache.set(v,externalID);
            return v;
        } else return null;
    }

    private Vertex getCachedVertex(final Object externalID) {
        Vertex v = retrieveFromCache(externalID);
        if (v == null) throw new IllegalArgumentException("Vertex for given ID cannot be found: " + externalID);
        return v;
    }

    @Override
    public Vertex getVertex(final Object id) {
        Vertex v = retrieveFromCache(id);
        if (v == null) {
            if (loadingFromScratch) return null;
            else {
                if (baseGraph.getFeatures().ignoresSuppliedIds) {
                    assert vertexIdKey!=null;
                    Iterator<Vertex> iter = baseGraph.getVertices(vertexIdKey,id).iterator();
                    if (!iter.hasNext()) return null;
                    v = iter.next();
                    if (iter.hasNext()) throw new IllegalArgumentException("There are multiple vertices with the provided id in the database: " + id);
                } else {
                    v = baseGraph.getVertex(id);
                    if (v==null) return null;
                }
                cache.set(v,id);
            }
        }
        return new BatchVertex(id);
    }

    @Override
    public Vertex addVertex(final Object id) {
        if (id == null) throw ExceptionFactory.vertexIdCanNotBeNull();
        if (retrieveFromCache(id)!=null) throw ExceptionFactory.vertexWithIdAlreadyExists(id);
        nextElement();

        Vertex v = baseGraph.addVertex(id);
        if (vertexIdKey != null) {
            v.setProperty(vertexIdKey, id);
        }
        cache.set(v, id);
        return new BatchVertex(id);
    }

    @Override
    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        if (!BatchVertex.class.isInstance(outVertex) || !BatchVertex.class.isInstance(inVertex))
            throw new IllegalArgumentException("Given element was not created in this baseGraph");
        nextElement();
        final Vertex ov = getCachedVertex(outVertex.getId());
        final Vertex iv = getCachedVertex(inVertex.getId());
        currentEdgeCached = baseGraph.addEdge(id, ov, iv, label);
        if (edgeIdKey != null && id != null) {
            currentEdgeCached.setProperty(edgeIdKey, id);
        }

        currentEdge = new BatchEdge();
        return currentEdge;
    }
    
    @Override
    public String toString() {
        return StringFactory.graphString(this, this.baseGraph.toString());
    }

    // ################### Unsupported Graph Methods ####################

    @Override
    public Edge getEdge(Object id) {
        throw retrievalNotSupported();
    }

    @Override
    public void removeVertex(Vertex vertex) {
        throw removalNotSupported();
    }

    @Override
    public Iterable<Vertex> getVertices() {
        throw retrievalNotSupported();
    }

    @Override
    public Iterable<Vertex> getVertices(String key, Object value) {
        throw retrievalNotSupported();
    }

    @Override
    public void removeEdge(Edge edge) {
        throw removalNotSupported();
    }

    @Override
    public Iterable<Edge> getEdges() {
        throw retrievalNotSupported();
    }

    @Override
    public Iterable<Edge> getEdges(String key, Object value) {
        throw retrievalNotSupported();
    }

    private class BatchVertex implements Vertex {

        private final Object externalID;

        BatchVertex(Object id) {
            if (id == null) throw new IllegalArgumentException("External id may not be null");
            externalID = id;
        }

        @Override
        public Iterable<Edge> getEdges(Direction direction, String... labels) {
            throw retrievalNotSupported();
        }

        @Override
        public Iterable<Vertex> getVertices(Direction direction, String... labels) {
            throw retrievalNotSupported();
        }

        @Override
        public Query query() {
            throw retrievalNotSupported();
        }

        @Override
        public void setProperty(String key, Object value) {
            getCachedVertex(externalID).setProperty(key, value);
        }

        @Override
        public Object getId() {
            return externalID;
        }

        @Override
        public Object getProperty(String key) {
            return getCachedVertex(externalID).getProperty(key);
        }

        @Override
        public Set<String> getPropertyKeys() {
            return getCachedVertex(externalID).getPropertyKeys();
        }

        @Override
        public Object removeProperty(String key) {
            return getCachedVertex(externalID).removeProperty(key);
        }
        
        @Override
        public String toString() {
            return "v["+externalID+"]";
        }
    }

    private class BatchEdge implements Edge {

        @Override
        public Vertex getVertex(Direction direction) throws IllegalArgumentException {
            return getWrappedEdge().getVertex(direction);
        }

        @Override
        public String getLabel() {
            return getWrappedEdge().getLabel();
        }

        @Override
        public void setProperty(String key, Object value) {
            getWrappedEdge().setProperty(key, value);
        }

        @Override
        public Object getId() {
            return getWrappedEdge().getId();
        }

        @Override
        public Object getProperty(String key) {
            return getWrappedEdge().getProperty(key);
        }

        @Override
        public Set<String> getPropertyKeys() {
            return getWrappedEdge().getPropertyKeys();
        }

        @Override
        public Object removeProperty(String key) {
            return getWrappedEdge().removeProperty(key);
        }

        private Edge getWrappedEdge() {
            if (this != currentEdge) {
                throw new UnsupportedOperationException("This edge is no longer in scope");
            }
            return currentEdgeCached;
        }
        
        @Override
        public String toString() {
            return getWrappedEdge().toString();
        }
        
    }


    private static UnsupportedOperationException retrievalNotSupported() {
        return new UnsupportedOperationException("Retrieval operations are not supported during batch loading");
    }

    private static UnsupportedOperationException removalNotSupported() {
        return new UnsupportedOperationException("Removal operations are not supported during batch loading");
    }


}
