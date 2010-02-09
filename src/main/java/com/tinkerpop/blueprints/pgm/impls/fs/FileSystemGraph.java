package com.tinkerpop.blueprints.pgm.impls.fs;


import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.Vertex;

import java.io.File;
import java.util.Arrays;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class FileSystemGraph implements Graph {

    public Vertex getVertex(Object id) {
        File file = new File(id.toString());
        return new FileSystemVertex(file);
    }

    public Vertex addVertex(Object id) {
        throw new UnsupportedOperationException();
    }

    public void removeVertex(Vertex vertex) {
        throw new UnsupportedOperationException();
    }


    public Edge addEdge(Object id, Vertex outVertex, Vertex inVertex, String label) {
        throw new UnsupportedOperationException();
    }

    public void removeEdge(Edge edge) {
        throw new UnsupportedOperationException();
    }

    public Iterable<Vertex> getVertices() {
        throw new UnsupportedOperationException();
    }

    public Iterable<Edge> getEdges() {
        throw new UnsupportedOperationException();
    }

    public Index getIndex() {
        throw new UnsupportedOperationException();
    }

    public void shutdown() {

    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    public String toString() {
        String roots = Arrays.asList(File.listRoots()).toString();
        roots = roots.substring(1, roots.length() - 1);
        return "filegraph[" + roots + "]";
    }
}
