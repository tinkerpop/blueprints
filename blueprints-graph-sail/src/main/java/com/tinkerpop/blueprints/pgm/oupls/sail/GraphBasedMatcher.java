package com.tinkerpop.blueprints.pgm.oupls.sail;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;

import java.util.Iterator;

/**
 * A matcher which uses the vertex-edge structure of the graph to retrieve statements.  It does not require an edge index of any kind,
 * but it can only be applied to triple patterns in which the subject or object is specified
 * (which includes all patterns apart from "p", "c", and "pc").
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class GraphBasedMatcher extends Matcher {
    private final GraphSail.DataStore store;

    /**
     * Create a new graph-based matcher with the given triple pattern.
     *
     * @param s     whether the subject is specified
     * @param p     whether the predicate is specified
     * @param o     whether the object is specified
     * @param c     whether the context is specified
     * @param store the Blueprints data store
     */
    public GraphBasedMatcher(final boolean s, final boolean p, final boolean o, final boolean c, final GraphSail.DataStore store) {
        super(s, p, o, c);
        this.store = store;
    }

    public Iterator<Edge> match(final String subject, final String predicate, final String object, final String context) {
        //System.out.println("+ spoc: " + s + " " + p + " " + o + " " + c);
        //System.out.println("+ \ts: " + subject + ", p: " + predicate + ", o: " + object + ", c: " + context);

        if (s && o) {
            Vertex vs = store.getVertex(subject);
            Vertex vo = store.getVertex(object);

            if (null == vs || null == vo) {
                return new EmptyIterator<Edge>();
            } else {
                // TODO: use a simple heuristic (e.g. based on the value type of the vertices) to choose either subject or object.
                // Right now, we arbitrarily choose the subject as the starting point.
                return new FilteredIterator<Edge>(vs.getOutEdges().iterator(), new FilteredIterator.Criterion<Edge>() {
                    public boolean fulfilledBy(final Edge edge) {
                        return store.getValueOf(edge.getInVertex()).equals(object) && (!p || edge.getLabel().equals(predicate)) && (!c || edge.getProperty(GraphSail.CONTEXT_PROP).equals(context));
                    }
                });
            }
        } else if (s) {
            Vertex vs = store.getVertex(subject);
            return null == vs ? new EmptyIterator<Edge>() : new FilteredIterator<Edge>(vs.getOutEdges().iterator(), new FilteredIterator.Criterion<Edge>() {
                public boolean fulfilledBy(final Edge edge) {
                    return (!p || edge.getLabel().equals(predicate)) && (!c || edge.getProperty(GraphSail.CONTEXT_PROP).equals(context));
                }
            });
        } else {
            Vertex vo = store.getVertex(object);
            return null == vo ? new EmptyIterator<Edge>() : new FilteredIterator<Edge>(vo.getInEdges().iterator(), new FilteredIterator.Criterion<Edge>() {
                public boolean fulfilledBy(final Edge edge) {
                    return (!p || edge.getLabel().equals(predicate)) && (!c || edge.getProperty(GraphSail.CONTEXT_PROP).equals(context));
                }
            });
        }
    }

    private class EmptyIterator<T> implements Iterator<T> {
        public boolean hasNext() {
            return false;
        }

        public T next() {
            return null;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
