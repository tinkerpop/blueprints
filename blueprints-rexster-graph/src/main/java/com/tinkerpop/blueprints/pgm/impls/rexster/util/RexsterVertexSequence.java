package com.tinkerpop.blueprints.pgm.impls.rexster.util;

import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.rexster.RexsterGraph;
import com.tinkerpop.blueprints.pgm.impls.rexster.RexsterTokens;
import com.tinkerpop.blueprints.pgm.impls.rexster.RexsterVertex;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class RexsterVertexSequence extends RexsterElementSequence<Vertex> {

    public RexsterVertexSequence(final String uri, final RexsterGraph graph) {
        super(uri, graph);
    }

    protected void fillBuffer() {
        final JSONObject object = RestHelper.get(this.uri + this.createSeparator() + RexsterTokens.REXSTER_OFFSET_START + RexsterTokens.EQUALS + this.start + RexsterTokens.AND + RexsterTokens.REXSTER_OFFSET_END + RexsterTokens.EQUALS + this.end);
        for (final Object vertex : (JSONArray) object.get(RexsterTokens.RESULTS)) {
            this.queue.add(new RexsterVertex(RestHelper.getResultObject(graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + ((JSONObject) vertex).get(RexsterTokens._ID)), this.graph));
        }
        this.start = this.start + this.bufferSize;
        this.end = this.end + this.bufferSize;
    }
}
