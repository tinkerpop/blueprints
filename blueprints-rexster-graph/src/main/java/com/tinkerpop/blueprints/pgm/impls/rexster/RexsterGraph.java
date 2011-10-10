package com.tinkerpop.blueprints.pgm.impls.rexster;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.impls.rexster.util.RestHelper;
import com.tinkerpop.blueprints.pgm.impls.rexster.util.RexsterEdgeSequence;
import com.tinkerpop.blueprints.pgm.impls.rexster.util.RexsterVertexSequence;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A Blueprints implementation of the RESTful API of Rexster (http://rexster.tinkerpop.com)
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class RexsterGraph implements IndexableGraph {

    private final String graphURI;
    private int bufferSize;

    public RexsterGraph(final String graphURI) {
        this(graphURI, 100);
    }

    public RexsterGraph(final String graphURI, final int bufferSize) {
        this.graphURI = graphURI;
        this.bufferSize = bufferSize;
    }

    public String getGraphURI() {
        return this.graphURI;
    }

    /**
     * This method does nothing. To shutdown a RexsterGraph, it must be shutdown locally on the Rexster server.
     */
    public void shutdown() {

    }

    /**
     * Get the size of the communication buffer.
     *
     * @return the communication buffer size
     */
    public int getBufferSize() {
        return this.bufferSize;
    }

    /**
     * This represents the communication buffer. The larger the buffer, the more information is marshaled back and forth.
     *
     * @param bufferSize the size of the buffer
     */
    public void setBufferSize(final int bufferSize) {
        this.bufferSize = bufferSize;
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
            return new RexsterVertex(RestHelper.postResultObject(this.graphURI + RexsterTokens.SLASH_VERTICES_SLASH + RestHelper.encode(id)), this);
    }

    public Vertex getVertex(final Object id) {
        if (null == id)
            throw new IllegalArgumentException("Element identifier cannot be null");

        try {
            return new RexsterVertex(RestHelper.getResultObject(this.graphURI + RexsterTokens.SLASH_VERTICES_SLASH + RestHelper.encode(id)), this);
        } catch (Exception e) {
            // todo: need to improve this.  respect http status codes is better.
            return null;
        }
    }

    public Edge getEdge(final Object id) {
        if (null == id)
            throw new IllegalArgumentException("Element identifier cannot be null");

        try {
            return new RexsterEdge(RestHelper.getResultObject(this.graphURI + RexsterTokens.SLASH_EDGES_SLASH + RestHelper.encode(id)), this);
        } catch (Exception e) {
            // todo: need to improve this.  respect http status codes is better.
            return null;
        }
    }

    public Iterable<Edge> getEdges() {
        return new RexsterEdgeSequence(this.graphURI + RexsterTokens.SLASH_EDGES, this);
    }

    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        if (null == id)
            return new RexsterEdge(RestHelper.postResultObject(this.graphURI + RexsterTokens.SLASH_EDGES + RexsterTokens.QUESTION + RexsterTokens._OUTV + RexsterTokens.EQUALS + RestHelper.encode(outVertex.getId()) + RexsterTokens.AND + RexsterTokens._INV + RexsterTokens.EQUALS + RestHelper.encode(inVertex.getId()) + RexsterTokens.AND + RexsterTokens._LABEL + RexsterTokens.EQUALS + RestHelper.encode(label)), this);
        else
            return new RexsterEdge(RestHelper.postResultObject(this.graphURI + RexsterTokens.SLASH_EDGES_SLASH + RestHelper.encode(id) + RexsterTokens.QUESTION + RexsterTokens._OUTV + RexsterTokens.EQUALS + RestHelper.encode(outVertex.getId()) + RexsterTokens.AND + RexsterTokens._INV + RexsterTokens.EQUALS + RestHelper.encode(inVertex.getId()) + RexsterTokens.AND + RexsterTokens._LABEL + RexsterTokens.EQUALS + RestHelper.encode(label)), this);
    }

    public void removeEdge(final Edge edge) {
        RestHelper.delete(this.graphURI + RexsterTokens.SLASH_EDGES_SLASH + RestHelper.encode(edge.getId()));
    }

    public void removeVertex(final Vertex vertex) {
        RestHelper.delete(this.graphURI + RexsterTokens.SLASH_VERTICES_SLASH + RestHelper.encode(vertex.getId()));
    }

    public void dropIndex(final String indexName) {
        RestHelper.delete(this.graphURI + RexsterTokens.SLASH_INDICES_SLASH + RestHelper.encode(indexName));
    }

    public Iterable<Index<? extends Element>> getIndices() {
        List<Index<? extends Element>> indices = new ArrayList<Index<? extends Element>>();
        JSONArray json = RestHelper.getResultArray(this.graphURI + RexsterTokens.SLASH_INDICES);

        for (int ix = 0; ix < json.length(); ix++) {
            JSONObject index = json.optJSONObject(ix);
            Class c;
            String clazz = index.optString(RexsterTokens.CLASS);
            if (clazz.toLowerCase().contains(RexsterTokens.VERTEX))
                c = Vertex.class;
            else if (clazz.toLowerCase().contains(RexsterTokens.EDGE))
                c = Edge.class;
            else
                throw new RuntimeException("Can not determine whether " + clazz + " is a vertex or edge class");

            if (index.opt(RexsterTokens.TYPE).equals(Index.Type.AUTOMATIC.toString().toLowerCase()))
                indices.add(new RexsterAutomaticIndex(this, index.optString(RexsterTokens.NAME), c));
            else
                indices.add(new RexsterIndex(this, index.optString(RexsterTokens.NAME), c));

        }

        return indices;
    }

    public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
        for (Index index : getIndices()) {
            if (index.getIndexName().equals(indexName)) {
                if (!index.getIndexClass().isAssignableFrom(indexClass))
                    throw new RuntimeException("Stored index is " + index.getIndexClass() + " and is being loaded as a " + indexClass + " index");
                return index;
            }
        }
        return null;
    }

    public <T extends Element> AutomaticIndex<T> createAutomaticIndex(final String indexName, final Class<T> indexClass, final Set<String> indexKeys) {
        String c;
        if (Vertex.class.isAssignableFrom(indexClass))
            c = RexsterTokens.VERTEX;
        else
            c = RexsterTokens.EDGE;

        JSONObject index;
        if (null != indexKeys) {
            List<String> keys = new ArrayList<String>();
            keys.addAll(indexKeys);
            index = RestHelper.postResultObject(this.graphURI + RexsterTokens.SLASH_INDICES_SLASH + RestHelper.encode(indexName) + RexsterTokens.QUESTION + RexsterTokens.TYPE_EQUALS + Index.Type.AUTOMATIC.toString().toLowerCase() + RexsterTokens.AND + RexsterTokens.CLASS_EQUALS + c + RexsterTokens.AND + RexsterTokens.KEYS_EQUALS + keys);
        } else {
            index = RestHelper.postResultObject(this.graphURI + RexsterTokens.SLASH_INDICES_SLASH + RestHelper.encode(indexName) + RexsterTokens.QUESTION + RexsterTokens.TYPE_EQUALS + Index.Type.AUTOMATIC.toString().toLowerCase() + RexsterTokens.AND + RexsterTokens.CLASS_EQUALS + c);
        }
        if (!index.opt(RexsterTokens.NAME).equals(indexName))
            throw new RuntimeException("Could not create index: " + index.optString(RexsterTokens.MESSAGE));

        return new RexsterAutomaticIndex<T>(this, indexName, indexClass);

    }

    public <T extends Element> Index<T> createManualIndex(final String indexName, final Class<T> indexClass) {
        String c;
        if (Vertex.class.isAssignableFrom(indexClass))
            c = RexsterTokens.VERTEX;
        else
            c = RexsterTokens.EDGE;

        JSONObject index = RestHelper.postResultObject(this.graphURI + RexsterTokens.SLASH_INDICES_SLASH + RestHelper.encode(indexName) + RexsterTokens.QUESTION + RexsterTokens.TYPE_EQUALS + Index.Type.MANUAL.toString().toLowerCase() + RexsterTokens.AND + RexsterTokens.CLASS_EQUALS + c);
        if (!index.opt(RexsterTokens.NAME).equals(indexName))
            throw new RuntimeException("Could not create index: " + index.optString(RexsterTokens.MESSAGE));

        return new RexsterIndex<T>(this, indexName, indexClass);
    }

    public String toString() {
        final String graphName = RestHelper.get(graphURI).optString(RexsterTokens.GRAPH);
        return StringFactory.graphString(this, this.graphURI + "[" + graphName + "]");
    }

    public JSONObject getRawGraph() {
        JSONObject rawGraph;
        try {
            rawGraph = RestHelper.get(this.graphURI);
        } catch (Exception e) {
            rawGraph = null;
        }
        return rawGraph;
    }

}
