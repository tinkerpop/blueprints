package com.tinkerpop.blueprints.pgm.impls.rexster.util;

import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.rexster.RexsterGraph;
import com.tinkerpop.blueprints.pgm.impls.rexster.RexsterTokens;
import com.tinkerpop.blueprints.pgm.impls.rexster.RexsterVertex;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class RexsterVertexSequence implements Iterable<Vertex>, Iterator<Vertex> {

    private int start = 0;
    private int end = 25;
    private final int bufferSize = 25;
    private final Queue<Vertex> queue = new LinkedList<Vertex>();
    private final RexsterGraph graph;
    private final String uri;

    public RexsterVertexSequence(final String uri, final RexsterGraph graph) {
        this.uri = uri;
        this.graph = graph;
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

    public Vertex next() {
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

    public Iterator<Vertex> iterator() {
        return this;
    }

    private void fillBuffer() {
        final JSONObject object = RestHelper.get(this.uri + RexsterTokens.QUESTION + RexsterTokens.REXSTER_OFFSET_START + RexsterTokens.EQUALS + this.start + RexsterTokens.AND + RexsterTokens.REXSTER_OFFSET_END + RexsterTokens.EQUALS + this.end);
        for (final Object vertex : (JSONArray) object.get(RexsterTokens.RESULTS)) {
            queue.add(new RexsterVertex(RestHelper.getResultObject(graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + ((JSONObject) vertex).get(RexsterTokens._ID)), this.graph));
        }
        this.start = this.start + this.bufferSize;
        this.end = this.end + this.bufferSize;
    }
}
