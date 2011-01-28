package com.tinkerpop.blueprints.pgm.oupls.sail;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;

import java.util.Iterator;

/**
 * A matcher to handle the trivial triple pattern in which subject, predicate, object, and context are all unspecified.
 * It returns all statements.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class TrivialMatcher extends Matcher {
    private final Graph graph;

    public TrivialMatcher(final Graph graph) {
        super(false, false, false, false);
        this.graph = graph;
    }

    @Override
    public Iterator<Edge> match(final String subject, final String predicate, final String object, final String context) {
        return graph.getEdges().iterator();
    }
}
