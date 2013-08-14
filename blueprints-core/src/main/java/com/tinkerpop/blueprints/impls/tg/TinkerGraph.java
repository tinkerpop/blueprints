package com.tinkerpop.blueprints.impls.tg;


import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.DefaultGraphQuery;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import com.tinkerpop.blueprints.util.KeyIndexableGraphHelper;
import com.tinkerpop.blueprints.util.PropertyFilteredIterable;
import com.tinkerpop.blueprints.util.StringFactory;
import org.apache.commons.configuration.Configuration;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An in-memory, reference implementation of the property graph interfaces provided by Blueprints.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class TinkerGraph implements IndexableGraph, KeyIndexableGraph, Serializable {

    protected Long currentId = 0l;
    protected Map<String, Vertex> vertices = new HashMap<String, Vertex>();
    protected Map<String, Edge> edges = new HashMap<String, Edge>();
    protected Map<String, TinkerIndex> indices = new HashMap<String, TinkerIndex>();

    protected TinkerKeyIndex<TinkerVertex> vertexKeyIndex = new TinkerKeyIndex<TinkerVertex>(TinkerVertex.class, this);
    protected TinkerKeyIndex<TinkerEdge> edgeKeyIndex = new TinkerKeyIndex<TinkerEdge>(TinkerEdge.class, this);

    private final String directory;
    private final FileType fileType;

    private static final Features FEATURES = new Features();
    private static final Features PERSISTENT_FEATURES;

    static {
        FEATURES.supportsDuplicateEdges = true;
        FEATURES.supportsSelfLoops = true;
        FEATURES.supportsSerializableObjectProperty = true;
        FEATURES.supportsBooleanProperty = true;
        FEATURES.supportsDoubleProperty = true;
        FEATURES.supportsFloatProperty = true;
        FEATURES.supportsIntegerProperty = true;
        FEATURES.supportsPrimitiveArrayProperty = true;
        FEATURES.supportsUniformListProperty = true;
        FEATURES.supportsMixedListProperty = true;
        FEATURES.supportsLongProperty = true;
        FEATURES.supportsMapProperty = true;
        FEATURES.supportsStringProperty = true;

        FEATURES.ignoresSuppliedIds = false;
        FEATURES.isPersistent = false;
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
        FEATURES.supportsEdgeRetrieval = true;
        FEATURES.supportsVertexProperties = true;
        FEATURES.supportsEdgeProperties = true;
        FEATURES.supportsThreadedTransactions = false;

        PERSISTENT_FEATURES = FEATURES.copyFeatures();
        PERSISTENT_FEATURES.isPersistent = true;
    }

    public enum FileType {
        JAVA,
        GML,
        GRAPHML,
        GRAPHSON
    }

    public TinkerGraph(final Configuration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration cannot be null");
        }

        this.directory = configuration.getString("blueprints.tg.directory", null);
        this.fileType = FileType.valueOf(configuration.getString("blueprints.tg.file-type", "JAVA"));

        if (directory != null) {
            this.init();
        }
    }

    public TinkerGraph(final String directory, final FileType fileType) {
        this.directory = directory;
        this.fileType = fileType;
        this.init();
    }

    public TinkerGraph(final String directory) {
        this(directory, FileType.JAVA);
    }

    public TinkerGraph() {
        this.directory = null;
        this.fileType = FileType.JAVA;
    }

    private void init() {
        try {
            final File file = new File(directory);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    throw new RuntimeException("Could not create directory");
                }
            } else {
                final TinkerStorage tinkerStorage = TinkerStorageFactory.getInstance().getTinkerStorage(fileType);
                final TinkerGraph graph = tinkerStorage.load(directory);

                this.vertices = graph.vertices;
                this.edges = graph.edges;
                this.currentId = graph.currentId;
                this.indices = graph.indices;
                this.vertexKeyIndex = graph.vertexKeyIndex;
                this.edgeKeyIndex = graph.edgeKeyIndex;
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Iterable<Vertex> getVertices(final String key, final Object value) {
        if (vertexKeyIndex.getIndexedKeys().contains(key)) {
            return (Iterable) vertexKeyIndex.get(key, value);
        } else {
            return new PropertyFilteredIterable<Vertex>(key, value, this.getVertices());
        }
    }

    public Iterable<Edge> getEdges(final String key, final Object value) {
        if (edgeKeyIndex.getIndexedKeys().contains(key)) {
            return (Iterable) edgeKeyIndex.get(key, value);
        } else {
            return new PropertyFilteredIterable<Edge>(key, value, this.getEdges());
        }
    }

    public <T extends Element> void createKeyIndex(final String key, final Class<T> elementClass, final Parameter... indexParameters) {
        if (elementClass == null)
            throw ExceptionFactory.classForElementCannotBeNull();

        if (Vertex.class.isAssignableFrom(elementClass)) {
            this.vertexKeyIndex.createKeyIndex(key);
        } else if (Edge.class.isAssignableFrom(elementClass)) {
            this.edgeKeyIndex.createKeyIndex(key);
        } else {
            throw ExceptionFactory.classIsNotIndexable(elementClass);
        }
    }

    public <T extends Element> void dropKeyIndex(final String key, final Class<T> elementClass) {
        if (elementClass == null)
            throw ExceptionFactory.classForElementCannotBeNull();

        if (Vertex.class.isAssignableFrom(elementClass)) {
            this.vertexKeyIndex.dropKeyIndex(key);
        } else if (Edge.class.isAssignableFrom(elementClass)) {
            this.edgeKeyIndex.dropKeyIndex(key);
        } else {
            throw ExceptionFactory.classIsNotIndexable(elementClass);
        }
    }

    public <T extends Element> Set<String> getIndexedKeys(final Class<T> elementClass) {
        if (elementClass == null)
            throw ExceptionFactory.classForElementCannotBeNull();

        if (Vertex.class.isAssignableFrom(elementClass)) {
            return this.vertexKeyIndex.getIndexedKeys();
        } else if (Edge.class.isAssignableFrom(elementClass)) {
            return this.edgeKeyIndex.getIndexedKeys();
        } else {
            throw ExceptionFactory.classIsNotIndexable(elementClass);
        }
    }

    public <T extends Element> Index<T> createIndex(final String indexName, final Class<T> indexClass, final Parameter... indexParameters) {
        if (this.indices.containsKey(indexName))
            throw ExceptionFactory.indexAlreadyExists(indexName);

        final TinkerIndex index = new TinkerIndex(indexName, indexClass);
        this.indices.put(index.getIndexName(), index);
        return index;
    }

    public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
        Index index = this.indices.get(indexName);
        if (null == index)
            return null;
        if (!indexClass.isAssignableFrom(index.getIndexClass()))
            throw ExceptionFactory.indexDoesNotSupportClass(indexName, indexClass);
        else
            return index;
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
                throw ExceptionFactory.vertexWithIdAlreadyExists(id);
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
            throw ExceptionFactory.vertexIdCanNotBeNull();

        String idString = id.toString();
        return this.vertices.get(idString);
    }

    public Edge getEdge(final Object id) {
        if (null == id)
            throw ExceptionFactory.edgeIdCanNotBeNull();

        String idString = id.toString();
        return this.edges.get(idString);
    }


    public Iterable<Vertex> getVertices() {
        return new ArrayList<Vertex>(this.vertices.values());
    }

    public Iterable<Edge> getEdges() {
        return new ArrayList<Edge>(this.edges.values());
    }

    public void removeVertex(final Vertex vertex) {
        if (!this.vertices.containsKey(vertex.getId().toString()))
            throw ExceptionFactory.vertexWithIdDoesNotExist(vertex.getId());

        for (Edge edge : vertex.getEdges(Direction.BOTH)) {
            this.removeEdge(edge);
        }

        this.vertexKeyIndex.removeElement((TinkerVertex) vertex);
        for (Index index : this.getIndices()) {
            if (Vertex.class.isAssignableFrom(index.getIndexClass())) {
                TinkerIndex<TinkerVertex> idx = (TinkerIndex<TinkerVertex>) index;
                idx.removeElement((TinkerVertex) vertex);
            }
        }

        this.vertices.remove(vertex.getId().toString());
    }

    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        if (label == null)
            throw ExceptionFactory.edgeLabelCanNotBeNull();

        String idString = null;
        Edge edge;
        if (null != id) {
            idString = id.toString();
            edge = this.edges.get(idString);
            if (null != edge) {
                throw ExceptionFactory.edgeWithIdAlreadyExist(id);
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
        TinkerVertex outVertex = (TinkerVertex) edge.getVertex(Direction.OUT);
        TinkerVertex inVertex = (TinkerVertex) edge.getVertex(Direction.IN);
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


        this.edgeKeyIndex.removeElement((TinkerEdge) edge);
        for (Index index : this.getIndices()) {
            if (Edge.class.isAssignableFrom(index.getIndexClass())) {
                TinkerIndex<TinkerEdge> idx = (TinkerIndex<TinkerEdge>) index;
                idx.removeElement((TinkerEdge) edge);
            }
        }

        this.edges.remove(edge.getId().toString());
    }

    public GraphQuery query() {
        return new DefaultGraphQuery(this);
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
        this.vertexKeyIndex = new TinkerKeyIndex<TinkerVertex>(TinkerVertex.class, this);
        this.edgeKeyIndex = new TinkerKeyIndex<TinkerEdge>(TinkerEdge.class, this);
    }

    public void shutdown() {
        if (null != this.directory) {
            try {
                final TinkerStorage tinkerStorage = TinkerStorageFactory.getInstance().getTinkerStorage(this.fileType);
                tinkerStorage.save(this, this.directory);
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
        if (null == directory)
            return FEATURES;
        else
            return PERSISTENT_FEATURES;
    }

    protected class TinkerKeyIndex<T extends TinkerElement> extends TinkerIndex<T> implements Serializable {

        private final Set<String> indexedKeys = new HashSet<String>();
        private TinkerGraph graph;

        public TinkerKeyIndex(final Class<T> indexClass, final TinkerGraph graph) {
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
                KeyIndexableGraphHelper.reIndexElements(graph, graph.getVertices(), new HashSet<String>(Arrays.asList(key)));
            } else {
                KeyIndexableGraphHelper.reIndexElements(graph, graph.getEdges(), new HashSet<String>(Arrays.asList(key)));
            }
        }

        public void dropKeyIndex(final String key) {
            if (!this.indexedKeys.contains(key))
                return;

            this.indexedKeys.remove(key);
            this.index.remove(key);

        }

        public Set<String> getIndexedKeys() {
            if (null != this.indexedKeys)
                return new HashSet<String>(this.indexedKeys);
            else
                return Collections.emptySet();
        }
    }

}
