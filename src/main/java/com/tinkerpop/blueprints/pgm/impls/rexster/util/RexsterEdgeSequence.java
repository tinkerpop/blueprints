package com.tinkerpop.blueprints.pgm.impls.rexster.util;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.impls.rexster.RexsterEdge;
import com.tinkerpop.blueprints.pgm.impls.rexster.RexsterGraph;
import com.tinkerpop.blueprints.pgm.impls.rexster.RexsterTokens;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class RexsterEdgeSequence implements Iterable<Edge>, Iterator<Edge> {

    private int start = 0;
    private int end = 25;
    private final int bufferSize = 25;
    private final Queue<Edge> queue = new LinkedList<Edge>();
    private final RexsterGraph graph;
    private final String uri;

    public RexsterEdgeSequence(final String uri, final RexsterGraph graph) {
        this.graph = graph;
        this.uri = uri;
        this.fillBuffer();
    }

    public boolean hasNext() {
        if (!queue.isEmpty())
            return true;
        else {
            fillBuffer();
            return !queue.isEmpty();
        }
    }

    public Edge next() {
        if (!queue.isEmpty())
            return queue.remove();
        else {
            fillBuffer();
            if (!queue.isEmpty())
                return queue.remove();
            else
                throw new NoSuchElementException();
        }
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public Iterator<Edge> iterator() {
        return this;
    }

    private void fillBuffer() {
        JSONObject object = RestHelper.get(this.uri + RexsterTokens.QUESTION + RexsterTokens.REXSTER_OFFSET_START + RexsterTokens.EQUALS + this.start + RexsterTokens.AND + RexsterTokens.REXSTER_OFFSET_END + RexsterTokens.EQUALS + this.end);
        for (Object edge : (JSONArray) object.get(RexsterTokens.RESULTS)) {
            JSONObject rawEdge = (JSONObject) edge;
            queue.add(new RexsterEdge(RestHelper.getResultObject(graph.getGraphURI() + RexsterTokens.SLASH_EDGES_SLASH + rawEdge.get(RexsterTokens._ID)), this.graph));
        }
        this.start = this.start + this.bufferSize;
        this.end = this.end + this.bufferSize;
    }
}
