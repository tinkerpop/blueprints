package com.tinkerpop.blueprints.impls.rexster;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import com.tinkerpop.blueprints.util.StringFactory;
import org.codehaus.jettison.json.JSONObject;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class RexsterEdge extends RexsterElement implements Edge {

    private final String label;
    private final Object outVertex;
    private final Object inVertex;


    protected RexsterEdge(final JSONObject rawEdge, final RexsterGraph graph) {
        super(rawEdge, graph);
        this.label = rawEdge.optString(RexsterTokens._LABEL);
        this.outVertex = rawEdge.opt(RexsterTokens._OUTV);
        this.inVertex = rawEdge.opt(RexsterTokens._INV);
    }

    public Vertex getVertex(final Direction direction) {
        if (direction.equals(Direction.OUT))
            return new RexsterVertex(RestHelper.getResultObject(this.graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + RestHelper.encode(this.outVertex)), this.graph);
        else if (direction.equals(Direction.IN))
            return new RexsterVertex(RestHelper.getResultObject(this.graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + RestHelper.encode(this.inVertex)), this.graph);
        else
            throw ExceptionFactory.bothIsNotSupported();
    }


    public String getLabel() {
        return this.label;
    }

    public String toString() {
        return StringFactory.E + StringFactory.L_BRACKET + this.getId() + StringFactory.R_BRACKET + StringFactory.L_BRACKET + this.outVertex + StringFactory.DASH + this.getLabel() + StringFactory.ARROW + this.inVertex + StringFactory.R_BRACKET;
    }

    public JSONObject getRawEdge() {
        return RestHelper.getResultObject(graph.getGraphURI() + RexsterTokens.SLASH_EDGES_SLASH + this.getId());
    }

}
