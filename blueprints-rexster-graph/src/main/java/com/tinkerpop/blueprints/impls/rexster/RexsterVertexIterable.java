package com.tinkerpop.blueprints.impls.rexster;

import com.tinkerpop.blueprints.Vertex;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import java.util.Queue;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
class RexsterVertexIterable extends RexsterElementIterable<Vertex> {

    public RexsterVertexIterable(final String uri, final RexsterGraph graph) {
        super(uri, graph);
    }

    protected void fillBuffer(final Queue<Vertex> queue, final int start, final int end) {
        final JSONObject object = RestHelper.get(this.uri + this.createSeparator() + RexsterTokens.REXSTER_OFFSET_START + RexsterTokens.EQUALS + start + RexsterTokens.AND + RexsterTokens.REXSTER_OFFSET_END + RexsterTokens.EQUALS + end);

        JSONArray array = object.optJSONArray(RexsterTokens.RESULTS);
        for (int ix = 0; ix < array.length(); ix++) {
            queue.add(new RexsterVertex(array.optJSONObject(ix), this.graph));
        }
    }
}
