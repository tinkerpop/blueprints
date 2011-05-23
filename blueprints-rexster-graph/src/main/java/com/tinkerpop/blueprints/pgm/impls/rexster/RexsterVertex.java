package com.tinkerpop.blueprints.pgm.impls.rexster;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.impls.rexster.util.RestHelper;
import com.tinkerpop.blueprints.pgm.impls.rexster.util.RexsterEdgeSequence;
import org.json.simple.JSONObject;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class RexsterVertex extends RexsterElement implements Vertex {

    public RexsterVertex(final JSONObject rawVertex, final RexsterGraph graph) {
        super(rawVertex, graph);
    }

    public Iterable<Edge> getInEdges() {
        return new RexsterEdgeSequence(this.graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + RestHelper.encode(this.getId()) + RexsterTokens.SLASH_INE, this.graph);
    }

    public Iterable<Edge> getOutEdges() {
        return new RexsterEdgeSequence(this.graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + RestHelper.encode(this.getId()) + RexsterTokens.SLASH_OUTE, this.graph);
    }

    public Iterable<Edge> getInEdges(final String label) {
        return new RexsterEdgeSequence(this.graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + RestHelper.encode(this.getId()) + RexsterTokens.SLASH_INE + RexsterTokens.QUESTION + RexsterTokens._LABEL_EQUALS + RestHelper.encode(label), this.graph);
    }

    public Iterable<Edge> getOutEdges(final String label) {
        return new RexsterEdgeSequence(this.graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + RestHelper.encode(this.getId()) + RexsterTokens.SLASH_OUTE + RexsterTokens.QUESTION + RexsterTokens._LABEL_EQUALS + RestHelper.encode(label), this.graph);
    }

    public String toString() {
        return StringFactory.vertexString(this);
    }

    public JSONObject getRawVertex() {
        return this.getRawElement();
        // return RestHelper.getResultObject(graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + this.getId());
    }

}
