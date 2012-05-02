package com.tinkerpop.blueprints.pgm.util.wrappers.id;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.CloseableSequence;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Parameter;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * An IndexableGraph implementation which wraps another IndexableGraph implementation,
 * enabling custom element IDs even for those graphs which don't otherwise support them.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class IdGraph implements IndexableGraph {
    private static final Logger LOGGER = Logger.getLogger(IdGraph.class.getName());

    // Note: using "__id" instead of "_id" avoids collision with Rexster's "_id"
    public static final String ID = "__id";

    public static final String
            VERTEX_IDS = "__vertex-ids",
            EDGE_IDS = "__edge-ids";

    private final IndexableGraph baseGraph;
    private Index<Vertex> vertexIds;
    private Index<Edge> edgeIds;
    private final IdFactory idFactory;

    /**
     * Adds custom ID functionality to the given graph.
     *
     * @param baseGraph the base graph which does not permit custom element IDs
     */
    public IdGraph(final IndexableGraph baseGraph) {
        this(baseGraph, null);
    }

    /**
     * Adds custom ID functionality to the given graph, also specifying a factory for new element IDs.
     *
     * @param baseGraph the base graph which may or may not permit custom element IDs
     * @param idFactory a factory for new element IDs.
     *                  When vertices or edges are created using null IDs, the actual IDs are chosen based on this factory.
     */
    public IdGraph(final IndexableGraph baseGraph,
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
        Set<String> ids = new HashSet<String>();
        ids.add(ID);

        vertexIds = null == baseGraph.getIndex(VERTEX_IDS, Vertex.class)
                ? baseGraph.createAutomaticIndex(VERTEX_IDS, Vertex.class, ids)
                : baseGraph.getIndex(VERTEX_IDS, Vertex.class);

        edgeIds = null == baseGraph.getIndex(EDGE_IDS, Edge.class)
                ? baseGraph.createAutomaticIndex(EDGE_IDS, Edge.class, ids)
                : baseGraph.getIndex(EDGE_IDS, Edge.class);
    }

    public <T extends Element> Index<T> createManualIndex(final String s,
                                                          final Class<T> tClass,
                                                          final Parameter... params) {
        if (0 < params.length) {
            LOGGER.warning("index parameters will be ignored");
        }

        if (s.equals(VERTEX_IDS) || s.equals(EDGE_IDS)) {
            throw new IllegalArgumentException("can't create index with reserved name '" + s + "'");
        }

        Index<T> index = baseGraph.createManualIndex(s, tClass);

        if (Vertex.class.isAssignableFrom(tClass)) {
            return (Index<T>) new VertexIndexWrapper((Index<Vertex>) index);
        } else if (Edge.class.isAssignableFrom(tClass)) {
            return (Index<T>) new EdgeIndexWrapper((Index<Edge>) index);
        } else {
            throw new IllegalStateException("unexpected index class: " + tClass);
        }
    }

    public <T extends Element> AutomaticIndex<T> createAutomaticIndex(final String s,
                                                                      final Class<T> tClass,
                                                                      final Set<String> strings,
                                                                      final Parameter... params) {
        if (0 < params.length) {
            LOGGER.warning("index parameters will be ignored");
        }

        if (s.equals(VERTEX_IDS) || s.equals(EDGE_IDS)) {
            throw new IllegalArgumentException("can't create index with reserved name '" + s + "'");
        }

        if (null != strings && strings.contains(ID)) {
            throw new IllegalArgumentException("can't index on reserved property '" + ID + "'");
        }

        AutomaticIndex<T> index = baseGraph.createAutomaticIndex(s, tClass, strings);

        if (Vertex.class.isAssignableFrom(tClass)) {
            return (AutomaticIndex<T>) new AutoVertexIndexWrapper((Index<Vertex>) index);
        } else if (Edge.class.isAssignableFrom(tClass)) {
            return (AutomaticIndex<T>) new AutoEdgeIndexWrapper((Index<Edge>) index);
        } else {
            throw new IllegalStateException("unexpected index class: " + tClass);
        }
    }

    public <T extends Element> Index<T> getIndex(String s, Class<T> tClass) {
        if (s.equals(VERTEX_IDS) || s.equals(EDGE_IDS)) {
            return null;
        } else {
            Index<T> index = baseGraph.getIndex(s, tClass);

            if (null == index) {
                return null;
            }

            if (Vertex.class.isAssignableFrom(tClass)) {
                if (Index.Type.AUTOMATIC.equals(index.getIndexType())) {
                    return (Index<T>) new AutoVertexIndexWrapper((Index<Vertex>) index);
                } else {
                    return (Index<T>) new VertexIndexWrapper((Index<Vertex>) index);
                }
            } else if (Edge.class.isAssignableFrom(tClass)) {
                if (Index.Type.AUTOMATIC.equals(index.getIndexType())) {
                    return (Index<T>) new AutoEdgeIndexWrapper((Index<Edge>) index);
                } else {
                    return (Index<T>) new EdgeIndexWrapper((Index<Edge>) index);
                }
            } else {
                throw new IllegalStateException("unexpected index class: " + tClass);
            }
        }
    }

    public Iterable<Index<? extends Element>> getIndices() {
        Collection<Index<? extends Element>> indices = new LinkedList<Index<? extends Element>>();

        for (Index<? extends Element> i : baseGraph.getIndices()) {
            if (!(i.getIndexName().equals(VERTEX_IDS) || i.getIndexName().equals(EDGE_IDS))) {

                if (Vertex.class.isAssignableFrom(i.getIndexClass())) {
                    if (Index.Type.AUTOMATIC.equals(i.getIndexType())) {
                        indices.add(new AutoVertexIndexWrapper((Index<Vertex>) i));
                    } else {
                        indices.add(new VertexIndexWrapper((Index<Vertex>) i));
                    }
                } else if (Edge.class.isAssignableFrom(i.getIndexClass())) {
                    if (Index.Type.AUTOMATIC.equals(i.getIndexType())) {
                        indices.add(new AutoEdgeIndexWrapper((Index<Edge>) i));
                    } else {
                        indices.add(new EdgeIndexWrapper((Index<Edge>) i));
                    }
                } else {
                    throw new IllegalStateException("unexpected index class: " + i.getIndexClass());
                }
            }
        }

        return indices;
    }

    public void dropIndex(String s) {
        if (!(s.equals(VERTEX_IDS) || s.equals(EDGE_IDS))) {
            baseGraph.dropIndex(s);
        }
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

        CloseableSequence<Vertex> i = vertexIds.get(ID, id);
        try {
            if (!i.hasNext()) {
                return null;
            } else {
                Vertex e = i.next();

                if (i.hasNext()) {
                    throw new IllegalStateException("multiple vertices exist with id '" + id + "'");
                }

                return new IdVertex(e);
            }
        } finally {
            i.close();
        }
    }

    public void removeVertex(Vertex vertex) {
        verifyNativeElement(vertex);

        baseGraph.removeVertex(((IdVertex) vertex).getBase());
    }

    public Iterable<Vertex> getVertices() {
        return new IdVertexIterable(baseGraph.getVertices());
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

        CloseableSequence<Edge> i = edgeIds.get(ID, id);
        try {
            if (!i.hasNext()) {
                return null;
            } else {
                Edge e = i.next();

                if (i.hasNext()) {
                    throw new IllegalStateException("multiple edges exist with id '" + id + "'");
                }

                return new IdEdge(e);
            }
        } finally {
            i.close();
        }
    }

    public void removeEdge(Edge edge) {
        verifyNativeElement(edge);

        baseGraph.removeEdge(((IdEdge) edge).getBase());
    }

    public Iterable<Edge> getEdges() {
        return new IdEdgeIterable(baseGraph.getEdges());
    }

    public void clear() {
        baseGraph.clear();
        createIndices();
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

    /**
     * A factory for IDs of newly-created vertices and edges (where an ID is not otherwise specified).
     */
    public static interface IdFactory {
        Object createId();
    }

    private static class VertexIndexWrapper implements Index<Vertex> {
        protected final Index<Vertex> base;

        public VertexIndexWrapper(Index<Vertex> base) {
            this.base = base;
        }

        public String getIndexName() {
            return base.getIndexName();
        }

        public Class<Vertex> getIndexClass() {
            return base.getIndexClass();
        }

        public Type getIndexType() {
            return base.getIndexType();
        }

        public void put(String s, Object o, Vertex vertex) {
            verifyNativeElement(vertex);

            base.put(s, o, ((IdVertex) vertex).getBase());
        }

        public CloseableSequence<Vertex> get(String s, Object o) {
            return new CloseableVertexSequence(base.get(s, o));
        }

        public long count(String s, Object o) {
            return base.count(s, o);
        }

        public void remove(String s, Object o, Vertex vertex) {
            verifyNativeElement(vertex);

            base.remove(s, o, ((IdVertex) vertex).getBase());
        }
    }

    private static class AutoVertexIndexWrapper extends VertexIndexWrapper implements AutomaticIndex<Vertex> {
        public AutoVertexIndexWrapper(Index<Vertex> base) {
            super(base);
        }

        public Set<String> getAutoIndexKeys() {
            return ((AutomaticIndex) base).getAutoIndexKeys();
        }
    }

    private class EdgeIndexWrapper implements Index<Edge> {
        protected final Index<Edge> base;

        public EdgeIndexWrapper(Index<Edge> base) {
            this.base = base;
        }

        public String getIndexName() {
            return base.getIndexName();
        }

        public Class<Edge> getIndexClass() {
            return base.getIndexClass();
        }

        public Type getIndexType() {
            return base.getIndexType();
        }

        public void put(String s, Object o, Edge edge) {
            verifyNativeElement(edge);

            base.put(s, o, ((IdEdge) edge).getBase());
        }

        public CloseableSequence<Edge> get(String s, Object o) {
            return new CloseableEdgeSequence(base.get(s, o));
        }

        public long count(String s, Object o) {
            return base.count(s, o);
        }

        public void remove(String s, Object o, Edge edge) {
            verifyNativeElement(edge);

            base.remove(s, o, ((IdEdge) edge).getBase());
        }
    }

    private class AutoEdgeIndexWrapper extends EdgeIndexWrapper implements AutomaticIndex<Edge> {
        public AutoEdgeIndexWrapper(Index<Edge> base) {
            super(base);
        }

        public Set<String> getAutoIndexKeys() {
            return ((AutomaticIndex) base).getAutoIndexKeys();
        }
    }

    private static class CloseableVertexSequence implements CloseableSequence<Vertex> {
        private final CloseableSequence<Vertex> base;

        public CloseableVertexSequence(CloseableSequence<Vertex> base) {
            this.base = base;
        }

        public void close() {
            base.close();
        }

        public Iterator<Vertex> iterator() {
            return this;
        }

        public boolean hasNext() {
            return base.hasNext();
        }

        public Vertex next() {
            return new IdVertex(base.next());
        }

        public void remove() {
            base.remove();
        }
    }

    private static class CloseableEdgeSequence implements CloseableSequence<Edge> {
        private final CloseableSequence<Edge> base;

        public CloseableEdgeSequence(CloseableSequence<Edge> base) {
            this.base = base;
        }

        public void close() {
            base.close();
        }

        public Iterator<Edge> iterator() {
            return this;
        }

        public boolean hasNext() {
            return base.hasNext();
        }

        public Edge next() {
            return new IdEdge(base.next());
        }

        public void remove() {
            base.remove();
        }
    }

    public static void main(final String[] args) throws Exception {
        System.out.println(UUID.randomUUID().toString());
    }
}
