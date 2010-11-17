package com.tinkerpop.blueprints.pgm.impls.rexster;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.impls.rexster.util.RestHelper;
import org.json.simple.JSONObject;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class RexsterEdge extends RexsterElement implements Edge {

    public RexsterEdge(JSONObject rawEdge, RexsterGraph graph) {
        super(rawEdge, graph);
    }

    public Vertex getOutVertex() {
        return new RexsterVertex(RestHelper.getResultObject(this.graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + this.rawElement.get(RexsterTokens._OUTV)), this.graph);
    }

    public Vertex getInVertex() {
        return new RexsterVertex(RestHelper.getResultObject(this.graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + this.rawElement.get(RexsterTokens._INV)), this.graph);

    }

    public String getLabel() {
        return (String) this.rawElement.get(RexsterTokens._LABEL);
    }

    public String toString() {
        return StringFactory.edgeString(this);
    }

    public boolean equals(final Object object) {
        return object instanceof RexsterEdge && ((RexsterEdge) object).getId().equals(this.getId());
    }
}
