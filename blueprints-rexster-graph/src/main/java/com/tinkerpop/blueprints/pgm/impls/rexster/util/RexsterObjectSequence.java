package com.tinkerpop.blueprints.pgm.impls.rexster.util;

import com.tinkerpop.blueprints.pgm.impls.rexster.RexsterGraph;
import com.tinkerpop.blueprints.pgm.impls.rexster.RexsterTokens;
import com.tinkerpop.blueprints.pgm.impls.rexster.RexsterVertex;
import com.tinkerpop.blueprints.pgm.impls.rexster.RexsterEdge;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * @author Pierre De Wilde (pierredewilde@gmail.com)
 */
public class RexsterObjectSequence extends RexsterElementSequence<Object> {

    public RexsterObjectSequence(final String uri, final RexsterGraph graph) {
        super(uri, graph);
    }

    protected void fillBuffer() {
        // warning: require Rexster 0.4+
        final JSONObject object = RestHelper.get(this.uri + this.createSeparator() + RexsterTokens.REXSTER_OFFSET_START + RexsterTokens.EQUALS + this.start + RexsterTokens.AND + RexsterTokens.REXSTER_OFFSET_END + RexsterTokens.EQUALS + this.end);
        for (Object result : (JSONArray) object.get(RexsterTokens.RESULTS)) {
            if (result instanceof JSONObject) {
                JSONObject json = (JSONObject) result;
                if (RexsterTokens.VERTEX.equals((String) json.get(RexsterTokens._TYPE)))
                    this.queue.add(new RexsterVertex(json, this.graph));
                else if (RexsterTokens.EDGE.equals((String) json.get(RexsterTokens._TYPE)))
                    this.queue.add(new RexsterEdge(json, this.graph));
                else {
                    System.out.println(json.get(RexsterTokens._TYPE));
                    this.queue.add(json);
                }
            }
            else
                this.queue.add(result);
        }
        if (this.queue.size() == this.bufferSize) { // buffer is full => prepare next buffer
            this.start = this.start + this.bufferSize;
            this.end = this.end + this.bufferSize;
        } else { // last buffer
            this.start = this.end;
        }
    }
}
