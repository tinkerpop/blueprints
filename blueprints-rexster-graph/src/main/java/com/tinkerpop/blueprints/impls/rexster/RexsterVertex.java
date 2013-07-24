package com.tinkerpop.blueprints.impls.rexster;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexQuery;
import com.tinkerpop.blueprints.util.DefaultVertexQuery;
import com.tinkerpop.blueprints.util.MultiIterable;
import com.tinkerpop.blueprints.util.StringFactory;
import com.tinkerpop.blueprints.util.VerticesFromEdgesIterable;
import org.codehaus.jettison.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class RexsterVertex extends RexsterElement implements Vertex {

    protected RexsterVertex(final JSONObject rawVertex, final RexsterGraph graph) {
        super(rawVertex, graph);
    }

    public Iterable<Edge> getEdges(final Direction direction, final String... labels) {
        if (direction.equals(Direction.OUT)) {
            return this.getOutEdges(labels);
        } else if (direction.equals(Direction.IN))
            return this.getInEdges(labels);
        else {
            return new MultiIterable<Edge>(Arrays.asList(this.getInEdges(labels), this.getOutEdges(labels)));
        }
    }

    public Iterable<Vertex> getVertices(final Direction direction, final String... labels) {
        return new VerticesFromEdgesIterable(this, direction, labels);
    }

    private Iterable<Edge> getOutEdges(final String... labels) {
        if (labels.length == 0)
            return new RexsterEdgeIterable(this.graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + RestHelper.encode(this.getId()) + RexsterTokens.SLASH_OUTE, this.graph);

        else if (labels.length == 1) {
            return new RexsterEdgeIterable(this.graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + RestHelper.encode(this.getId()) + RexsterTokens.SLASH_OUTE + RexsterTokens.QUESTION + RexsterTokens._LABEL_EQUALS + RestHelper.encode(labels[0]), this.graph);
        } else {
            final List<Iterable<Edge>> edges = new ArrayList<Iterable<Edge>>();
            for (final Object filter : labels) {
                edges.add(new RexsterEdgeIterable(this.graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + RestHelper.encode(this.getId()) + RexsterTokens.SLASH_OUTE + RexsterTokens.QUESTION + RexsterTokens._LABEL_EQUALS + RestHelper.encode(filter), this.graph));
            }
            return new MultiIterable<Edge>(edges);
        }
    }

    private Iterable<Edge> getInEdges(final String... labels) {
        if (labels.length == 0)
            return new RexsterEdgeIterable(this.graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + RestHelper.encode(this.getId()) + RexsterTokens.SLASH_INE, this.graph);

        else if (labels.length == 1) {
            return new RexsterEdgeIterable(this.graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + RestHelper.encode(this.getId()) + RexsterTokens.SLASH_INE + RexsterTokens.QUESTION + RexsterTokens._LABEL_EQUALS + RestHelper.encode(labels[0]), this.graph);
        } else {

            final List<Iterable<Edge>> edges = new ArrayList<Iterable<Edge>>();
            for (final Object filter : labels) {
                edges.add(new RexsterEdgeIterable(this.graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + RestHelper.encode(this.getId()) + RexsterTokens.SLASH_INE + RexsterTokens.QUESTION + RexsterTokens._LABEL_EQUALS + RestHelper.encode(filter), this.graph));
            }
            return new MultiIterable<Edge>(edges);
        }
    }

    public Edge addEdge(final String label, final Vertex vertex) {
        return this.graph.addEdge(null, this, vertex, label);
    }

    public VertexQuery query() {
        return new DefaultVertexQuery(this);
    }

    public String toString() {
        return StringFactory.vertexString(this);
    }

    public JSONObject getRawVertex() {
        return RestHelper.getResultObject(graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + this.getId());
    }

}
