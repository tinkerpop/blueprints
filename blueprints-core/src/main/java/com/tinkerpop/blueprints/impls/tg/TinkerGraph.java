package com.tinkerpop.blueprints.impls.tg;


import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import com.tinkerpop.blueprints.util.KeyIndexableGraphHelper;
import com.tinkerpop.blueprints.util.PropertyFilteredIterable;
import com.tinkerpop.blueprints.util.StringFactory;
import com.tinkerpop.blueprints.util.io.gml.GMLReader;
import com.tinkerpop.blueprints.util.io.gml.GMLWriter;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReader;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONMode;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONReader;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONWriter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

    private Long currentId = 0l;
    protected Map<String, Vertex> vertices = new HashMap<String, Vertex>();
    protected Map<String, Edge> edges = new HashMap<String, Edge>();
    protected Map<String, TinkerIndex> indices = new HashMap<String, TinkerIndex>();

    protected TinkerKeyIndex<TinkerVertex> vertexKeyIndex = new TinkerKeyIndex<TinkerVertex>(TinkerVertex.class, this);
    protected TinkerKeyIndex<TinkerEdge> edgeKeyIndex = new TinkerKeyIndex<TinkerEdge>(TinkerEdge.class, this);

    private final String directory;
    private final FileType fileType;
    private static final String GRAPH_FILE_JAVA = "/tinkergraph.dat";
    private static final String GRAPH_FILE_GML = "/tinkergraph.gml";
    private static final String GRAPH_FILE_GRAPHML = "/tinkergraph.xml";
    private static final String GRAPH_FILE_GSON = "/tinkergraph.json";
    private static final String GRAPH_FILE_METADATA = "/tinkergraph-metadata.dat";

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

    public TinkerGraph(final String directory, final FileType fileType) {
        this.directory = directory;
        this.fileType = fileType;

        try {
            final File file = new File(directory);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    throw new RuntimeException("Could not create directory");
                }
            } else {
                TinkerGraph graph = new TinkerGraph();

                switch (fileType) {
                    case GML:
                        GMLReader.inputGraph(graph, new FileInputStream(directory + GRAPH_FILE_GML));
                        break;

                    case GRAPHML:
                        GraphMLReader.inputGraph(graph, new FileInputStream(directory + GRAPH_FILE_GRAPHML));
                        break;

                    case GRAPHSON:
                        GraphSONReader.inputGraph(graph, new FileInputStream(directory + GRAPH_FILE_GSON));
                        break;

                    case JAVA:
                        ObjectInputStream input = new ObjectInputStream(new FileInputStream(directory + GRAPH_FILE_JAVA));
                        graph = (TinkerGraph) input.readObject();
                        input.close();
                        break;
                }

                this.vertices = graph.vertices;
                this.edges = graph.edges;

                if (fileType != FileType.JAVA) {
                    DataInputStream reader = null;
                    try {
                        reader = new DataInputStream(new FileInputStream(directory + GRAPH_FILE_METADATA));

                        // Read the current ID
                        this.currentId = reader.readLong();

                        // Read the number of indices
                        int indexCount = reader.readInt();
                        for (int i = 0; i < indexCount; i++) {
                            // Read the index name
                            String indexName = reader.readUTF();

                            // Read the index type
                            byte indexType = reader.readByte();

                            if (indexType != 1 && indexType != 2) {
                                throw new RuntimeException("Unknown index class type");
                            }

                            TinkerIndex tinkerIndex = new TinkerIndex(indexName, indexType == 1 ? Vertex.class : Edge.class);

                            // Read the number of items associated with this index name
                            int indexItemCount = reader.readInt();
                            for (int j = 0; j < indexItemCount; j++) {
                                // Read the item key
                                String indexItemKey = reader.readUTF();

                                // Read the number of sub-items associated with this item
                                int indexValueItemSetCount = reader.readInt();
                                for (int k = 0; k < indexValueItemSetCount; k++) {
                                    // Read the number of vertices or edges in this sub-item
                                    int setCount = reader.readInt();
                                    for (int l = 0; l < setCount; l++) {
                                        // Read the vertex or edge identifier
                                        if (indexType == 1) {
                                            Vertex v = graph.getVertex(reader.readUTF());
                                            if (v != null) {
                                                tinkerIndex.put(indexItemKey, v.getProperty(indexItemKey), v);
                                            }
                                        } else if (indexType == 2) {
                                            Edge e = graph.getEdge(reader.readUTF());
                                            if (e != null) {
                                                tinkerIndex.put(indexItemKey, e.getProperty(indexItemKey), e);
                                            }
                                        }
                                    }
                                }
                            }

                            this.indices.put(indexName, tinkerIndex);
                        }

                        // Read the number of vertex key indices
                        indexCount = reader.readInt();
                        for (int i = 0; i < indexCount; i++) {
                            // Read the key index name
                            String indexName = reader.readUTF();

                            this.vertexKeyIndex.createKeyIndex(indexName);

                            Map<Object, Set<TinkerVertex>> items = new HashMap<Object, Set<TinkerVertex>>();

                            // Read the number of items associated with this key index name
                            int itemCount = reader.readInt();
                            for (int j = 0; j < itemCount; j++) {
                                // Read the item key
                                Object key = readTypedData(reader);

                                Set<TinkerVertex> vertices = new HashSet<TinkerVertex>();

                                // Read the number of vertices in this item
                                int vertexCount = reader.readInt();
                                for (int k = 0; k < vertexCount; k++) {
                                    // Read the vertex identifier
                                    Vertex v = graph.getVertex(reader.readUTF());
                                    if (v != null) {
                                        vertices.add((TinkerVertex) v);
                                    }
                                }

                                items.put(key, vertices);
                            }

                            this.vertexKeyIndex.index.put(indexName, items);
                        }

                        // Read the number of edge key indices
                        indexCount = reader.readInt();
                        for (int i = 0; i < indexCount; i++) {
                            // Read the key index name
                            String indexName = reader.readUTF();

                            this.edgeKeyIndex.createKeyIndex(indexName);

                            Map<Object, Set<TinkerEdge>> items = new HashMap<Object, Set<TinkerEdge>>();

                            // Read the number of items associated with this key index name
                            int itemCount = reader.readInt();
                            for (int j = 0; j < itemCount; j++) {
                                // Read the item key
                                Object key = readTypedData(reader);

                                Set<TinkerEdge> edges = new HashSet<TinkerEdge>();

                                // Read the number of edges in this item
                                int edgeCount = reader.readInt();
                                for (int k = 0; k < edgeCount; k++) {
                                    // Read the edge identifier
                                    Edge e = graph.getEdge(reader.readUTF());
                                    if (e != null) {
                                        edges.add((TinkerEdge) e);
                                    }
                                }

                                items.put(key, edges);
                            }

                            this.edgeKeyIndex.index.put(indexName, items);
                        }
                    }
                    catch (IOException e) {
                        throw new RuntimeException("Could not read metadata file");
                    }
                    finally {
                        try {
                            if (reader != null) {
                                reader.close( );
                            }
                        }
                        catch (IOException e) {
                            throw new RuntimeException("Could not read metadata file");
                        }
                    }
                } else {
                    this.currentId = graph.currentId;
                    this.indices = graph.indices;
                    this.vertexKeyIndex = graph.vertexKeyIndex;
                    this.edgeKeyIndex = graph.edgeKeyIndex;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public TinkerGraph(final String directory) {
        this(directory, FileType.JAVA);
    }

    public TinkerGraph() {
        this.directory = null;
        this.fileType = FileType.JAVA;
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
        if (Vertex.class.isAssignableFrom(elementClass)) {
            this.vertexKeyIndex.createKeyIndex(key);
        } else if (Edge.class.isAssignableFrom(elementClass)) {
            this.edgeKeyIndex.createKeyIndex(key);
        } else {
            throw ExceptionFactory.classIsNotIndexable(elementClass);
        }
    }

    public <T extends Element> void dropKeyIndex(final String key, final Class<T> elementClass) {
        if (Vertex.class.isAssignableFrom(elementClass)) {
            this.vertexKeyIndex.dropKeyIndex(key);
        } else if (Edge.class.isAssignableFrom(elementClass)) {
            this.edgeKeyIndex.dropKeyIndex(key);
        } else {
            throw ExceptionFactory.classIsNotIndexable(elementClass);
        }
    }

    public <T extends Element> Set<String> getIndexedKeys(final Class<T> elementClass) {
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
                String path = null;

                switch (this.fileType) {
                    case GML:
                        path = this.directory + GRAPH_FILE_GML;
                        break;

                    case GRAPHML:
                        path = this.directory + GRAPH_FILE_GRAPHML;
                        break;

                    case GRAPHSON:
                        path = this.directory + GRAPH_FILE_GSON;
                        break;

                    case JAVA:
                        path = this.directory + GRAPH_FILE_JAVA;
                        break;
                }

                if (path != null) {
                    final File file = new File(path);
                    if (file.exists()) {
                        file.delete();
                    }

                    switch (this.fileType) {
                       case GML:
                           GMLWriter.outputGraph(this, new FileOutputStream(path));
                           break;

                       case GRAPHML:
                           GraphMLWriter.outputGraph(this, new FileOutputStream(path));
                           break;

                       case GRAPHSON:
                           GraphSONWriter.outputGraph(this, new FileOutputStream(path), GraphSONMode.EXTENDED);
                           break;

                       case JAVA:
                           ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path));
                           out.writeObject(this);
                           out.close();
                           break;
                    }
                }

                if (this.fileType != FileType.JAVA) {
                    final File file = new File(this.directory + GRAPH_FILE_METADATA);
                    if (file.exists()) {
                        file.delete();
                    }

                    DataOutputStream writer = null;
                    try {
                        writer = new DataOutputStream(new FileOutputStream(this.directory + GRAPH_FILE_METADATA));

                        // Write the current ID
                        writer.writeLong(this.currentId);

                        // Write the number of indices
                        writer.writeInt(this.indices.size());
                        for (Map.Entry<String, TinkerIndex> index : this.indices.entrySet()) {
                            // Write the index name
                            writer.writeUTF(index.getKey());

                            TinkerIndex tinkerIndex = index.getValue();
                            Class indexClass = tinkerIndex.indexClass;

                            // Write the index type
                            writer.writeByte(indexClass.equals(Vertex.class) ? 1 : 2);

                            // Write the number of items associated with this index name
                            writer.writeInt(tinkerIndex.index.size());
                            for (Object o : tinkerIndex.index.entrySet()) {
                                Map.Entry tinkerIndexItem = (Map.Entry) o;

                                // Write the item key
                                writer.writeUTF((String) tinkerIndexItem.getKey());

                                Map tinkerIndexItemSet = (Map) tinkerIndexItem.getValue();

                                // Write the number of sub-items associated with this item
                                writer.writeInt(tinkerIndexItemSet.size());
                                for (Object p : tinkerIndexItemSet.entrySet()) {
                                    Map.Entry items = (Map.Entry) p;

                                    if (indexClass.equals(Vertex.class)) {
                                        Set<TinkerVertex> vertices = (Set<TinkerVertex>) items.getValue();

                                        // Write the number of vertices in this sub-item
                                        writer.writeInt(vertices.size());
                                        for (TinkerVertex v : vertices) {
                                            // Write the vertex identifier
                                            writer.writeUTF(v.getId());
                                        }
                                    } else if (indexClass.equals(Edge.class)) {
                                        Set<TinkerEdge> edges = (Set<TinkerEdge>) items.getValue();

                                        // Write the number of edges in this sub-item
                                        writer.writeInt(edges.size());
                                        for (TinkerEdge e : edges) {
                                            // Write the edge identifier
                                            writer.writeUTF(e.getId());
                                        }
                                    }
                                }
                            }
                        }

                        // Write the number of vertex key indices
                        writer.writeInt(this.vertexKeyIndex.index.size());
                        for (Map.Entry<String, Map<Object, Set<TinkerVertex>>> index : this.vertexKeyIndex.index.entrySet()) {
                            // Write the key index name
                            writer.writeUTF(index.getKey());

                            // Write the number of items associated with this key index name
                            writer.writeInt(index.getValue().size());
                            for (Map.Entry<Object, Set<TinkerVertex>> item : index.getValue().entrySet()) {
                                // Write the item key
                                writeTypedData(writer, item.getKey());

                                // Write the number of vertices in this item
                                writer.writeInt(item.getValue().size());
                                for (TinkerVertex v : item.getValue()) {
                                    // Write the vertex identifier
                                    writer.writeUTF(v.getId());
                                }
                            }
                        }

                        // Write the number of edge key indices
                        writer.writeInt(this.edgeKeyIndex.index.size());
                        for (Map.Entry<String, Map<Object, Set<TinkerEdge>>> index : this.edgeKeyIndex.index.entrySet()) {
                            // Write the key index name
                            writer.writeUTF(index.getKey());

                            // Write the number of items associated with this key index name
                            writer.writeInt(index.getValue().size());
                            for (Map.Entry<Object, Set<TinkerEdge>> item : index.getValue().entrySet()) {
                                // Write the item key
                                writeTypedData(writer, item.getKey());

                                // Write the number of edges in this item
                                writer.writeInt(item.getValue().size());
                                for (TinkerEdge e : item.getValue()) {
                                    // Write the edge identifier
                                    writer.writeUTF(e.getId());
                                }
                            }
                        }
                    }
                    catch (IOException e) {
                        throw new RuntimeException("Could not write metadata file");
                    }
                    finally {
                        try {
                            if (writer != null) {
                                writer.close();
                            }
                        }
                        catch (IOException e) {
                            throw new RuntimeException("Could not write metadata file");
                        }
                    }
                }
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

    private void writeTypedData(DataOutputStream writer, Object data) throws IOException {
        if (data instanceof String) {
            writer.writeByte(1);
            writer.writeUTF((String) data);
        } else if (data instanceof Integer) {
            writer.writeByte(2);
            writer.writeInt((Integer) data);
        } else if (data instanceof Long) {
            writer.writeByte(3);
            writer.writeLong((Long) data);
        } else if (data instanceof Short) {
            writer.writeByte(4);
            writer.writeShort((Short) data);
        } else if (data instanceof Float) {
            writer.writeByte(5);
            writer.writeFloat((Float) data);
        } else if (data instanceof Double) {
            writer.writeByte(6);
            writer.writeDouble((Double) data);
        } else {
            throw new IOException("unknown data type: use java serialization");
        }
    }

    private Object readTypedData(DataInputStream reader) throws IOException {
        byte type = reader.readByte();

        if (type == 1) {
            return reader.readUTF();
        } else if (type == 2) {
            return reader.readInt();
        } else if (type == 3) {
            return reader.readLong();
        } else if (type == 4) {
            return reader.readShort();
        } else if (type == 5) {
            return reader.readFloat();
        } else if (type == 6) {
            return reader.readDouble();
        } else {
            throw new IOException("unknown data type: use java serialization");
        }
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