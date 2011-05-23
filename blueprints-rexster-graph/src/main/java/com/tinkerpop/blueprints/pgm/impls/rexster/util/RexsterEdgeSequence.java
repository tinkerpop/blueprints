package com.tinkerpop.blueprints.pgm.impls.rexster.util;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.impls.rexster.RexsterEdge;
import com.tinkerpop.blueprints.pgm.impls.rexster.RexsterGraph;
import com.tinkerpop.blueprints.pgm.impls.rexster.RexsterTokens;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class RexsterEdgeSequence extends RexsterElementSequence<Edge> {

    public RexsterEdgeSequence(final String uri, final RexsterGraph graph) {
        super(uri, graph);
    }

    protected void fillBuffer() {
        final int bufferSize = this.graph.getBufferSize();
        final JSONObject object = RestHelper.get(this.uri + this.createSeparator() + RexsterTokens.REXSTER_OFFSET_START + RexsterTokens.EQUALS + this.start + RexsterTokens.AND + RexsterTokens.REXSTER_OFFSET_END + RexsterTokens.EQUALS + this.end);
        for (Object edge : (JSONArray) object.get(RexsterTokens.RESULTS)) {
            this.queue.add(new RexsterEdge((JSONObject) edge, this.graph));
            // this.queue.add(new RexsterEdge(RestHelper.getResultObject(graph.getGraphURI() + RexsterTokens.SLASH_EDGES_SLASH + ((JSONObject) edge).get(RexsterTokens._ID)), this.graph));
        }
        if (this.queue.size() == bufferSize) { // next buffer if full
            this.start = this.start + bufferSize;
            this.end = this.end + bufferSize;
        } else { // last buffer
            this.start = this.end;
        }
    }
}
