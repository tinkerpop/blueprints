package com.tinkerpop.blueprints.util.wrappers.id;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.StringFactory;
import com.tinkerpop.blueprints.util.wrappers.WrapperGraph;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

/**
 * A Graph implementation which wraps another Graph implementation,
 * enabling custom element IDs even for those graphs which don't otherwise support them.
 * <p/>
 * The base Graph must be an instance of KeyIndexableGraph.
 * It *may* be an instance of IndexableGraph, in which case its indices will be wrapped appropriately.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class IdGraph<T extends KeyIndexableGraph> implements KeyIndexableGraph, WrapperGraph<T>, IndexableGraph, TransactionalGraph {

    // Note: using "__id" instead of "_id" avoids collision with Rexster's "_id"
    public static final String ID = "__id";

    private final T baseGraph;

    private final IdFactory vertexIdFactory;
    private final IdFactory edgeIdFactory;

    private final Features features;

    private boolean uniqueIds = true;

    /**
     * Adds custom ID functionality to the given graph.
     *
     * @param baseGraph the base graph which does not permit custom element IDs
     */
    public IdGraph(final T baseGraph) {
        this(baseGraph, null);
    }

    /**
     * Adds custom ID functionality to the given graph, also specifying a factory for new element IDs.
     *
     * @param baseGraph the base graph which may or may not permit custom element IDs
     * @param idFactory a factory for new vertex and edge IDs.
     *                  When vertices or edges are created using null IDs, the actual IDs are chosen based on this factory.
     */
    public IdGraph(final T baseGraph, final IdFactory idFactory) {
        this(baseGraph, idFactory, idFactory);
    }

    /**
     * Adds custom ID functionality to the given graph, also specifying (separate) factories for new vertex and edge IDs.
     *
     * @param baseGraph       the base graph which may or may not permit custom element IDs
     * @param vertexIdFactory a factory for new vertex IDs.
     *                        When vertices are created using null IDs, the actual IDs are chosen based on this factory.
     * @param edgeIdFactory   a factory for new edge IDs.
     *                        When edges are created using null IDs, the actual IDs are chosen based on this factory.
     */
    public IdGraph(final T baseGraph,
                   final IdFactory vertexIdFactory,
                   final IdFactory edgeIdFactory) {
        this.baseGraph = baseGraph;
        this.features = this.baseGraph.getFeatures().copyFeatures();
        features.isWrapper = true;
        features.ignoresSuppliedIds = false;

        this.vertexIdFactory = null == vertexIdFactory ? new DefaultIdFactory() : vertexIdFactory;
        this.edgeIdFactory = null == edgeIdFactory ? new DefaultIdFactory() : edgeIdFactory;

        createIndices();
    }

    public IdFactory getVertexIdFactory() {
        return vertexIdFactory;
    }

    public IdFactory getEdgeIdFactory() {
        return edgeIdFactory;
    }

    private void createIndices() {
        if (!baseGraph.getIndexedKeys(Vertex.class).contains(ID)) {
            baseGraph.createKeyIndex(ID, Vertex.class);
        }

        if (!baseGraph.getIndexedKeys(Edge.class).contains(ID)) {
            baseGraph.createKeyIndex(ID, Edge.class);
        }
    }

    public Features getFeatures() {
        return features;
    }

    public Vertex addVertex(final Object id) {
        if (uniqueIds && null != id && null != getVertex(id)) {
            throw new IllegalArgumentException("Vertex with given id already exists: '" + id + "'");
        }

        final Vertex base = baseGraph.addVertex(null);

        Object v = null == id ? vertexIdFactory.createId() : id;

        if (null == v) {
            base.removeProperty(ID);
        } else {
            base.setProperty(ID, v);
        }

        return new IdVertex(base);
    }

    public Vertex getVertex(final Object id) {
        if (null == id) {
            throw new IllegalArgumentException("Element identifier cannot be null");
        }

        final Iterable<Vertex> i = baseGraph.getVertices(ID, id);
        final Iterator<Vertex> iter = i.iterator();
        if (!iter.hasNext()) {
            return null;
        } else {
            Vertex e = iter.next();

            if (iter.hasNext()) {
                throw new IllegalStateException("multiple vertices exist with id '" + id + "'");
            }

            return new IdVertex(e);
        }
    }

    public void removeVertex(final Vertex vertex) {
        verifyNativeElement(vertex);
        baseGraph.removeVertex(((IdVertex) vertex).getBaseVertex());
    }

    public Iterable<Vertex> getVertices() {
        return new IdVertexIterable(baseGraph.getVertices());
    }

    public Iterable<Vertex> getVertices(final String key, final Object value) {
        if (key.equals(ID)) {
            throw new IllegalArgumentException("Index key " + ID + " is reserved by IdGraph");
        } else {
            return new IdVertexIterable(baseGraph.getVertices(key, value));
        }
    }

    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        if (uniqueIds && null != id && null != getEdge(id)) {
            throw new IllegalArgumentException("Edge with given id already exists: " + id);
        }

        verifyNativeElement(outVertex);
        verifyNativeElement(inVertex);

        Edge base = baseGraph.addEdge(null, ((IdVertex) outVertex).getBaseVertex(), ((IdVertex) inVertex).getBaseVertex(), label);

        Object v = null == id ? edgeIdFactory.createId() : id;

        if (null == v) {
            base.removeProperty(ID);
        } else {
            base.setProperty(ID, v);
        }

        return new IdEdge(base);
    }

    public Edge getEdge(final Object id) {
        if (null == id) {
            throw new IllegalArgumentException("Element identifier cannot be null");
        }

        Iterable<Edge> i = baseGraph.getEdges(ID, id);
        Iterator<Edge> iter = i.iterator();
        if (!iter.hasNext()) {
            return null;
        } else {
            Edge e = iter.next();

            if (iter.hasNext()) {
                throw new IllegalStateException("Multiple edges exist with id " + id);
            }

            return new IdEdge(e);
        }
    }

    public void removeEdge(final Edge edge) {
        verifyNativeElement(edge);

        baseGraph.removeEdge(((IdEdge) edge).getBaseEdge());
    }

    public Iterable<Edge> getEdges() {
        return new IdEdgeIterable(baseGraph.getEdges());
    }

    public Iterable<Edge> getEdges(final String key, final Object value) {
        if (key.equals(ID)) {
            throw new IllegalArgumentException("Index key " + ID + " is reserved by IdGraph");
        } else {
            return new IdEdgeIterable(baseGraph.getEdges(key, value));
        }
    }

    // Note: this is a no-op if the base graph is not an instance of TransactionalGraph
    public void stopTransaction(Conclusion conclusion) {
        if (baseGraph instanceof TransactionalGraph) {
            ((TransactionalGraph) baseGraph).stopTransaction(conclusion);
        }
    }

    public void shutdown() {
        baseGraph.shutdown();
    }

    private static void verifyNativeElement(final Element e) {
        if (!(e instanceof IdElement)) {
            throw new IllegalArgumentException("Given element was not created in this graph");
        }
    }

    @Override
    public String toString() {
        return StringFactory.graphString(this, this.baseGraph.toString());
    }

    public <T extends Element> void dropKeyIndex(final String key, final Class<T> elementClass) {
        if (key.equals(ID)) {
            throw new IllegalArgumentException("Index key " + ID + " is reserved by IdGraph");
        } else {
            baseGraph.dropKeyIndex(key, elementClass);
        }
    }

    public <T extends Element> void createKeyIndex(final String key, final Class<T> elementClass) {
        if (key.equals(ID)) {
            throw new IllegalArgumentException("Index key " + ID + " is reserved by IdGraph");
        } else {
            baseGraph.createKeyIndex(key, elementClass);
        }
    }

    public <T extends Element> Set<String> getIndexedKeys(final Class<T> elementClass) {
        final Set<String> keys = new HashSet<String>();
        keys.addAll(baseGraph.getIndexedKeys(elementClass));
        keys.remove(ID);
        return keys;
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
                ? (Index<T>) new IdVertexIndex((Index<Vertex>) ((IndexableGraph) baseGraph).createIndex(indexName, indexClass, indexParameters))
                : (Index<T>) new IdEdgeIndex((Index<Edge>) ((IndexableGraph) baseGraph).createIndex(indexName, indexClass, indexParameters));
    }

    public <T extends Element> Index<T> getIndex(final String indexName,
                                                 final Class<T> indexClass) {
        verifyBaseGraphIsIndexableGraph();

        if (isVertexClass(indexClass)) {
            Index<Vertex> baseIndex = (Index<Vertex>) ((IndexableGraph) baseGraph).getIndex(indexName, indexClass);
            return null == baseIndex ? null : (Index<T>) new IdVertexIndex(baseIndex);
        } else {
            Index<Edge> baseIndex = (Index<Edge>) ((IndexableGraph) baseGraph).getIndex(indexName, indexClass);
            return null == baseIndex ? null : (Index<T>) new IdEdgeIndex(baseIndex);
        }
    }

    public Iterable<Index<? extends Element>> getIndices() {
        throw new UnsupportedOperationException("sorry, you currently can't get a list of indexes through IdGraph");
    }

    public void dropIndex(final String indexName) {
        verifyBaseGraphIsIndexableGraph();

        ((IndexableGraph) baseGraph).dropIndex(indexName);
    }

    private void verifyBaseGraphIsIndexableGraph() {
        if (!(baseGraph instanceof IndexableGraph)) {
            throw new IllegalStateException("base graph is not an indexable graph");
        }
    }

    private boolean isVertexClass(final Class c) {
        return Vertex.class.isAssignableFrom(c);
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
}
