package com.tinkerpop.blueprints.util.wrappers.id;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.StringFactory;
import com.tinkerpop.blueprints.util.wrappers.WrapperGraph;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

/**
 * A KeyIndexableGraph implementation which wraps another KeyIndexableGraph implementation,
 * enabling custom element IDs even for those graphs which don't otherwise support them.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class IdGraph<T extends KeyIndexableGraph> implements KeyIndexableGraph, WrapperGraph<T> {

    // Note: using "__id" instead of "_id" avoids collision with Rexster's "_id"
    public static final String ID = "__id";

    private final T baseGraph;
    private final IdFactory idFactory;

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
     * @param idFactory a factory for new element IDs.
     *                  When vertices or edges are created using null IDs, the actual IDs are chosen based on this factory.
     */
    public IdGraph(final T baseGraph, final IdFactory idFactory) {
        this.baseGraph = baseGraph;
        this.features = this.baseGraph.getFeatures().copyFeatures();
        features.isWrapper = true;
        features.ignoresSuppliedIds = false;

        this.idFactory = null == idFactory
                ? new IdFactory() {
            public Object createId() {
                return UUID.randomUUID().toString();
            }
        } : idFactory;

        createIndices();
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
        base.setProperty(ID, null == id ? idFactory.createId() : id);
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

        Edge e = baseGraph.addEdge(null, ((IdVertex) outVertex).getBaseVertex(), ((IdVertex) inVertex).getBaseVertex(), label);

        e.setProperty(ID, null == id ? idFactory.createId() : id);

        return new IdEdge(e);
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

    /**
     * A factory for IDs of newly-created vertices and edges (where an ID is not otherwise specified).
     */
    public static interface IdFactory {
        Object createId();
    }
}
