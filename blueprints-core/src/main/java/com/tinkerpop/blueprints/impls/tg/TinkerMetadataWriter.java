package com.tinkerpop.blueprints.impls.tg;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

/**
 * Writes TinkerGraph metadata to an OutputStream.
 *
 * @author Victor Su
 */
class TinkerMetadataWriter {
    private final TinkerGraph graph;

    /**
     * @param graph the TinkerGraph to pull the data from
     */
    public TinkerMetadataWriter(final TinkerGraph graph) {
        this.graph = graph;
    }

    /**
     * Write TinkerGraph metadata to a file.
     *
     * @param filename the name of the file to write the TinkerGraph metadata to
     * @throws IOException thrown if there is an error writing the TinkerGraph metadata
     */
    public void save(final String filename) throws IOException {
        final FileOutputStream fos = new FileOutputStream(filename);
        save(fos);
        fos.close();
    }

    /**
     * Write TinkerGraph metadata to an OutputStream.
     *
     * @param outputStream the OutputStream to write the TinkerGraph metadata to
     * @throws IOException thrown if there is an error writing the TinkerGraph metadata
     */
    public void save(final OutputStream outputStream) throws IOException {
        DataOutputStream writer = null;
        try {
            writer = new DataOutputStream(outputStream);
            writer.writeLong(this.graph.currentId);
            writeIndices(writer, this.graph);
            writeVertexKeyIndices(writer, this.graph);
            writeEdgeKeyIndices(writer, this.graph);
        } catch (IOException e) {
            throw new RuntimeException("Could not write metadata file");
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                throw new RuntimeException("Could not write metadata file");
            }
        }
    }

    /**
     * Write TinkerGraph metadata to an OutputStream.
     *
     * @param graph        the TinkerGraph to pull the metadata from
     * @param outputStream the OutputStream to write the TinkerGraph metadata to
     * @throws IOException thrown if there is an error writing the TinkerGraph metadata
     */
    public static void save(final TinkerGraph graph, final OutputStream outputStream) throws IOException {
        TinkerMetadataWriter writer = new TinkerMetadataWriter(graph);
        writer.save(outputStream);
    }

    /**
     * Write TinkerGraph metadata to a file.
     *
     * @param graph    the TinkerGraph to pull the data from
     * @param filename the name of the file to write the TinkerGraph metadata to
     * @throws IOException thrown if there is an error writing the TinkerGraph metadata
     */
    public static void save(final TinkerGraph graph, final String filename) throws IOException {
        TinkerMetadataWriter writer = new TinkerMetadataWriter(graph);
        writer.save(filename);
    }

    private void writeIndices(final DataOutputStream writer, final TinkerGraph graph) throws IOException {
        // Write the number of indices
        writer.writeInt(graph.indices.size());

        for (Map.Entry<String, TinkerIndex> index : graph.indices.entrySet()) {
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
                        Set<Vertex> vertices = (Set<Vertex>) items.getValue();

                        // Write the number of vertices in this sub-item
                        writer.writeInt(vertices.size());
                        for (Vertex v : vertices) {
                            // Write the vertex identifier
                            writeTypedData(writer, v.getId());
                        }
                    } else if (indexClass.equals(Edge.class)) {
                        Set<Edge> edges = (Set<Edge>) items.getValue();

                        // Write the number of edges in this sub-item
                        writer.writeInt(edges.size());
                        for (Edge e : edges) {
                            // Write the edge identifier
                            writeTypedData(writer, e.getId());
                        }
                    }
                }
            }
        }
    }

    private void writeVertexKeyIndices(final DataOutputStream writer, final TinkerGraph graph) throws IOException {
        // Write the number of vertex key indices
        writer.writeInt(graph.vertexKeyIndex.index.size());

        for (Map.Entry<String, Map<Object, Set<TinkerVertex>>> index : graph.vertexKeyIndex.index.entrySet()) {
            // Write the key index name
            writer.writeUTF(index.getKey());

            // Write the number of items associated with this key index name
            writer.writeInt(index.getValue().size());
            for (Map.Entry<Object, Set<TinkerVertex>> item : index.getValue().entrySet()) {
                // Write the item key
                writeTypedData(writer, item.getKey());

                // Write the number of vertices in this item
                writer.writeInt(item.getValue().size());
                for (Vertex v : item.getValue()) {
                    // Write the vertex identifier
                    writeTypedData(writer, v.getId());
                }
            }
        }
    }

    private void writeEdgeKeyIndices(final DataOutputStream writer, final TinkerGraph graph) throws IOException {
        // Write the number of edge key indices
        writer.writeInt(graph.edgeKeyIndex.index.size());

        for (Map.Entry<String, Map<Object, Set<TinkerEdge>>> index : graph.edgeKeyIndex.index.entrySet()) {
            // Write the key index name
            writer.writeUTF(index.getKey());

            // Write the number of items associated with this key index name
            writer.writeInt(index.getValue().size());
            for (Map.Entry<Object, Set<TinkerEdge>> item : index.getValue().entrySet()) {
                // Write the item key
                writeTypedData(writer, item.getKey());

                // Write the number of edges in this item
                writer.writeInt(item.getValue().size());
                for (Edge e : item.getValue()) {
                    // Write the edge identifier
                    writeTypedData(writer, e.getId());
                }
            }
        }
    }

    private void writeTypedData(final DataOutputStream writer, final Object data) throws IOException {
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
}
