package com.tinkerpop.blueprints.oupls.sail;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

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
    public Iterable<Edge> match(final Resource subject,
                                final URI predicate,
                                final Value object,
                                final Resource context,
                                final boolean includeInferred) {
        Iterable<Edge> i = graph.getEdges();

        if (!includeInferred) {
            i = new FilteredIterator<Edge>(i, new NoInferenceCriterion());
        }

        return new IteratorCloseableIterable<Edge>(i.iterator());
    }
}
