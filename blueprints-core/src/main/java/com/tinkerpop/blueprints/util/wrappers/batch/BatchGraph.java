package com.tinkerpop.blueprints.util.wrappers.batch;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexQuery;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import com.tinkerpop.blueprints.util.StringFactory;
import com.tinkerpop.blueprints.util.wrappers.WrapperGraph;
import com.tinkerpop.blueprints.util.wrappers.batch.cache.VertexCache;
import com.tinkerpop.blueprints.util.wrappers.id.IdGraph;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * BatchGraph is a wrapper that enables batch loading of a large number of edges and vertices by chunking the entire
 * load into smaller batches and maintaining a memory-efficient vertex cache so that the entire transactional state can
 * be flushed after each chunk is loaded.
 *
 * BatchGraph is ONLY meant for loading data and does not support any retrieval or removal operations.
 * That is, BatchGraph only supports the following methods:
 * - {@link #addVertex(Object)} for adding vertices
 * - {@link #addEdge(Object, com.tinkerpop.blueprints.Vertex, com.tinkerpop.blueprints.Vertex, String)} for adding edges
 * - {@link #getVertex(Object)} to be used when adding edges
 * - Property getter, setter and removal methods for vertices and edges.
 *
 * An important limitation of BatchGraph is that edge properties can only be set immediately after the edge has been added.
 * If other vertices or edges have been created in the meantime, setting, getting or removing properties will throw
 * exceptions. This is done to avoid caching of edges which would require a great amount of memory.
 *
 * BatchGraph wraps {@link TransactionalGraph}. To wrap arbitrary graphs, use {@link #wrap(com.tinkerpop.blueprints.Graph)}
 * which will additionally wrap non-transactional.
 *
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


    private final T baseGraph;

    private String vertexIdKey = null;
    private String edgeIdKey = null;
    private boolean loadingFromScratch = true;

    private final VertexCache cache;

    private long bufferSize = DEFAULT_BUFFER_SIZE;
    private long remainingBufferSize;

    private BatchEdge currentEdge = null;
    private Edge currentEdgeCached = null;

    private Object previousOutVertexId = null;

    /**
     * Constructs a BatchGraph wrapping the provided baseGraph, using the specified buffer size and expecting vertex ids of
     * the specified IdType. Supplying vertex ids which do not match this type will throw exceptions.
     *
     * @param graph      Graph to be wrapped
     * @param type       Type of vertex id expected. This information is used to optimize the vertex cache memory footprint.
     * @param bufferSize Defines the number of vertices and edges loaded before starting a new transaction. The larger this value, the more memory is required but the faster the loading process.
     */
    public BatchGraph(final T graph, final VertexIDType type, final long bufferSize) {
        if (graph == null) throw new IllegalArgumentException("Graph may not be null");
        if (type == null) throw new IllegalArgumentException("Type may not be null");
        if (bufferSize <= 0) throw new IllegalArgumentException("BufferSize must be positive");
        this.baseGraph = graph;
        this.bufferSize = bufferSize;

        vertexIdKey = null;
        edgeIdKey = null;

        cache = type.getVertexCache();

        remainingBufferSize = this.bufferSize;
    }
    
    /**
     * Constructs a BatchGraph wrapping the provided baseGraph.
     *
     * @param graph Graph to be wrapped
     * @param bufferSize Defines the number of vertices and edges loaded before starting a new transaction. The larger this value, the more memory is required but the faster the loading process.
     */
    public BatchGraph(final T graph, final long bufferSize) {
        this(graph, VertexIDType.OBJECT, bufferSize);
    }

    /**
     * Constructs a BatchGraph wrapping the provided baseGraph.
     *
     * @param graph Graph to be wrapped
     */
    public BatchGraph(final T graph) {
        this(graph, VertexIDType.OBJECT, DEFAULT_BUFFER_SIZE);
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
            return new BatchGraph((TransactionalGraph) graph, VertexIDType.OBJECT, buffer);
        else return new BatchGraph(new WritethroughGraph(graph), VertexIDType.OBJECT, buffer);
    }

    /**
     * Sets the key to be used when setting the vertex id as a property on the respective vertex.
     * If the key is null, then no property will be set.
     * If the loaded baseGraph should later be wrapped with {@link IdGraph} use IdGraph.ID.
     *
     * @param key Key to be used.
     */
    public void setVertexIdKey(final String key) {
        if (!loadingFromScratch && key == null && baseGraph.getFeatures().ignoresSuppliedIds)
            throw new IllegalStateException("Cannot set vertex id key to null when not loading from scratch while ids are ignored.");
        this.vertexIdKey = key;
    }

    /**
     * Returns the key used to set the id on the vertices or null if such has not been set
     * via {@link #setVertexIdKey(String)}
     *
     * @return The key used to set the id on the vertices or null if such has not been set
     *         via {@link #setVertexIdKey(String)}
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
     *         via {@link #setEdgeIdKey(String)}
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
        if (fromScratch == false && vertexIdKey == null && baseGraph.getFeatures().ignoresSuppliedIds)
            throw new IllegalStateException("Vertex id key is required to query existing vertices in wrapped graph.");
        loadingFromScratch = fromScratch;
    }

    /**
     * Whether this BatchGraph is loading data from scratch or incrementally into an existing graph.
     *
     * By default, this returns true.
     *
     * @return Whether this BatchGraph is loading data from scratch or incrementally into an existing graph.
     * @see #setLoadingFromScratch(boolean)
     */
    public boolean isLoadingFromScratch() {
        return loadingFromScratch;
    }

    private void nextElement() {
        currentEdge = null;
        currentEdgeCached = null;
        if (remainingBufferSize <= 0) {
            baseGraph.commit();
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
    public void stopTransaction(Conclusion conclusion) {
        if (Conclusion.SUCCESS == conclusion)
            commit();
        else
            rollback();
    }

    /**
     * Should only be invoked after loading is complete. Committing the transaction before will cause the loading to fail.
     */
    @Override
    public void commit() {
        currentEdge = null;
        currentEdgeCached = null;
        remainingBufferSize = 0;
        baseGraph.commit();
    }

    /**
     * Not supported for batch loading, since data may have already been partially persisted.
     */
    @Override
    public void rollback() {
        throw new IllegalStateException("Can not rollback during batch loading");
    }

    @Override
    public void shutdown() {
        baseGraph.commit();
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
            cache.set(v, externalID);
            return v;
        } else return null;
    }

    private Vertex getCachedVertex(final Object externalID) {
        Vertex v = retrieveFromCache(externalID);
        if (v == null) throw new IllegalArgumentException("Vertex for given ID cannot be found: " + externalID);
        return v;
    }


    /**
     * If the input data are sorted, then out vertex will be repeated for several edges in a row.
     * In this case, bypass cache and instead immediately return a new vertex using the known id.
     * This gives a modest performance boost, especially when the cache is large or there are
     * on average many edges per vertex.
     */
    @Override
    public Vertex getVertex(final Object id) {

        if ((previousOutVertexId != null) && (previousOutVertexId.equals(id))) {
            return new BatchVertex(previousOutVertexId);
        } else {

            Vertex v = retrieveFromCache(id);
            if (v == null) {
                if (loadingFromScratch) return null;
                else {
                    if (baseGraph.getFeatures().ignoresSuppliedIds) {
                        assert vertexIdKey != null;
                        Iterator<Vertex> iter = baseGraph.getVertices(vertexIdKey, id).iterator();
                        if (!iter.hasNext()) return null;
                        v = iter.next();
                        if (iter.hasNext())
                            throw new IllegalArgumentException("There are multiple vertices with the provided id in the database: " + id);
                    } else {
                        v = baseGraph.getVertex(id);
                        if (v == null) return null;
                    }
                    cache.set(v, id);
                }
            }
            return new BatchVertex(id);
        }
    }

    @Override
    public Vertex addVertex(final Object id) {
        return addVertex(id, (Object[]) null);
    }

    public Vertex addVertex(final Object id, final Object... properties) {
        if (id == null) throw ExceptionFactory.vertexIdCanNotBeNull();
        if (retrieveFromCache(id) != null) throw ExceptionFactory.vertexWithIdAlreadyExists(id);
        nextElement();

        Vertex v = baseGraph.addVertex(id);
        if (vertexIdKey != null) {
            v.setProperty(vertexIdKey, id);
        }
        cache.set(v, id);
        final BatchVertex newVertex = new BatchVertex(id);

        setProperties(newVertex, properties);

        return newVertex;
    }

    @Override
    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        return addEdge(id, outVertex, inVertex, label, (Object[]) null);
    }

    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label, final Object... properties) {
        if (!BatchVertex.class.isInstance(outVertex) || !BatchVertex.class.isInstance(inVertex))
            throw new IllegalArgumentException("Given element was not created in this baseGraph");
        nextElement();

        final Vertex ov = getCachedVertex(outVertex.getId());
        final Vertex iv = getCachedVertex(inVertex.getId());

        previousOutVertexId = outVertex.getId();  //keep track of the previous out vertex id

        currentEdgeCached = baseGraph.addEdge(id, ov, iv, label);
        if (edgeIdKey != null && id != null) {
            currentEdgeCached.setProperty(edgeIdKey, id);
        }

        currentEdge = new BatchEdge();

        setProperties(currentEdge, properties);

        return currentEdge;
    }

    protected <E extends Element> E setProperties(final E element, final Object... properties) {
        if (properties != null && properties.length > 0) {
            if (properties.length == 1) {
                final Object f = properties[0];
                if (f instanceof Map<?, ?>) {
                    for (Map.Entry<?, ?> entry : ((Map<?, ?>) f).entrySet())
                        element.setProperty(entry.getKey().toString(), entry.getValue());
                } else
                    throw new IllegalArgumentException(
                            "Invalid properties: expecting a pairs of fields as String,Object or a single Map<String,Object>, but found: " + f);
            } else
                // SET PROPERTIES ONE BY ONE
                for (int i = 0; i < properties.length; i += 2)
                    element.setProperty(properties[i].toString(), properties[i + 1]);
        }
        return element;
    }


    protected Edge addEdgeSupport(final Vertex outVertex, final Vertex inVertex, final String label) {
        return this.addEdge(null, outVertex, inVertex, label);
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

    @Override
    public GraphQuery query() {
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
        public VertexQuery query() {
            throw retrievalNotSupported();
        }

        public Edge addEdge(final String label, final Vertex vertex) {
            return addEdgeSupport(this, vertex, label);
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

        public void remove() {
            removeVertex(this);
        }

        @Override
        public String toString() {
            return "v[" + externalID + "]";
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

        public void remove() {
            removeEdge(this);
        }

    }


    private static UnsupportedOperationException retrievalNotSupported() {
        return new UnsupportedOperationException("Retrieval operations are not supported during batch loading");
    }

    private static UnsupportedOperationException removalNotSupported() {
        return new UnsupportedOperationException("Removal operations are not supported during batch loading");
    }


}
