package com.tinkerpop.blueprints.pgm.impls.rexster;

import com.tinkerpop.blueprints.pgm.*;
import com.tinkerpop.blueprints.pgm.impls.rexster.util.RestHelper;
import com.tinkerpop.blueprints.pgm.impls.rexster.util.RexsterEdgeSequence;
import com.tinkerpop.blueprints.pgm.impls.rexster.util.RexsterVertexSequence;
import com.tinkerpop.blueprints.pgm.impls.rexster.util.RexsterObjectSequence; //PDW

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.net.URL; //PDW
import java.net.URLEncoder; //PDW

/**
 * A Blueprints implementation of the RESTful API of Rexster (http://rexster.tinkerpop.com)
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class RexsterGraph implements IndexableGraph {

    private final String graphURI;
    private JSONObject rawGraph; //PDW not final since updated via getRawGraph
    private Boolean isGremlin; //PDW not final updated via getIsGremlin

    public RexsterGraph(final String graphURI) {
        this.graphURI = graphURI;
        this.rawGraph = this.setRawGraph(); //PDW
        this.isGremlin = this.setIsGremlin(); //PDW
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

    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        if (null == id)
            return new RexsterEdge(RestHelper.postResultObject(this.graphURI + RexsterTokens.SLASH_EDGES + RexsterTokens.QUESTION + RexsterTokens._OUTV + RexsterTokens.EQUALS + outVertex.getId() + RexsterTokens.AND + RexsterTokens._INV + RexsterTokens.EQUALS + inVertex.getId() + RexsterTokens.AND + RexsterTokens._LABEL + RexsterTokens.EQUALS + label), this);
        else
            return new RexsterEdge(RestHelper.postResultObject(this.graphURI + RexsterTokens.SLASH_EDGES_SLASH + id + RexsterTokens.QUESTION + RexsterTokens._OUTV + RexsterTokens.EQUALS + outVertex.getId() + RexsterTokens.AND + RexsterTokens._INV + RexsterTokens.EQUALS + inVertex.getId() + RexsterTokens.AND + RexsterTokens._LABEL + RexsterTokens.EQUALS + label), this);
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
            String clazz = (String) index.get(RexsterTokens.CLASS);
            if (clazz.toLowerCase().contains(RexsterTokens.VERTEX))
                c = Vertex.class;
            else if (clazz.toLowerCase().contains(RexsterTokens.EDGE))
                c = Edge.class;
            else
                throw new RuntimeException("Can not determine whether " + clazz + " is a vertex or edge class");

            if (index.get(RexsterTokens.TYPE).equals(Index.Type.AUTOMATIC.toString().toLowerCase()))
                indices.add(new RexsterAutomaticIndex(this, (String) index.get(RexsterTokens.NAME), c));
            else
                indices.add(new RexsterIndex(this, (String) index.get(RexsterTokens.NAME), c));

        }
        return indices;
    }

    public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
        JSONObject index = RestHelper.get(this.graphURI + RexsterTokens.SLASH_INDICES_SLASH + indexName);
        if (index.get(RexsterTokens.NAME).equals(indexName)) {
            Class c;
            String clazz = (String) index.get(RexsterTokens.CLASS);
            if (clazz.toLowerCase().contains(RexsterTokens.VERTEX))
                c = Vertex.class;
            else if (clazz.toLowerCase().contains(RexsterTokens.EDGE))
                c = Edge.class;
            else
                throw new RuntimeException("Can not determine whether " + clazz + " is a vertex or edge class");

            if (!c.isAssignableFrom(indexClass))
                throw new RuntimeException("Stored index is " + c + " and is being loaded as a " + indexClass + " index");

            if (index.get(RexsterTokens.TYPE).equals(Index.Type.AUTOMATIC.toString().toLowerCase()))
                return new RexsterAutomaticIndex<T>(this, (String) index.get(RexsterTokens.NAME), c);
            else
                return new RexsterIndex<T>(this, (String) index.get(RexsterTokens.NAME), c);
        }
        throw new RuntimeException("No index with name " + indexName + " exists");
    }

    // public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
    //     JSONArray json = RestHelper.getResultArray(this.graphURI + RexsterTokens.SLASH_INDICES);
    //     for (JSONObject index : (List<JSONObject>) json) {
    //         if (index.get(RexsterTokens.NAME).equals(indexName)) {
    //             Class c;
    //             String clazz = (String) index.get(RexsterTokens.CLASS);
    //             if (clazz.toLowerCase().contains(RexsterTokens.VERTEX))
    //                 c = Vertex.class;
    //             else if (clazz.toLowerCase().contains(RexsterTokens.EDGE))
    //                 c = Edge.class;
    //             else
    //                 throw new RuntimeException("Can not determine whether " + clazz + " is a vertex or edge class");
    // 
    // 
    //             if (!c.isAssignableFrom(indexClass))
    //                 throw new RuntimeException("Stored index is " + c + " and is being loaded as a " + indexClass + " index");
    // 
    //             if (index.get(RexsterTokens.TYPE).equals(Index.Type.AUTOMATIC.toString().toLowerCase()))
    //                 return new RexsterAutomaticIndex<T>(this, (String) index.get(RexsterTokens.NAME), c);
    //             else
    //                 return new RexsterIndex<T>(this, (String) index.get(RexsterTokens.NAME), c);
    //         }
    //     }
    //     throw new RuntimeException("No index with name " + indexName + " exists");
    // }

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
            index = RestHelper.postResultObject(this.graphURI + RexsterTokens.SLASH_INDICES_SLASH + indexName + RexsterTokens.QUESTION + RexsterTokens.TYPE_EQUALS + Index.Type.AUTOMATIC.toString().toLowerCase() + RexsterTokens.AND + RexsterTokens.CLASS_EQUALS + c + RexsterTokens.AND + RexsterTokens.KEYS_EQUALS + keys);
        } else {
            index = RestHelper.postResultObject(this.graphURI + RexsterTokens.SLASH_INDICES_SLASH + indexName + RexsterTokens.QUESTION + RexsterTokens.TYPE_EQUALS + Index.Type.AUTOMATIC.toString().toLowerCase() + RexsterTokens.AND + RexsterTokens.CLASS_EQUALS + c);
        }
        if (!index.get(RexsterTokens.NAME).equals(indexName))
            throw new RuntimeException("Could not create index: " + index.get(RexsterTokens.MESSAGE));


        return new RexsterAutomaticIndex<T>(this, indexName, indexClass);

    }

    public <T extends Element> Index<T> createManualIndex(final String indexName, final Class<T> indexClass) {
        String c;
        if (Vertex.class.isAssignableFrom(indexClass))
            c = RexsterTokens.VERTEX;
        else
            c = RexsterTokens.EDGE;

        JSONObject index = RestHelper.postResultObject(this.graphURI + RexsterTokens.SLASH_INDICES_SLASH + indexName + RexsterTokens.QUESTION + RexsterTokens.TYPE_EQUALS + Index.Type.MANUAL.toString().toLowerCase() + RexsterTokens.AND + RexsterTokens.CLASS_EQUALS + c);
        if (!index.get(RexsterTokens.NAME).equals(indexName))
            throw new RuntimeException("Could not create index: " + index.get(RexsterTokens.MESSAGE));

        return new RexsterIndex<T>(this, indexName, indexClass);
    }

    public String toString() {
        // JSONObject object = RestHelper.get(this.graphURI); //PDW this.graphURI instead of graphURI
        // String graphName = (String) object.get(RexsterTokens.GRAPH);
        String graphName = (String) this.rawGraph.get(RexsterTokens.GRAPH);
        return "rexstergraph[" + this.graphURI + "][" + graphName + "]";
    }
	
    public JSONObject getRawGraph() {
        // return (JSONObject) RestHelper.getResultObject(this.graphURI); //bug: use .get() since no 'results' key
        // return this.rawGraph; // cached value
        return this.setRawGraph(); // refreshed value
    }

    //PDW private g.setRawGraph()
    private JSONObject setRawGraph() {
        this.rawGraph = (JSONObject) RestHelper.get(this.graphURI);
        return this.rawGraph;
    }

    //PDW private g.setIsGremlin()
    private Boolean setIsGremlin() {
        Boolean isGremlin = false;
        for (Object result : (JSONArray) this.rawGraph.get(RexsterTokens.EXTENSIONS)) {
            final JSONObject extension = (JSONObject) result;
            if (extension.get(RexsterTokens.HREF).equals(RexsterTokens.GREMLIN_EXTENSION)) {
                isGremlin = true;
                break;
            }
        }
        return isGremlin;
    }
    
    //PDW public g.isGremlin()
    public Boolean isGremlin() {
        return this.isGremlin;
    }
    
    //PDW public g.runGremlin(script)
	public Iterable<Object> runGremlin(final String script) {
	    if (!this.isGremlin())
            throw new RuntimeException("Gremlin Extension is not available on this graph.");
	    try {
            return new RexsterObjectSequence(this.graphURI + RexsterTokens.SLASH + RexsterTokens.GREMLIN_EXTENSION + RexsterTokens.QUESTION + RexsterTokens.SCRIPT_EQUALS + URLEncoder.encode(script), this);
	    } catch (Exception e) {
            throw new RuntimeException("Could not run Gremlin script: " + script, e);
	    }
	}
	
}
