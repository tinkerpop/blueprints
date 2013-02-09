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
 * TinkerMetadataWriter writes TinkerGraph metadata to an OutputStream.
 *
 * @author Victor Su
 */
public class TinkerMetadataWriter {
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
    public void outputGraph(final String filename) throws IOException {
        FileOutputStream fos = new FileOutputStream(filename);
        outputGraph(fos);
        fos.close();
    }

    /**
     * Write TinkerGraph metadata to an OutputStream.
     *
     * @param outputStream the OutputStream to write the TinkerGraph metadata to
     * @throws IOException thrown if there is an error writing the TinkerGraph metadata
     */
    public void outputGraph(final OutputStream outputStream) throws IOException {
        DataOutputStream writer = null;
        try {
            writer = new DataOutputStream(outputStream);

            // Write the current ID
            writer.writeLong(this.graph.currentId);

            // Write the number of indices
            writer.writeInt(this.graph.indices.size());
            for (Map.Entry<String, TinkerIndex> index : this.graph.indices.entrySet()) {
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
            writer.writeInt(this.graph.vertexKeyIndex.index.size());
            for (Map.Entry<String, Map<Object, Set<TinkerVertex>>> index : this.graph.vertexKeyIndex.index.entrySet()) {
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
            writer.writeInt(this.graph.edgeKeyIndex.index.size());
            for (Map.Entry<String, Map<Object, Set<TinkerEdge>>> index : this.graph.edgeKeyIndex.index.entrySet()) {
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

    /**
     * Write TinkerGraph metadata to an OutputStream.
     *
     * @param graph               the TinkerGraph to pull the metadata from
     * @param outputStream        the OutputStream to write the TinkerGraph metadata to
     * @throws IOException thrown if there is an error writing the TinkerGraph metadata
     */
    public static void outputGraph(final TinkerGraph graph, final OutputStream outputStream) throws IOException {
        TinkerMetadataWriter writer = new TinkerMetadataWriter(graph);
        writer.outputGraph(outputStream);
    }

    /**
     * Write TinkerGraph metadata to a file.
     *
     * @param graph               the TinkerGraph to pull the data from
     * @param filename            the name of the file to write the TinkerGraph metadata to
     * @throws IOException thrown if there is an error writing the TinkerGraph metadata
     */
    public static void outputGraph(final TinkerGraph graph, final String filename) throws IOException {
        TinkerMetadataWriter writer = new TinkerMetadataWriter(graph);
        writer.outputGraph(filename);
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
}
