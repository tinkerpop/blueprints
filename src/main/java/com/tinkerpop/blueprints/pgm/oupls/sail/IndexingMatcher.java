package com.tinkerpop.blueprints.pgm.oupls.sail;

import com.tinkerpop.blueprints.pgm.Edge;

import java.util.Iterator;
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

    private final String propertyName;
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
    public IndexingMatcher(final boolean s, final boolean p, final boolean o, final boolean c, final GraphSail.DataStore store) {
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
        propertyName = sb.toString();
    }

    public Iterator<Edge> match(final String subject, final String predicate, final String object, final String context) {
        // FIXME: the temporary linked list is a little wasty
        List<PartOfSpeechCriterion> criteria = new LinkedList<PartOfSpeechCriterion>();

        StringBuilder sb = new StringBuilder();

        if (c) {
            sb.append(GraphSail.SEPARATOR).append(context);
        } else if (null != context) {
            criteria.add(new PartOfSpeechCriterion(PartOfSpeech.CONTEXT, context));
        }
        if (s) {
            sb.append(GraphSail.SEPARATOR).append(subject);
        } else if (null != subject) {
            criteria.add(new PartOfSpeechCriterion(PartOfSpeech.SUBJECT, subject));
        }

        if (p) {
            sb.append(GraphSail.SEPARATOR).append(predicate);
        } else if (null != predicate) {
            criteria.add(new PartOfSpeechCriterion(PartOfSpeech.PREDICATE, predicate));
        }

        if (o) {
            sb.append(GraphSail.SEPARATOR).append(object);
        } else if (null != object) {
            criteria.add(new PartOfSpeechCriterion(PartOfSpeech.OBJECT, object));
        }

        //System.out.println("spoc: " + s + " " + p + " " + o + " " + c);
        //System.out.println("\ts: " + subject + ", p: " + predicate + ", o: " + object + ", c: " + context);

        Iterator<Edge> results = store.edges.get(propertyName, sb.toString().substring(1)).iterator();

        for (PartOfSpeechCriterion m : criteria) {
            results = new FilteredIterator<Edge>(results, m);
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
    public void indexStatement(final Edge statement, final String subject, final String predicate, final String object, final String context) {
        StringBuilder sb = new StringBuilder();

        if (c) {
            sb.append(GraphSail.SEPARATOR).append(context);
        }

        if (s) {
            sb.append(GraphSail.SEPARATOR).append(subject);
        }

        if (p) {
            sb.append(GraphSail.SEPARATOR).append(predicate);
        }

        if (o) {
            sb.append(GraphSail.SEPARATOR).append(object);
        }

        //edges.put(propertyName, sb.toString(), edge);
        statement.setProperty(propertyName, sb.toString().substring(1));
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
                    return value.equals(store.getValueOf(edge.getInVertex()));
                case PREDICATE:
                    return value.equals(edge.getProperty(GraphSail.PREDICATE_PROP));
                case SUBJECT:
                    return value.equals(store.getValueOf(edge.getOutVertex()));
                default:
                    throw new IllegalStateException();
            }
        }
    }
}
