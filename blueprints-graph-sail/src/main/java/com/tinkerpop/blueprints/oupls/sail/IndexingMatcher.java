package com.tinkerpop.blueprints.oupls.sail;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import java.util.LinkedList;
import java.util.List;

/**
 * A matcher which uses Blueprints indexing functionality to both index and retrieve statements.  Indexing matchers
 * can be created for any triple pattern.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class IndexingMatcher extends Matcher {
    private enum PartOfSpeech {
        SUBJECT, PREDICATE, OBJECT, CONTEXT
    }

    private final String propertyKey;
    private final GraphSail.DataStore store;

    /**
     * Create a new indexing matcher based on the given triple pattern.
     *
     * @param s     whether the subject is specified
     * @param p     whether the predicate is specified
     * @param o     whether the object is specified
     * @param c     whether the context is specified
     * @param store the Blueprints data store
     */
    public IndexingMatcher(final boolean s,
                           final boolean p,
                           final boolean o,
                           final boolean c,
                           final GraphSail.DataStore store) {
        super(s, p, o, c);

        this.store = store;

        StringBuilder sb = new StringBuilder();
        if (c) {
            sb.append("c");
        }
        if (s) {
            sb.append("s");
        }
        if (p) {
            sb.append("p");
        }
        if (o) {
            sb.append("o");
        }
        propertyKey = sb.toString();
    }

    private <T> List<T> addLazy(List<T> list,
                                final T toAdd) {
        list = null == list ? new LinkedList<T>() : list;
        list.add(toAdd);
        return list;
    }

    private String appendToKey(final String key,
                               final String part) {
        return null == key ? part : key + GraphSail.SEPARATOR + part;
    }

    public Iterable<Edge> match(final Resource subject,
                                final URI predicate,
                                final Value object,
                                final Resource context,
                                final boolean includeInferred) {

        List<FilteredIterator.Criterion<Edge>> criteria = null;
        String key = null;

        if (c) {
            key = null == context ? GraphSail.NULL_CONTEXT_NATIVE : store.resourceToNative(context);
        } else if (null != context) {
            criteria = addLazy(criteria, new PartOfSpeechCriterion(PartOfSpeech.CONTEXT, store.resourceToNative(context)));
        }

        if (s) {
            key = appendToKey(key, store.resourceToNative(subject));
        } else if (null != subject) {
            criteria = addLazy(criteria, new PartOfSpeechCriterion(PartOfSpeech.SUBJECT, store.resourceToNative(subject)));
        }

        if (p) {
            key = appendToKey(key, store.uriToNative(predicate));
        } else if (null != predicate) {
            criteria = addLazy(criteria, new PartOfSpeechCriterion(PartOfSpeech.PREDICATE, store.uriToNative(predicate)));
        }

        if (o) {
            key = appendToKey(key, store.valueToNative(object));
        } else if (null != object) {
            criteria = addLazy(criteria, new PartOfSpeechCriterion(PartOfSpeech.OBJECT, store.valueToNative(object)));
        }

        if (!includeInferred) {
            criteria = addLazy(criteria, new NoInferenceCriterion());
        }

        Iterable<Edge> results = store.graph.getEdges(propertyKey, key);

        if (null != criteria) {
            FilteredIterator.Criterion<Edge> c = new FilteredIterator.CompoundCriterion<Edge>(criteria);
            results = new FilteredIterator<Edge>(results, c);
        }

        return results;
    }

    /**
     * Index a statement using this Matcher's triple pattern.  The subject, predicate, object and context values
     * are provided for efficiency only, and should agree with the corresponding values associated with the graph
     * structure of the edge.
     *
     * @param statement the edge to index as an RDF statement
     * @param subject   the subject of the statement
     * @param predicate the predicate of the statement
     * @param object    the object of the statement
     * @param context   the context of the statement
     */
    public void indexStatement(final Edge statement, final Resource subject, final URI predicate, final Value object, final String context) {
        StringBuilder sb = new StringBuilder();

        if (c) {
            sb.append(GraphSail.SEPARATOR).append(context);
        }

        if (s) {
            sb.append(GraphSail.SEPARATOR).append(store.resourceToNative(subject));
        }

        if (p) {
            sb.append(GraphSail.SEPARATOR).append(store.uriToNative(predicate));
        }

        if (o) {
            sb.append(GraphSail.SEPARATOR).append(store.valueToNative(object));
        }

        statement.setProperty(propertyKey, sb.toString().substring(1));
    }

    // TODO: unindexStatement

    private class PartOfSpeechCriterion implements FilteredIterator.Criterion<Edge> {
        private final PartOfSpeech partOfSpeech;
        private final String value;

        public PartOfSpeechCriterion(final PartOfSpeech partOfSpeech, final String value) {
            this.partOfSpeech = partOfSpeech;
            this.value = value;
        }

        public boolean fulfilledBy(final Edge edge) {
            //GraphSail.debugEdge(edge);
            //System.out.println("pos: " + partOfSpeech + ", value: " + value);

            switch (partOfSpeech) {
                case CONTEXT:
                    return value.equals(edge.getProperty(GraphSail.CONTEXT_PROP));
                case OBJECT:
                    return value.equals(store.getValueOf(edge.getVertex(Direction.IN)));
                case PREDICATE:
                    return value.equals(edge.getProperty(GraphSail.PREDICATE_PROP));
                case SUBJECT:
                    return value.equals(store.getValueOf(edge.getVertex(Direction.OUT)));
                default:
                    throw new IllegalStateException();
            }
        }
    }
}
