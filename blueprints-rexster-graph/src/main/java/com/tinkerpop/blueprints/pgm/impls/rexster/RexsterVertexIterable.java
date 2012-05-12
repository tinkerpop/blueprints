package com.tinkerpop.blueprints.pgm.impls.rexster;

import com.tinkerpop.blueprints.pgm.Vertex;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class RexsterVertexIterable extends RexsterElementIterable<Vertex> {

    public RexsterVertexIterable(final String uri, final RexsterGraph graph) {
        super(uri, graph);
    }

    protected void fillBuffer() {
        final int bufferSize = this.graph.getBufferSize();
        final JSONObject object = RestHelper.get(this.uri + this.createSeparator() + RexsterTokens.REXSTER_OFFSET_START + RexsterTokens.EQUALS + this.start + RexsterTokens.AND + RexsterTokens.REXSTER_OFFSET_END + RexsterTokens.EQUALS + this.end);

        JSONArray array = object.optJSONArray(RexsterTokens.RESULTS);
        for (int ix = 0; ix < array.length(); ix++) {
            this.queue.add(new RexsterVertex(array.optJSONObject(ix), this.graph));
        }

        if (this.queue.size() == bufferSize) { // next buffer if full
            this.start = this.start + bufferSize;
            this.end = this.end + bufferSize;
        } else { // last buffer
            this.start = this.end;
        }
    }
}
