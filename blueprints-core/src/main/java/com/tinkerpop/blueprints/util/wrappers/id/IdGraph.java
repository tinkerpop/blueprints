package com.tinkerpop.blueprints.util.wrappers.id;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import com.tinkerpop.blueprints.util.StringFactory;
import com.tinkerpop.blueprints.util.wrappers.WrappedGraphQuery;
import com.tinkerpop.blueprints.util.wrappers.WrapperGraph;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * A Graph implementation which wraps another Graph implementation,
 * enabling custom element IDs even for those graphs which don't otherwise support them.
 *
 * The base Graph must be an instance of KeyIndexableGraph.
 * It *may* be an instance of IndexableGraph, in which case its indices will be wrapped appropriately.
 * It *may* be an instance of TransactionalGraph, in which case transaction operations will be passed through.
 * For those graphs which support vertex indices but not edge indices (or vice versa),
 * you may configure IdGraph to use custom IDs only for vertices or only for edges.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class IdGraph<T extends KeyIndexableGraph> implements KeyIndexableGraph, WrapperGraph<T>, IndexableGraph, TransactionalGraph {

    private static final Logger LOGGER = Logger.getLogger(IdGraph.class.getName());

    // Note: using "__id" instead of "_id" avoids collision with Rexster's "_id"
    public static final String ID = "__id";

    private final T baseGraph;

    private IdFactory vertexIdFactory;
    private IdFactory edgeIdFactory;

    private final Features features;

    private final boolean supportVertexIds, supportEdgeIds;

    private boolean uniqueIds = true;

    /**
     * Adds custom ID functionality to the given graph,
     * supporting both custom vertex IDs and custom edge IDs.
     *
     * @param baseGraph the base graph which does not necessarily support custom IDs
     */
    public IdGraph(final T baseGraph) {
        this(baseGraph, true, true);
    }

    /**
     * Adds custom ID functionality to the given graph,
     * supporting either custom vertex IDs, custom edge IDs, or both.
     *
     * @param baseGraph        the base graph which does not necessarily support custom IDs
     * @param supportVertexIds whether to support custom vertex IDs
     * @param supportEdgeIds   whether to support custom edge IDs
     */
    public IdGraph(final T baseGraph,
                   final boolean supportVertexIds,
                   final boolean supportEdgeIds) {
        this.baseGraph = baseGraph;
        this.features = this.baseGraph.getFeatures().copyFeatures();
        features.isWrapper = true;
        features.ignoresSuppliedIds = false;

        this.supportVertexIds = supportVertexIds;
        this.supportEdgeIds = supportEdgeIds;

        if (!supportVertexIds && !supportEdgeIds) {
            throw new IllegalArgumentException("if neither custom vertex IDs nor custom edge IDs are supported, IdGraph can't help you!");
        }

        createIndices();

        vertexIdFactory = new DefaultIdFactory();
        edgeIdFactory = new DefaultIdFactory();
    }

    /**
     * @param idFactory a factory for new vertex IDs.
     *                  When vertices are created using null IDs, the actual IDs are chosen based on this factory.
     */
    public void setVertexIdFactory(final IdFactory idFactory) {
        vertexIdFactory = idFactory;
    }

    /**
     * @param idFactory a factory for new edge IDs.
     *                  When edges are created using null IDs, the actual IDs are chosen based on this factory.
     */
    public void setEdgeIdFactory(final IdFactory idFactory) {
        edgeIdFactory = idFactory;
    }

    /**
     * @return the factory for new vertex IDs.
     *         When vertices are created using null IDs, the actual IDs are chosen based on this factory.
     */
    public IdFactory getVertexIdFactory() {
        return vertexIdFactory;
    }

    /**
     * return the factory for new edge IDs.
     * When edges are created using null IDs, the actual IDs are chosen based on this factory.
     */
    public IdFactory getEdgeIdFactory() {
        return edgeIdFactory;
    }

    public Features getFeatures() {
        return features;
    }

    public Vertex addVertex(final Object id) {
        if (uniqueIds && null != id && null != getVertex(id)) {
            throw new IllegalArgumentException("vertex with given id already exists: '" + id + "'");
        }

        final Vertex base = baseGraph.addVertex(null);

        if (supportVertexIds) {
            Object v = null == id ? vertexIdFactory.createId() : id;

            if (null != v) {
                base.setProperty(ID, v);
            }
        }

        return new IdVertex(base, this);
    }

    public Vertex getVertex(final Object id) {
        if (null == id) {
            throw new IllegalArgumentException("vertex identifier cannot be null");
        }

        if (supportVertexIds) {
            final Iterable<Vertex> i = baseGraph.getVertices(ID, id);
            final Iterator<Vertex> iter = i.iterator();
            if (!iter.hasNext()) {
                return null;
            } else {
                Vertex v = iter.next();

                if (iter.hasNext()) {
                    LOGGER.warning("multiple vertices exist with id '" + id + "'. Arbitarily choosing " + v);
                }

                return new IdVertex(v, this);
            }
        } else {
            Vertex base = baseGraph.getVertex(id);
            return null == base ? null : new IdVertex(base, this);
        }
    }

    public void removeVertex(final Vertex vertex) {
        verifyNativeElement(vertex);
        baseGraph.removeVertex(((IdVertex) vertex).getBaseVertex());
    }

    public Iterable<Vertex> getVertices() {
        return new IdVertexIterable(baseGraph.getVertices(), this);
    }

    public Iterable<Vertex> getVertices(final String key,
                                        final Object value) {
        if (supportVertexIds && key.equals(ID)) {
            throw new IllegalArgumentException("index key " + ID + " is reserved by IdGraph");
        } else {
            return new IdVertexIterable(baseGraph.getVertices(key, value), this);
        }
    }

    public Edge addEdge(final Object id,
                        final Vertex outVertex,
                        final Vertex inVertex,
                        final String label) {
        if (uniqueIds && null != id && null != getEdge(id)) {
            throw new IllegalArgumentException("edge with given id already exists: " + id);
        }

        verifyNativeElement(outVertex);
        verifyNativeElement(inVertex);

        Edge base = baseGraph.addEdge(null, ((IdVertex) outVertex).getBaseVertex(), ((IdVertex) inVertex).getBaseVertex(), label);

        if (supportEdgeIds) {
            Object v = null == id ? edgeIdFactory.createId() : id;

            if (null != v) {
                base.setProperty(ID, v);
            }
        }

        return new IdEdge(base, this);
    }

    public Edge getEdge(final Object id) {
        if (null == id) {
            throw new IllegalArgumentException("edge identifier cannot be null");
        }

        if (supportEdgeIds) {
            Iterable<Edge> i = baseGraph.getEdges(ID, id);
            Iterator<Edge> iter = i.iterator();
            if (!iter.hasNext()) {
                return null;
            } else {
                Edge e = iter.next();

                if (iter.hasNext()) {
                    LOGGER.warning("multiple edges exist with id '" + id + "'. Arbitarily choosing " + e);
                }

                return new IdEdge(e, this);
            }
        } else {
            Edge base = baseGraph.getEdge(id);
            return null == base ? null : new IdEdge(base, this);
        }
    }

    public void removeEdge(final Edge edge) {
        verifyNativeElement(edge);

        baseGraph.removeEdge(((IdEdge) edge).getBaseEdge());
    }

    public Iterable<Edge> getEdges() {
        return new IdEdgeIterable(baseGraph.getEdges(), this);
    }

    public Iterable<Edge> getEdges(final String key,
                                   final Object value) {
        if (supportEdgeIds && key.equals(ID)) {
            throw new IllegalArgumentException("index key " + ID + " is reserved by IdGraph");
        } else {
            return new IdEdgeIterable(baseGraph.getEdges(key, value), this);
        }
    }

    // Note: this is a no-op if the base graph is not an instance of TransactionalGraph
    public void stopTransaction(Conclusion conclusion) {
        if (Conclusion.SUCCESS == conclusion)
            commit();
        else
            rollback();
    }

    public void rollback() {
        if (this.baseGraph instanceof TransactionalGraph) {
            ((TransactionalGraph) baseGraph).rollback();
        }
    }

    public void commit() {
        if (this.baseGraph instanceof TransactionalGraph) {
            ((TransactionalGraph) baseGraph).commit();
        }
    }

    public void shutdown() {
        baseGraph.shutdown();
    }

    public String toString() {
        return StringFactory.graphString(this, this.baseGraph.toString());
    }

    public <T extends Element> void dropKeyIndex(final String key, final Class<T> elementClass) {
        if (elementClass == null)
            throw ExceptionFactory.classForElementCannotBeNull();

        boolean v = isVertexClass(elementClass);
        boolean supported = ((v && supportVertexIds) || (!v && supportEdgeIds));

        if (supported && key.equals(ID)) {
            throw new IllegalArgumentException("index key " + ID + " is reserved by IdGraph");
        } else {
            baseGraph.dropKeyIndex(key, elementClass);
        }
    }

    public <T extends Element> void createKeyIndex(final String key,
                                                   final Class<T> elementClass,
                                                   final Parameter... indexParameters) {
        if (elementClass == null)
            throw ExceptionFactory.classForElementCannotBeNull();

        boolean v = isVertexClass(elementClass);
        boolean supported = ((v && supportVertexIds) || (!v && supportEdgeIds));

        if (supported && key.equals(ID)) {
            throw new IllegalArgumentException("index key " + ID + " is reserved by IdGraph");
        } else {
            baseGraph.createKeyIndex(key, elementClass, indexParameters);
        }
    }

    public <T extends Element> Set<String> getIndexedKeys(final Class<T> elementClass) {
        if (elementClass == null)
            throw ExceptionFactory.classForElementCannotBeNull();

        boolean v = isVertexClass(elementClass);
        boolean supported = ((v && supportVertexIds) || (!v && supportEdgeIds));

        if (supported) {
            Set<String> keys = new HashSet<String>();
            keys.addAll(baseGraph.getIndexedKeys(elementClass));
            keys.remove(ID);
            return keys;
        } else {
            return baseGraph.getIndexedKeys(elementClass);
        }
    }

    public T getBaseGraph() {
        return this.baseGraph;
    }

    public void enforceUniqueIds(boolean enforceUniqueIds) {
        this.uniqueIds = enforceUniqueIds;
    }

    public <T extends Element> Index<T> createIndex(final String indexName,
                                                    final Class<T> indexClass,
                                                    final Parameter... indexParameters) {
        verifyBaseGraphIsIndexableGraph();

        return isVertexClass(indexClass)
                ? (Index<T>) new IdVertexIndex((Index<Vertex>) ((IndexableGraph) baseGraph).createIndex(indexName, indexClass, indexParameters), this)
                : (Index<T>) new IdEdgeIndex((Index<Edge>) ((IndexableGraph) baseGraph).createIndex(indexName, indexClass, indexParameters), this);
    }

    public <T extends Element> Index<T> getIndex(final String indexName,
                                                 final Class<T> indexClass) {
        verifyBaseGraphIsIndexableGraph();

        if (isVertexClass(indexClass)) {
            Index<Vertex> baseIndex = (Index<Vertex>) ((IndexableGraph) baseGraph).getIndex(indexName, indexClass);
            return null == baseIndex ? null : (Index<T>) new IdVertexIndex(baseIndex, this);
        } else {
            Index<Edge> baseIndex = (Index<Edge>) ((IndexableGraph) baseGraph).getIndex(indexName, indexClass);
            return null == baseIndex ? null : (Index<T>) new IdEdgeIndex(baseIndex, this);
        }
    }

    public Iterable<Index<? extends Element>> getIndices() {
        throw new UnsupportedOperationException("sorry, you currently can't get a list of indexes through IdGraph");
    }

    public void dropIndex(final String indexName) {
        verifyBaseGraphIsIndexableGraph();

        ((IndexableGraph) baseGraph).dropIndex(indexName);
    }

    public GraphQuery query() {
        final IdGraph idGraph = this;
        return new WrappedGraphQuery(this.baseGraph.query()) {
            @Override
            public Iterable<Edge> edges() {
                return new IdEdgeIterable(this.query.edges(), idGraph);
            }

            @Override
            public Iterable<Vertex> vertices() {
                return new IdVertexIterable(this.query.vertices(), idGraph);
            }
        };
    }

    public boolean getSupportVertexIds() {
        return supportVertexIds;
    }

    public boolean getSupportEdgeIds() {
        return supportEdgeIds;
    }

    /**
     * A factory for IDs of newly-created vertices and edges (where an ID is not otherwise specified).
     */
    public static interface IdFactory {
        Object createId();
    }

    private static class DefaultIdFactory implements IdFactory {
        public Object createId() {
            return UUID.randomUUID().toString();
        }
    }

    private void verifyBaseGraphIsIndexableGraph() {
        if (!(baseGraph instanceof IndexableGraph)) {
            throw new IllegalStateException("base graph is not an indexable graph");
        }
    }

    private boolean isVertexClass(final Class c) {
        return Vertex.class.isAssignableFrom(c);
    }

    private void createIndices() {
        if (supportVertexIds && !baseGraph.getIndexedKeys(Vertex.class).contains(ID)) {
            baseGraph.createKeyIndex(ID, Vertex.class);
        }

        if (supportEdgeIds && !baseGraph.getIndexedKeys(Edge.class).contains(ID)) {
            baseGraph.createKeyIndex(ID, Edge.class);
        }
    }

    private static void verifyNativeElement(final Element e) {
        if (!(e instanceof IdElement)) {
            throw new IllegalArgumentException("given element was not created in this graph");
        }
    }
}
