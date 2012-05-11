package com.tinkerpop.blueprints.pgm.util.wrappers.id;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Features;
import com.tinkerpop.blueprints.pgm.KeyIndexableGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * A KeyIndexableGraph implementation which wraps another KeyIndexableGraph implementation,
 * enabling custom element IDs even for those graphs which don't otherwise support them.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class IdGraph implements KeyIndexableGraph {
    private static final Logger LOGGER = Logger.getLogger(IdGraph.class.getName());

    // Note: using "__id" instead of "_id" avoids collision with Rexster's "_id"
    public static final String ID = "__id";

    private final KeyIndexableGraph baseGraph;
    private final IdFactory idFactory;

    /**
     * Adds custom ID functionality to the given graph.
     *
     * @param baseGraph the base graph which does not permit custom element IDs
     */
    public IdGraph(final KeyIndexableGraph baseGraph) {
        this(baseGraph, null);
    }

    /**
     * Adds custom ID functionality to the given graph, also specifying a factory for new element IDs.
     *
     * @param baseGraph the base graph which may or may not permit custom element IDs
     * @param idFactory a factory for new element IDs.
     *                  When vertices or edges are created using null IDs, the actual IDs are chosen based on this factory.
     */
    public IdGraph(final KeyIndexableGraph baseGraph,
                   final IdFactory idFactory) {
        this.baseGraph = baseGraph;

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
        return baseGraph.getFeatures();
    }

    public Vertex addVertex(Object id) {
        if (null != id && null != getVertex(id)) {
            throw new IllegalArgumentException("vertex with given id already exists: '" + id + "'");
        }

        Vertex base = baseGraph.addVertex(null);

        base.setProperty(ID, null == id ? idFactory.createId() : id);
        return new IdVertex(base);
    }

    public Vertex getVertex(Object id) {
        if (null == id) {
            throw new IllegalArgumentException("Element identifier cannot be null");
        }

        Iterable<Vertex> i = baseGraph.getVertices(ID, id);
        Iterator<Vertex> iter = i.iterator();
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

    public void removeVertex(Vertex vertex) {
        verifyNativeElement(vertex);

        baseGraph.removeVertex(((IdVertex) vertex).getBase());
    }

    public Iterable<Vertex> getVertices() {
        return new IdVertexIterable(baseGraph.getVertices());
    }

    public Iterable<Vertex> getVertices(String key, Object value) {
        if (key.equals(ID)) {
            throw new IllegalArgumentException("index key '" + ID + "' is reserved by IdGraph");
        } else {
            return new IdVertexIterable(baseGraph.getVertices(key, value));
        }
    }

    public Edge addEdge(Object id, Vertex v1, Vertex v2, String s) {
        if (null != id && null != getEdge(id)) {
            throw new IllegalArgumentException("edge with given id already exists: '" + id + "'");
        }

        verifyNativeElement(v1);
        verifyNativeElement(v2);

        Edge e = baseGraph.addEdge(null, ((IdVertex) v1).getBase(), ((IdVertex) v2).getBase(), s);

        e.setProperty(ID, null == id ? idFactory.createId() : id);

        return new IdEdge(e);
    }

    public Edge getEdge(Object id) {
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
                throw new IllegalStateException("multiple edges exist with id '" + id + "'");
            }

            return new IdEdge(e);
        }
    }

    public void removeEdge(Edge edge) {
        verifyNativeElement(edge);

        baseGraph.removeEdge(((IdEdge) edge).getBase());
    }

    public Iterable<Edge> getEdges() {
        return new IdEdgeIterable(baseGraph.getEdges());
    }

    public Iterable<Edge> getEdges(String key, Object value) {
        if (key.equals(ID)) {
            throw new IllegalArgumentException("index key '" + ID + "' is reserved by IdGraph");
        } else {
            return new IdEdgeIterable(baseGraph.getEdges(key, value));
        }
    }

    public void shutdown() {
        baseGraph.shutdown();
    }

    private static void verifyNativeElement(final Element e) {
        if (!(e instanceof IdElement)) {
            throw new IllegalArgumentException("given element was not created in this graph");
        }
    }

    @Override
    public String toString() {
        return StringFactory.graphString(this, "base graph: " + baseGraph.toString());
    }

    public <T extends Element> void dropKeyIndex(String key, Class<T> elementClass) {
        if (key.equals(ID)) {
            throw new IllegalArgumentException("index key '" + ID + "' is reserved by IdGraph");
        } else {
            baseGraph.dropKeyIndex(key, elementClass);
        }
    }

    public <T extends Element> void createKeyIndex(String key, Class<T> elementClass) {
        if (key.equals(ID)) {
            throw new IllegalArgumentException("index key '" + ID + "' is reserved by IdGraph");
        } else {
            baseGraph.createKeyIndex(key, elementClass);
        }
    }

    public <T extends Element> Set<String> getIndexedKeys(Class<T> elementClass) {
        Set<String> keys = new HashSet<String>();
        keys.addAll(baseGraph.getIndexedKeys(elementClass));
        keys.remove(ID);
        return keys;
    }

    /**
     * A factory for IDs of newly-created vertices and edges (where an ID is not otherwise specified).
     */
    public static interface IdFactory {
        Object createId();
    }

    public static void main(final String[] args) throws Exception {
        System.out.println(UUID.randomUUID().toString());
    }
}
