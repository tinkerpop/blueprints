package com.tinkerpop.blueprints.pgm.impls.rexster;

import com.tinkerpop.blueprints.pgm.*;
import com.tinkerpop.blueprints.pgm.impls.rexster.util.RestHelper;
import com.tinkerpop.blueprints.pgm.impls.rexster.util.RexsterEdgeSequence;
import com.tinkerpop.blueprints.pgm.impls.rexster.util.RexsterVertexSequence;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class RexsterGraph implements IndexableGraph {

    private final String graphURI;

    public RexsterGraph(final String graphURI) {
        this.graphURI = graphURI;
        // test to make sure its a valid, accessible url
        RestHelper.get(graphURI);
    }

    public String getGraphURI() {
        return this.graphURI;
    }

    public void shutdown() {

    }

    public void clear() {
        RestHelper.delete(this.graphURI);
    }

    public Iterable<Vertex> getVertices() {
        return new RexsterVertexSequence(this.graphURI + RexsterTokens.SLASH_VERTICES, this);
    }

    public Vertex addVertex(final Object id) {
        if (null == id)
            return new RexsterVertex(RestHelper.postResultObject(this.graphURI + RexsterTokens.SLASH_VERTICES), this);
        else
            return new RexsterVertex(RestHelper.postResultObject(this.graphURI + RexsterTokens.SLASH_VERTICES_SLASH + id), this);
    }

    public Vertex getVertex(final Object id) {
        return new RexsterVertex(RestHelper.getResultObject(this.graphURI + RexsterTokens.SLASH_VERTICES_SLASH + id), this);
    }

    public Edge getEdge(final Object id) {
        return new RexsterEdge(RestHelper.getResultObject(this.graphURI + RexsterTokens.SLASH_EDGES_SLASH + id), this);
    }

    public Iterable<Edge> getEdges() {
        return new RexsterEdgeSequence(this.graphURI + RexsterTokens.SLASH_EDGES, this);
    }

    public Edge addEdge(Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        return new RexsterEdge(RestHelper.postResultObject(this.graphURI + RexsterTokens.SLASH_EDGES + RexsterTokens.QUESTION + RexsterTokens._OUTV + RexsterTokens.EQUALS + outVertex.getId() + RexsterTokens.AND + RexsterTokens._INV + RexsterTokens.EQUALS + inVertex.getId() + RexsterTokens.AND + RexsterTokens._LABEL + RexsterTokens.EQUALS + label), this);
    }

    public void removeEdge(final Edge edge) {
        RestHelper.delete(this.graphURI + RexsterTokens.SLASH_EDGES_SLASH + edge.getId());
    }

    public void removeVertex(final Vertex vertex) {
        RestHelper.delete(this.graphURI + RexsterTokens.SLASH_VERTICES_SLASH + vertex.getId());
    }

    public void dropIndex(final String indexName) {
        RestHelper.delete(this.graphURI + RexsterTokens.SLASH_INDICES_SLASH + indexName);
    }

    public Iterable<Index<? extends Element>> getIndices() {
        List<Index<? extends Element>> indices = new ArrayList<Index<? extends Element>>();
        JSONArray json = RestHelper.getResultArray(this.graphURI + RexsterTokens.SLASH_INDICES);
        for (JSONObject index : (List<JSONObject>) json) {
            Class c;
            if (((String) index.get(RexsterTokens.CLASS)).contains("Vertex"))
                c = Vertex.class;
            else
                c = Edge.class;
            if (index.get(RexsterTokens.TYPE).equals(Index.Type.AUTOMATIC.toString().toLowerCase()))
                indices.add(new RexsterAutomaticIndex(this, (String) index.get(RexsterTokens.NAME), c));
            else
                indices.add(new RexsterIndex(this, (String) index.get(RexsterTokens.NAME), c));

        }
        return indices;
    }

    public <T extends Element> Index<T> getIndex(String indexName, Class<T> indexClass) {
        JSONArray json = RestHelper.getResultArray(this.graphURI + RexsterTokens.SLASH_INDICES);
        for (JSONObject index : (List<JSONObject>) json) {
            if (index.get(RexsterTokens.NAME).equals(indexName)) {
                Class c;
                if (((String) index.get(RexsterTokens.CLASS)).contains("Vertex"))
                    c = Vertex.class;
                else
                    c = Edge.class;

                if (!c.isAssignableFrom(indexClass))
                    throw new RuntimeException("Stored index is " + c + " and is being loaded as a " + indexClass + " index");

                if (index.get(RexsterTokens.TYPE).equals(Index.Type.AUTOMATIC.toString().toLowerCase()))
                    return new RexsterAutomaticIndex(this, (String) index.get(RexsterTokens.NAME), c);
                else
                    return new RexsterIndex(this, (String) index.get(RexsterTokens.NAME), c);
            }
        }
        throw new RuntimeException("No index with name " + indexName + " exists");
    }

    public <T extends Element> Index<T> createIndex(String indexName, Class<T> indexClass, Index.Type type) {
        String c;
        if (Vertex.class.isAssignableFrom(indexClass))
            c = RexsterTokens.VERTEX;
        else
            c = RexsterTokens.EDGE;

        JSONObject index = RestHelper.postResultObject(this.graphURI + RexsterTokens.SLASH_INDICES_SLASH + indexName + RexsterTokens.QUESTION + RexsterTokens.TYPE_EQUALS + type.toString().toLowerCase() + RexsterTokens.AND + RexsterTokens.CLASS_EQUALS + c);
        if (type.equals(Index.Type.AUTOMATIC))
            return new RexsterAutomaticIndex(this, indexName, indexClass);
        else
            return new RexsterIndex(this, indexName, indexClass);
    }

    public String toString() {
        return "rexstergraph[" + this.graphURI + "]";
    }
}
