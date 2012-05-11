package com.tinkerpop.blueprints.pgm.impls.tg;


import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Features;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.KeyIndexableGraph;
import com.tinkerpop.blueprints.pgm.Parameter;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.PropertyFilteredIterable;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A in-memory, reference implementation of the property graph interfaces provided by Blueprints.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class TinkerGraph implements IndexableGraph, KeyIndexableGraph, Serializable {

    private Long currentId = 0l;
    protected Map<String, Vertex> vertices = new HashMap<String, Vertex>();
    protected Map<String, Edge> edges = new HashMap<String, Edge>();
    protected Map<String, TinkerIndex> indices = new HashMap<String, TinkerIndex>();

    protected TinkerAutomaticIndex<TinkerVertex> vertexIndex = new TinkerAutomaticIndex<TinkerVertex>(TinkerVertex.class, this);
    protected TinkerAutomaticIndex<TinkerEdge> edgeIndex = new TinkerAutomaticIndex<TinkerEdge>(TinkerEdge.class, this);

    private final String directory;
    private static final String GRAPH_FILE = "/tinkergraph.dat";

    private static final Features FEATURES = new Features();

    static {
        FEATURES.allowDuplicateEdges = true;
        FEATURES.allowSelfLoops = true;
        FEATURES.allowSerializableObjectProperty = true;
        FEATURES.allowBooleanProperty = true;
        FEATURES.allowDoubleProperty = true;
        FEATURES.allowFloatProperty = true;
        FEATURES.allowIntegerProperty = true;
        FEATURES.allowPrimitiveArrayProperty = true;
        FEATURES.allowUniformListProperty = true;
        FEATURES.allowMixedListProperty = true;
        FEATURES.allowLongProperty = true;
        FEATURES.allowMapProperty = true;
        FEATURES.allowStringProperty = true;

        FEATURES.ignoresSuppliedIds = false;
        FEATURES.isPersistent = true;
        FEATURES.isRDFModel = false;
        FEATURES.isWrapper = false;

        FEATURES.supportsIndices = true;
        FEATURES.supportsKeyIndices = true;
        FEATURES.supportsVertexKeyIndex = true;
        FEATURES.supportsEdgeKeyIndex = true;
        FEATURES.supportsVertexIndex = true;
        FEATURES.supportsEdgeIndex = true;
        FEATURES.supportsTransactions = false;
        FEATURES.supportsVertexIteration = true;
        FEATURES.supportsEdgeIteration = true;
    }

    public TinkerGraph(final String directory) {
        this.directory = directory;
        try {
            final File file = new File(directory);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    throw new RuntimeException("Could not create directory.");
                }
            } else {
                ObjectInputStream input = new ObjectInputStream(new FileInputStream(directory + GRAPH_FILE));
                TinkerGraph temp = (TinkerGraph) input.readObject();
                input.close();
                this.currentId = temp.currentId;
                this.vertices = temp.vertices;
                this.edges = temp.edges;
                this.indices = temp.indices;
                this.vertexIndex = temp.vertexIndex;
                this.edgeIndex = temp.edgeIndex;
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public TinkerGraph() {
        this.directory = null;
    }

    public Iterable<Vertex> getVertices(final String key, final Object value) {
        if (vertexIndex.getIndexedKeys().contains(key)) {
            return (Iterable) vertexIndex.get(key, value);
        } else {
            return new PropertyFilteredIterable<Vertex>(key, value, this.getVertices());
        }
    }

    public Iterable<Edge> getEdges(final String key, final Object value) {
        if (edgeIndex.getIndexedKeys().contains(key)) {
            return (Iterable) edgeIndex.get(key, value);
        } else {
            return new PropertyFilteredIterable<Edge>(key, value, this.getEdges());
        }
    }

    public <T extends Element> void createKeyIndex(final String key, final Class<T> elementClass) {
        if (Vertex.class.isAssignableFrom(elementClass)) {
            this.vertexIndex.createKeyIndex(key);
        } else if (Edge.class.isAssignableFrom(elementClass)) {
            this.edgeIndex.createKeyIndex(key);
        } else {
            throw new IllegalArgumentException("The class " + elementClass + " is not indexable");
        }
    }

    public <T extends Element> void dropKeyIndex(final String key, final Class<T> elementClass) {
        if (Vertex.class.isAssignableFrom(elementClass)) {
            this.vertexIndex.dropKeyIndex(key);
        } else if (Edge.class.isAssignableFrom(elementClass)) {
            this.edgeIndex.dropKeyIndex(key);
        } else {
            throw new IllegalArgumentException("The class " + elementClass + " is not indexable");
        }
    }

    public <T extends Element> Set<String> getIndexedKeys(final Class<T> elementClass) {
        if (Vertex.class.isAssignableFrom(elementClass)) {
            return this.vertexIndex.getIndexedKeys();
        } else if (Edge.class.isAssignableFrom(elementClass)) {
            return this.edgeIndex.getIndexedKeys();
        } else {
            throw new IllegalArgumentException("The class " + elementClass + " is not indexable");
        }
    }

    public <T extends Element> Index<T> createIndex(final String indexName, final Class<T> indexClass, final Parameter... indexParameters) {
        if (this.indices.containsKey(indexName))
            throw new RuntimeException("Index already exists: " + indexName);

        final TinkerIndex index = new TinkerIndex(indexName, indexClass);
        this.indices.put(index.getIndexName(), index);
        return index;
    }

    public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
        Index index = this.indices.get(indexName);
        if (null == index)
            return null;
        if (!indexClass.isAssignableFrom(index.getIndexClass()))
            throw new RuntimeException(indexClass + " is not assignable from " + index.getIndexClass());
        else
            return (Index<T>) index;
    }

    public Iterable<Index<? extends Element>> getIndices() {
        final List<Index<? extends Element>> list = new ArrayList<Index<? extends Element>>();
        for (Index index : indices.values()) {
            list.add(index);
        }
        return list;
    }

    public void dropIndex(final String indexName) {
        this.indices.remove(indexName);
    }


    public Vertex addVertex(final Object id) {
        String idString = null;
        Vertex vertex;
        if (null != id) {
            idString = id.toString();
            vertex = this.vertices.get(idString);
            if (null != vertex) {
                throw new RuntimeException("Vertex with id " + idString + " already exists");
            }
        } else {
            boolean done = false;
            while (!done) {
                idString = this.getNextId();
                vertex = this.vertices.get(idString);
                if (null == vertex)
                    done = true;
            }
        }

        vertex = new TinkerVertex(idString, this);
        this.vertices.put(vertex.getId().toString(), vertex);
        return vertex;

    }

    public Vertex getVertex(final Object id) {
        if (null == id)
            throw new IllegalArgumentException("Element identifier cannot be null");

        String idString = id.toString();
        return this.vertices.get(idString);
    }

    public Edge getEdge(final Object id) {
        if (null == id)
            throw new IllegalArgumentException("Element identifier cannot be null");

        String idString = id.toString();
        return this.edges.get(idString);
    }


    public Iterable<Vertex> getVertices() {
        return new ArrayList<Vertex>(vertices.values());
    }

    public Iterable<Edge> getEdges() {
        return new ArrayList<Edge>(edges.values());
    }

    public void removeVertex(final Vertex vertex) {
        for (Edge edge : vertex.getInEdges()) {
            this.removeEdge(edge);
        }
        for (Edge edge : vertex.getOutEdges()) {
            this.removeEdge(edge);
        }

        this.vertexIndex.removeElement((TinkerVertex) vertex);
        for (Index index : this.getIndices()) {
            if (Vertex.class.isAssignableFrom(index.getIndexClass())) {
                TinkerIndex<TinkerVertex> idx = (TinkerIndex<TinkerVertex>) index;
                idx.removeElement((TinkerVertex) vertex);
            }
        }

        this.vertices.remove(vertex.getId().toString());
    }

    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        String idString = null;
        Edge edge;
        if (null != id) {
            idString = id.toString();
            edge = this.edges.get(idString);
            if (null != edge) {
                throw new RuntimeException("Edge with id " + id + " already exists");
            }
        } else {
            boolean done = false;
            while (!done) {
                idString = this.getNextId();
                edge = this.edges.get(idString);
                if (null == edge)
                    done = true;
            }
        }

        edge = new TinkerEdge(idString, outVertex, inVertex, label, this);
        this.edges.put(edge.getId().toString(), edge);
        final TinkerVertex out = (TinkerVertex) outVertex;
        final TinkerVertex in = (TinkerVertex) inVertex;
        out.addOutEdge(label, edge);
        in.addInEdge(label, edge);
        return edge;

    }

    public void removeEdge(final Edge edge) {
        TinkerVertex outVertex = (TinkerVertex) edge.getOutVertex();
        TinkerVertex inVertex = (TinkerVertex) edge.getInVertex();
        if (null != outVertex && null != outVertex.outEdges) {
            final Set<Edge> edges = outVertex.outEdges.get(edge.getLabel());
            if (null != edges)
                edges.remove(edge);
        }
        if (null != inVertex && null != inVertex.inEdges) {
            final Set<Edge> edges = inVertex.inEdges.get(edge.getLabel());
            if (null != edges)
                edges.remove(edge);
        }


        this.edgeIndex.removeElement((TinkerEdge) edge);
        for (Index index : this.getIndices()) {
            if (Edge.class.isAssignableFrom(index.getIndexClass())) {
                TinkerIndex<TinkerEdge> idx = (TinkerIndex<TinkerEdge>) index;
                idx.removeElement((TinkerEdge) edge);
            }
        }

        this.edges.remove(edge.getId().toString());
    }


    public String toString() {
        if (null == this.directory)
            return StringFactory.graphString(this, "vertices:" + this.vertices.size() + " edges:" + this.edges.size());
        else
            return StringFactory.graphString(this, "vertices:" + this.vertices.size() + " edges:" + this.edges.size() + " directory:" + this.directory);
    }

    public void clear() {
        this.vertices.clear();
        this.edges.clear();
        this.indices.clear();
        this.currentId = 0l;
        this.vertexIndex = new TinkerAutomaticIndex<TinkerVertex>(TinkerVertex.class, this);
        this.edgeIndex = new TinkerAutomaticIndex<TinkerEdge>(TinkerEdge.class, this);
    }

    public void shutdown() {
        if (null != this.directory) {
            try {
                File file = new File(this.directory + GRAPH_FILE);
                if (file.exists()) {
                    file.delete();
                }
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(this.directory + GRAPH_FILE));
                out.writeObject(this);
                out.close();
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    private String getNextId() {
        String idString;
        while (true) {
            idString = this.currentId.toString();
            this.currentId++;
            if (null == this.vertices.get(idString) || null == this.edges.get(idString) || this.currentId == Long.MAX_VALUE)
                break;
        }
        return idString;
    }

    public Features getFeatures() {
        return FEATURES;
    }

    protected class TinkerAutomaticIndex<T extends TinkerElement> extends TinkerIndex<T> implements Serializable {

        private final Set<String> indexedKeys = new HashSet<String>();
        private TinkerGraph graph;

        public TinkerAutomaticIndex(final Class<T> indexClass, final TinkerGraph graph) {
            super(null, indexClass);
            this.graph = graph;
        }

        public void autoUpdate(final String key, final Object newValue, final Object oldValue, final T element) {
            if (this.indexedKeys.contains(key)) {
                if (oldValue != null)
                    this.remove(key, oldValue, element);
                this.put(key, newValue, element);
            }
        }

        public void autoRemove(final String key, final Object oldValue, final T element) {
            if (this.indexedKeys.contains(key)) {
                this.remove(key, oldValue, element);
            }
        }

        public void createKeyIndex(final String key) {
            if (this.indexedKeys.contains(key))
                return;

            this.indexedKeys.add(key);
            if (TinkerVertex.class.equals(this.indexClass)) {
                for (final Vertex vertex : graph.getVertices()) {
                    if (vertex.getPropertyKeys().contains(key)) {
                        this.put(key, vertex.getProperty(key), (T) vertex);
                    }
                }
            } else {
                for (final Edge edge : graph.getEdges()) {
                    if (edge.getPropertyKeys().contains(key)) {
                        this.put(key, edge.getProperty(key), (T) edge);
                    }
                }
            }
        }

        public void dropKeyIndex(final String key) {
            if (!this.indexedKeys.contains(key))
                return;

            this.indexedKeys.remove(key);
            this.index.remove(key);

        }

        public Set<String> getIndexedKeys() {
            return new HashSet<String>(this.indexedKeys);
        }
    }

}
