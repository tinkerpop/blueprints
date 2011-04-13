package com.tinkerpop.blueprints.pgm.impls.git;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * User: josh
 * Date: 4/13/11
 * Time: 1:39 PM
 */
public class GitGraph implements IndexableGraph {
    private final File directory;
    private final IndexableGraph base;
    private final GitGraphHelper helper;

    public GitGraph(final File directory) throws IOException {
        this(directory, new TinkerGraph());
    }

    /**
     * @param directory the directory for GitGraph storage.
     *                  If this directory already exists, GitGraph will attempt to load previously saved data.
     *                  Data will be saved to this directory when this Graph is shutdown.
     * @param base      a graph for temporary storage.  Note: this graph will be cleared of any pre-existing data.
     * @throws IOException
     */
    private GitGraph(final File directory,
                     final IndexableGraph base) throws IOException {
        this.directory = directory;
        this.base = base;

        this.helper = new GitGraphHelper();
        helper.load(directory, base);
    }

    @Override
    public <T extends Element> Index<T> createManualIndex(final String indexName,
                                                          final Class<T> indexClass) {
        return base.createManualIndex(indexName, indexClass);
    }

    @Override
    public <T extends Element> AutomaticIndex<T> createAutomaticIndex(final String indexName,
                                                                      final Class<T> indexClass,
                                                                      final Set<String> indexKeys) {
        return base.createAutomaticIndex(indexName, indexClass, indexKeys);
    }

    @Override
    public <T extends Element> Index<T> getIndex(final String indexName,
                                                 final Class<T> indexClass) {
        return base.getIndex(indexName, indexClass);
    }

    @Override
    public Iterable<Index<? extends Element>> getIndices() {
        return base.getIndices();
    }

    @Override
    public void dropIndex(final String indexName) {
        base.dropIndex(indexName);
    }

    @Override
    public Vertex addVertex(final Object id) {
        validateElementId(id);
        return base.addVertex(id);
    }

    @Override
    public Vertex getVertex(final Object id) {
        return base.getVertex(id);
    }

    @Override
    public void removeVertex(final Vertex vertex) {
        base.removeVertex(vertex);
    }

    @Override
    public Iterable<Vertex> getVertices() {
        return base.getVertices();
    }

    @Override
    public Edge addEdge(final Object id,
                        final Vertex outVertex,
                        final Vertex inVertex,
                        final String label) {
        validateElementId(id);
        validateEdgeVertexId((String) id, (String) outVertex.getId());
        validateEdgeVertexId((String) id, (String) inVertex.getId());
        return base.addEdge(id, outVertex, inVertex, label);
    }

    @Override
    public Edge getEdge(final Object id) {
        return base.getEdge(id);
    }

    @Override
    public void removeEdge(final Edge edge) {
        base.removeEdge(edge);
    }

    @Override
    public Iterable<Edge> getEdges() {
        return base.getEdges();
    }

    @Override
    public void clear() {
        base.clear();
    }

    @Override
    public void shutdown() {
        try {
            save();
        } catch (IOException e) {
            // TODO: let shutdown() throw an exception?
            e.printStackTrace(System.err);
        }
        base.shutdown();
    }

    private void save() throws IOException {
        helper.save(base, directory);
    }

    private void validateElementId(final Object id) {
        if (!(id instanceof String)) {
            throw new IllegalArgumentException("element id is not a String: " + id);
        }
    }

    private void validateEdgeVertexId(final String edgeId,
                                      final String vertexId) {
        int i = edgeId.lastIndexOf("/");
        if (i >= 0) {
            if (!vertexId.startsWith(edgeId.substring(0, i + 1))) {
                throw new IllegalArgumentException("edge '" + edgeId + "' cannot reference vertex '" + vertexId + "'");
            }
        }
    }

    public static void main(final String[] args) throws Exception {

        /*
        ByteArrayOutputStream helper = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(helper);
        out.writeObject(2);
        out.writeObject(2);
        out.flush();
        out.close();
        for (byte b : helper.toByteArray()) {
            System.out.println("byte: " + b);
        }
        System.exit(0);
        //*/
    }
}
