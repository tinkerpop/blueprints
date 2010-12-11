package com.tinkerpop.blueprints.pgm.impls.rexster;

import com.tinkerpop.blueprints.pgm.*;
import com.tinkerpop.blueprints.pgm.impls.rexster.util.RestHelper;
import com.tinkerpop.blueprints.pgm.impls.rexster.util.RexsterEdgeSequence;
import com.tinkerpop.blueprints.pgm.impls.rexster.util.RexsterVertexSequence;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        // todo: this needs to use http://localhost/graph/edges/null (coming soon to rexster)
        if (null == id)
            id = UUID.randomUUID().toString();
        return new RexsterEdge(RestHelper.postResultObjectForm(this.graphURI + RexsterTokens.SLASH_EDGES_SLASH + id, RexsterTokens._OUTV + RexsterTokens.EQUALS + outVertex.getId() + RexsterTokens.AND + RexsterTokens._INV + RexsterTokens.EQUALS + inVertex.getId() + RexsterTokens.AND + RexsterTokens._LABEL + RexsterTokens.EQUALS + label), this);
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
            if (((String) index.get("class")).contains("Vertex"))
                indices.add(new RexsterIndex(this, (String) index.get("name"), Vertex.class));
            else
                indices.add(new RexsterIndex(this, (String) index.get("name"), Edge.class));
        }
        return indices;
    }

    public <T extends Element> Index<T> getIndex(String indexName, Class<T> indexClass) {
        JSONArray json = RestHelper.getResultArray(this.graphURI + RexsterTokens.SLASH_INDICES);
        for (JSONObject index : (List<JSONObject>) json) {
            if (index.get("name").equals(indexName)) {
                if (((String) index.get("class")).contains("Vertex"))
                    return new RexsterIndex(this, (String) index.get("name"), Vertex.class);
                else
                    return new RexsterIndex(this, (String) index.get("name"), Edge.class);
            }
        }
        throw new RuntimeException("No index with name " + indexName + " exists");
    }

    public <T extends Element> Index<T> createIndex(String indexName, Class<T> indexClass, Index.Type type) {
        throw new UnsupportedOperationException();
    }

    public String toString() {
        return "rexstergraph[" + this.graphURI + "]";
    }
}
