package com.tinkerpop.blueprints.impls.rexster;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.MetaGraph;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.DefaultGraphQuery;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import com.tinkerpop.blueprints.util.StringFactory;
import org.apache.commons.configuration.Configuration;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A Blueprints implementation of the RESTful API of Rexster (http://rexster.tinkerpop.com).
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class RexsterGraph implements MetaGraph<JSONObject> {

    public static final int DEFAULT_BUFFER_SIZE = 100;
    private final String graphURI;
    private int bufferSize;

    private static final Features FEATURES = new Features();

    static {
        // intended to be used with TinkerGraph as the endpoint graph
        FEATURES.supportsDuplicateEdges = true;
        FEATURES.supportsSelfLoops = true;
        FEATURES.ignoresSuppliedIds = false;
        FEATURES.isPersistent = false;
        FEATURES.supportsVertexIteration = true;
        FEATURES.supportsEdgeIteration = true;
        FEATURES.supportsVertexIndex = true;
        FEATURES.supportsEdgeIndex = true;
        FEATURES.isWrapper = true;
        FEATURES.supportsKeyIndices = true;
        FEATURES.supportsVertexKeyIndex = true;
        FEATURES.supportsEdgeKeyIndex = true;
        FEATURES.supportsIndices = true;
        FEATURES.supportsEdgeRetrieval = true;
        FEATURES.supportsVertexProperties = true;
        FEATURES.supportsEdgeProperties = true;


        // RexsterGraph will toString anything it can't convert to a standard Rexster type.
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
        FEATURES.supportsThreadedTransactions = false;
        FEATURES.supportsTransactions = false;
    }

    /**
     * Construct a RexsterGraph with no authentication and default buffer size.
     */
    public RexsterGraph(final String graphURI) {
        this(graphURI, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Construct a RexsterGraph with authentication enabled (assuming username and password are
     * both non-null values) and default buffer size.
     */
    public RexsterGraph(final String graphURI, final String username, final String password) {
        this(graphURI, DEFAULT_BUFFER_SIZE, username, password);
    }

    /**
     * Construct a RexsterGraph with no authentication.
     */
    public RexsterGraph(final String graphURI, final int bufferSize) {
        this(graphURI, bufferSize, null, null);
    }

    public RexsterGraph(final Configuration configuration) {
        this(configuration.getString("blueprints.rexster.url", null),
                configuration.getInt("blueprints.rexster.buffer-size", DEFAULT_BUFFER_SIZE),
                configuration.getString("blueprints.rexster.username", null),
                configuration.getString("blueprints.rexster.password", null));
    }

    /**
     * Construct a RexsterGraph with authentication enabled (assuming username and password are
     * both non-null values).
     */
    public RexsterGraph(final String graphURI, final int bufferSize, final String username, final String password) {
        this.graphURI = graphURI;
        this.bufferSize = bufferSize;
        RestHelper.Authentication = new RexsterAuthentication(username, password);
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

    public Iterable<Vertex> getVertices() {
        return new RexsterVertexIterable(this.graphURI + RexsterTokens.SLASH_VERTICES, this);
    }

    public Iterable<Vertex> getVertices(final String key, final Object value) {
        return new RexsterVertexIterable(this.graphURI + RexsterTokens.SLASH_VERTICES + RexsterTokens.QUESTION + RexsterTokens.KEY_EQUALS + key + RexsterTokens.AND + RexsterTokens.VALUE_EQUALS + RestHelper.uriCast(value), this);
    }

    public Vertex addVertex(final Object id) {
        if (null == id)
            return new RexsterVertex(RestHelper.postResultObject(this.graphURI + RexsterTokens.SLASH_VERTICES), this);
        else
            return new RexsterVertex(RestHelper.postResultObject(this.graphURI + RexsterTokens.SLASH_VERTICES_SLASH + RestHelper.encode(id)), this);
    }

    public Vertex getVertex(final Object id) {
        if (null == id)
            throw ExceptionFactory.vertexIdCanNotBeNull();

        try {
            return new RexsterVertex(RestHelper.getResultObject(this.graphURI + RexsterTokens.SLASH_VERTICES_SLASH + RestHelper.encode(id)), this);
        } catch (Exception e) {
            return null;
        }
    }

    public Edge getEdge(final Object id) {
        if (null == id)
            throw ExceptionFactory.edgeIdCanNotBeNull();

        try {
            return new RexsterEdge(RestHelper.getResultObject(this.graphURI + RexsterTokens.SLASH_EDGES_SLASH + RestHelper.encode(id)), this);
        } catch (Exception e) {
            return null;
        }
    }

    public Iterable<Edge> getEdges() {
        return new RexsterEdgeIterable(this.graphURI + RexsterTokens.SLASH_EDGES, this);
    }

    public Iterable<Edge> getEdges(final String key, final Object value) {
        return new RexsterEdgeIterable(this.graphURI + RexsterTokens.SLASH_EDGES + RexsterTokens.QUESTION + RexsterTokens.KEY_EQUALS + key + RexsterTokens.AND + RexsterTokens.VALUE_EQUALS + RestHelper.uriCast(value), this);
    }

    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        if (label == null)
            throw ExceptionFactory.edgeLabelCanNotBeNull();

        final Map<String, Object> data = new HashMap<String, Object>();
        data.put(RexsterTokens._OUTV, outVertex.getId());
        data.put(RexsterTokens._INV, inVertex.getId());
        data.put(RexsterTokens._LABEL, label);
        final JSONObject json = new JSONObject(data);

        if (null == id)
            return new RexsterEdge(RestHelper.postResultObject(this.graphURI + RexsterTokens.SLASH_EDGES, json), this);
        else
            return new RexsterEdge(RestHelper.postResultObject(this.graphURI + RexsterTokens.SLASH_EDGES_SLASH + RestHelper.encode(id), json), this);
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

    public Features getFeatures() {
        return FEATURES;
    }

    public <T extends Element> void dropIndex(String key, Class<T> elementClass) {
        final String c = getKeyIndexClass(elementClass);
        RestHelper.delete(this.graphURI + RexsterTokens.SLASH_KEY_INDICES_SLASH + c + RexsterTokens.SLASH + key);
    }

    public <T extends Element> void createIndex(String key, Class<T> elementClass, final Parameter... indexParameters) {
        final String c = getKeyIndexClass(elementClass);
        RestHelper.post(this.graphURI + RexsterTokens.SLASH_KEY_INDICES_SLASH + c + RexsterTokens.SLASH + key);
    }

    public <T extends Element> Set<String> getIndexedKeys(Class<T> elementClass) {
        final String c = getKeyIndexClass(elementClass);
        final JSONArray jsonArray = RestHelper.getResultArray(this.graphURI + RexsterTokens.SLASH_KEY_INDICES_SLASH + c);

        final HashSet<String> keys = new HashSet<String>();
        for (int ix = 0; ix < jsonArray.length(); ix++) {
            keys.add(jsonArray.optString(ix));
        }

        return keys;
    }

    public JSONArray execute(final String gremlinScript) {
        return execute(gremlinScript, (JSONObject) null);
    }

    public JSONArray execute(final String gremlinScript, final Map<String, Object> scriptParams) {
        JSONObject json = null;
        if (scriptParams != null && scriptParams.size() > 0) {
            json = new JSONObject(scriptParams);
        }

        return execute(gremlinScript, json);
    }

    public JSONArray execute(final String gremlinScript, final String scriptParams) throws JSONException {
        return execute(gremlinScript, new JSONObject(scriptParams));
    }

    public JSONArray execute(final String gremlinScript, final JSONObject scriptParams) {
        final Map<String, Object> scriptArgs = new HashMap<String, Object>();
        scriptArgs.put("script", gremlinScript);

        if (scriptParams != null) {
            scriptArgs.put("params", scriptParams);
        }

        return RestHelper.postResultArray(this.graphURI + RexsterTokens.SLASH_GREMLIN, new JSONObject(scriptArgs));
    }

    private static <T extends Element> String getKeyIndexClass(Class<T> elementClass) {
        String c;
        if (Vertex.class.isAssignableFrom(elementClass))
            c = RexsterTokens.VERTEX;
        else
            c = RexsterTokens.EDGE;
        return c;
    }

    public GraphQuery query() {
        return new DefaultGraphQuery(this);
    }

    public void commit() {
        throw ExceptionFactory.graphDoesNotSupportTransactions();
    }

    public void rollback() {
        throw ExceptionFactory.graphDoesNotSupportTransactions();
    }

    public Graph newTransaction() {
        throw ExceptionFactory.graphDoesNotSupportThreadedTransactions();
    }

}
