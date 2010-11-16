package com.tinkerpop.blueprints.pgm.impls.rexster;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.impls.rexster.util.RestHelper;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class RexsterVertex extends RexsterElement implements Vertex {

    public RexsterVertex(JSONObject rawVertex, RexsterGraph graph) {
        super(rawVertex, graph);
    }

    public Iterable<Edge> getInEdges() {
        List<Edge> edges = new ArrayList<Edge>();
        for (Object edge : RestHelper.parseResultArray(graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + this.getId() + RexsterTokens.SLASH_INE)) {
            JSONObject rawEdge = (JSONObject) edge;
            edges.add(new RexsterEdge(rawEdge, this.graph));
        }
        return edges;
    }

    public Iterable<Edge> getOutEdges() {
        List<Edge> edges = new ArrayList<Edge>();
        for (Object edge : RestHelper.parseResultArray(graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + this.getId() + RexsterTokens.SLASH_OUTE)) {
            JSONObject rawEdge = (JSONObject) edge;
            edges.add(new RexsterEdge(rawEdge, this.graph));
        }
        return edges;
    }

    public String toString() {
        return StringFactory.vertexString(this);
    }
}
