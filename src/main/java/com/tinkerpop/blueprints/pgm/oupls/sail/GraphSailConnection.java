package com.tinkerpop.blueprints.pgm.oupls.sail;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import info.aduna.iteration.CloseableIteration;
import net.fortytwo.sesametools.CompoundCloseableIteration;
import net.fortytwo.sesametools.SailConnectionTripleSource;
import org.openrdf.model.*;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * A stateful connection to a BlueprintsSail RDF store interface.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class GraphSailConnection implements SailConnection {
    private static final Resource[] NULL_CONTEXT_ARRAY = {null};
    private static final String NULL_CONTEXT_NATIVE = "" + GraphSail.NULL_CONTEXT_PREFIX;

    private final GraphSail.DataStore store;

    private boolean open;

    public GraphSailConnection(final GraphSail.DataStore store) {
        this.store = store;

        if (store.manualTransactions) {
            ((TransactionalGraph) store.graph).startTransaction();
        }

        open = true;
    }

    public boolean isOpen() throws SailException {
        return open;
    }

    public void close() throws SailException {
        open = false;

        // Roll back any uncommitted operations.
        if (store.manualTransactions) {
            ((TransactionalGraph) store.graph).stopTransaction(TransactionalGraph.Conclusion.FAILURE);
        }
    }

    public CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluate(final TupleExpr query, final Dataset dataset, final BindingSet bindings, final boolean includeInferred) throws SailException {
        try {
            TripleSource tripleSource = new SailConnectionTripleSource(this, store.valueFactory, includeInferred);
            EvaluationStrategyImpl strategy = new EvaluationStrategyImpl(tripleSource, dataset);
            return strategy.evaluate(query, bindings);
        } catch (QueryEvaluationException e) {
            throw new SailException(e);
        }
    }

    public CloseableIteration<? extends Resource, SailException> getContextIDs() throws SailException {
        throw new UnsupportedOperationException("the getContextIDs operation is not yet supported");
    }

    public CloseableIteration<? extends Statement, SailException> getStatements(final Resource subject, final URI predicate, final Value object, final boolean includeInferred, final Resource... contexts) throws SailException {
        String s, p, o, c;

        int index = 0;

        if (null != subject) {
            index |= 0x1;
            s = resourceToNative(subject);
        } else {
            s = null;
        }

        if (null != predicate) {
            index |= 0x2;
            p = uriToNative(predicate);
        } else {
            p = null;
        }

        if (null != object) {
            index |= 0x4;
            o = valueToNative(object);
        } else {
            o = null;
        }

        if (0 == contexts.length) {
            return createIteration(store.matchers[index].match(s, p, o, null));
        } else {
            Collection<CloseableIteration<Statement, SailException>> iterations = new LinkedList<CloseableIteration<Statement, SailException>>();

            // TODO: as an optimization, filter on multiple contexts simultaneously (when context is not used in the matcher), rather than trying each context consecutively.
            for (Resource context : contexts) {
                index |= 0x8;
                c = null == context ? NULL_CONTEXT_NATIVE : resourceToNative(context);

                Matcher m = store.matchers[index];
                iterations.add(createIteration(m.match(s, p, o, c)));
            }

            return new CompoundCloseableIteration<Statement, SailException>(iterations);
        }
    }

    public long size(final Resource... contexts) throws SailException {
        if (0 == contexts.length) {
            return countIterator(store.matchers[0x0].match(null, null, null, null));
        } else {
            int count = 0;

            for (Resource context : contexts) {
                String c = null == context ? NULL_CONTEXT_NATIVE : resourceToNative(context);
                count += countIterator(store.matchers[0x8].match(null, null, null, c));
            }

            return count;
        }
    }

    private int countIterator(final Iterator i) {
        int count = 0;
        while (i.hasNext()) {
            count++;
            i.next();
        }
        return count;
    }

    public void commit() throws SailException {
        if (store.manualTransactions) {
            ((TransactionalGraph) store.graph).stopTransaction(TransactionalGraph.Conclusion.SUCCESS);
            ((TransactionalGraph) store.graph).startTransaction();
        }
    }

    public void rollback() throws SailException {
        if (store.manualTransactions) {
            ((TransactionalGraph) store.graph).stopTransaction(TransactionalGraph.Conclusion.FAILURE);
            ((TransactionalGraph) store.graph).startTransaction();
        }
    }

    // TODO: avoid duplicate statements

    public void addStatement(final Resource subject, final URI predicate, final Value object, final Resource... contexts) throws SailException {
        for (Resource context : ((0 == contexts.length) ? NULL_CONTEXT_ARRAY : contexts)) {
            String s = resourceToNative(subject);
            String p = uriToNative(predicate);
            String o = valueToNative(object);
            String c = null == context ? NULL_CONTEXT_NATIVE : resourceToNative(context);

            Vertex out = getOrCreateVertex(s);
            Vertex in = getOrCreateVertex(o);
            Edge edge = store.graph.addEdge(null, out, in, p);

            for (IndexingMatcher m : store.indexers) {
                //System.out.println("\t\tindexing with: " + m);
                m.indexStatement(edge, s, p, o, c);
            }

            //System.out.println("added (s: " + s + ", p: " + p + ", o: " + o + ", c: " + c + ")");
            //System.out.print("\t--> ");
            //BlueprintsSail.debugEdge(edge);
        }
    }

    private Vertex getOrCreateVertex(final String id) {
        Vertex v = store.getVertex(id);
        if (null == v) {
            v = store.addVertex(id);
        }
        return v;
    }

    public void removeStatements(final Resource subject, final URI predicate, final Value object, final Resource... contexts) throws SailException {
        Collection<Edge> edgesToRemove = new LinkedList<Edge>();

        String s, p, o, c;

        int index = 0;

        if (null != subject) {
            index |= 0x1;
            s = resourceToNative(subject);
        } else {
            s = null;
        }

        if (null != predicate) {
            index |= 0x2;
            p = uriToNative(predicate);
        } else {
            p = null;
        }

        if (null != object) {
            index |= 0x4;
            o = valueToNative(object);
        } else {
            o = null;
        }

        if (0 == contexts.length) {
            Iterator<Edge> i = store.matchers[index].match(s, p, o, null);
            while (i.hasNext()) {
                edgesToRemove.add(i.next());
            }
        } else {
            // TODO: as an optimization, filter on multiple contexts simultaneously (when context is not used in the matcher), rather than trying each context consecutively.
            for (Resource context : contexts) {
                index |= 0x8;
                c = null == context ? NULL_CONTEXT_NATIVE : resourceToNative(context);

                //System.out.println("matcher: " + indexes.matchers[index]);
                Iterator<Edge> i = store.matchers[index].match(s, p, o, c);
                while (i.hasNext()) {
                    edgesToRemove.add(i.next());
                }
            }
        }

        for (Edge e : edgesToRemove) {
            //System.out.println("removing this edge: " + e);
            store.graph.removeEdge(e);
        }
    }

    public void clear(final Resource... contexts) throws SailException {
        if (0 == contexts.length) {
            deleteEdgesInIterator(store.matchers[0x0].match(null, null, null, null));
        } else {
            for (Resource context : contexts) {
                String c = null == context ? NULL_CONTEXT_NATIVE : resourceToNative(context);
                deleteEdgesInIterator(store.matchers[0x8].match(null, null, null, c));
            }
        }
    }

    private void deleteEdgesInIterator(final Iterator<Edge> i) {
        //System.out.println(".............");
        while (i.hasNext()) {
            //System.out.println("----------------");
            Edge e = i.next();
            try {
                i.remove();
            } catch (UnsupportedOperationException x) {
                // TODO: it so happens that Neo4jGraph, the only IndexableGraph implementation so far tested whose
                // iterators don't support remove(), does *not* throw ConcurrentModificationExceptions when you
                // delete an edge in an iterator currently being traversed.  So for now, just ignore the
                // UnsupportedOperationException and proceed to delete the edge from the graph.
                //System.out.println("###################################");
            }
            Vertex h = e.getInVertex();
            Vertex t = e.getOutVertex();
            store.graph.removeEdge(e);
            if (!h.getInEdges().iterator().hasNext() && !h.getOutEdges().iterator().hasNext()) {
                store.graph.removeVertex(h);
            }
            if (!t.getOutEdges().iterator().hasNext() && !t.getInEdges().iterator().hasNext()) {
                store.graph.removeVertex(t);
            }
        }
        //System.out.println("================");
    }

    public CloseableIteration<? extends Namespace, SailException> getNamespaces() throws SailException {
        final Iterator<String> prefixes = store.namespaces.getPropertyKeys().iterator();

        return new CloseableIteration<Namespace, SailException>() {
            public void close() throws SailException {
                // Do nothing.
            }

            public boolean hasNext() throws SailException {
                return prefixes.hasNext();
            }

            public Namespace next() throws SailException {
                String prefix = prefixes.next();
                String uri = (String) store.namespaces.getProperty(prefix);
                return new NamespaceImpl(prefix, uri);
            }

            public void remove() throws SailException {
                throw new UnsupportedOperationException();
            }
        };
    }

    public String getNamespace(final String prefix) throws SailException {
        return (String) store.namespaces.getProperty(prefix);
    }

    public void setNamespace(final String prefix, final String uri) throws SailException {
        store.namespaces.setProperty(prefix, uri);
    }

    public void removeNamespace(final String prefix) throws SailException {
        store.namespaces.removeProperty(prefix);
    }

    public void clearNamespaces() throws SailException {
        throw new UnsupportedOperationException("The clearNamespaces operation is not yet supported");
    }

    // statement iteration /////////////////////////////////////////////////////

    private CloseableIteration<Statement, SailException> createIteration(final Iterator<Edge> iterator) {
        return store.volatileStatements
                ? new VolatileStatementIteration(iterator)
                : new StableStatementIteration(iterator);
    }

    private class StableStatementIteration implements CloseableIteration<Statement, SailException> {
        private final Iterator<Edge> iterator;

        public StableStatementIteration(final Iterator<Edge> iterator) {
            this.iterator = iterator;
        }

        public void close() throws SailException {
            // Do nothing.
        }

        public boolean hasNext() throws SailException {
            return iterator.hasNext();
        }

        public Statement next() throws SailException {
            Edge e = iterator.next();

            SimpleStatement s = new SimpleStatement();
            s.subject = (Resource) toSesame(store.getValueOf(e.getOutVertex()));
            s.predicate = (URI) toSesame(((String) e.getProperty(GraphSail.PREDICATE_PROP)));
            s.object = toSesame(store.getValueOf(e.getInVertex()));
            s.context = (Resource) toSesame(((String) e.getProperty(GraphSail.CONTEXT_PROP)));

            return s;
        }

        public void remove() throws SailException {
            throw new UnsupportedOperationException();
        }
    }

    private class VolatileStatementIteration implements CloseableIteration<Statement, SailException> {
        private final SimpleStatement s = new SimpleStatement();
        private final Iterator<Edge> iterator;

        public VolatileStatementIteration(final Iterator<Edge> iterator) {
            this.iterator = iterator;
        }

        public void close() throws SailException {
            // Do nothing.
        }

        public boolean hasNext() throws SailException {
            return iterator.hasNext();
        }

        public Statement next() throws SailException {
            Edge e = iterator.next();

            s.subject = (Resource) toSesame(store.getValueOf(e.getOutVertex()));
            s.predicate = (URI) toSesame(((String) e.getProperty(GraphSail.PREDICATE_PROP)));
            s.object = toSesame(store.getValueOf(e.getInVertex()));
            s.context = (Resource) toSesame(((String) e.getProperty(GraphSail.CONTEXT_PROP)));

            return s;
        }

        public void remove() throws SailException {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * A POJO statement containing a subject, predicate, object and context.
     * The purpose of using a special Statement implementation (rather than using an existing ValueFactory) is to
     * guarantee that it does not contain anything which would interfere
     * with JDK optimization aimed at eliminating creation of short-lived (Statement) objects.
     * You can observe the effect of such interference by un-commenting the <code>finalize()</code> method below.
     */
    private class SimpleStatement implements Statement {
        private Resource subject;
        private URI predicate;
        private Value object;
        private Resource context;

        public Resource getSubject() {
            return subject;
        }

        public URI getPredicate() {
            return predicate;
        }

        public Value getObject() {
            return object;
        }

        public Resource getContext() {
            return context;
        }

        /*
        protected void finalize() throws Throwable {
            super.finalize();
        }
        //*/
    }

    // value conversion ////////////////////////////////////////////////////////

    private Value toSesame(final String s) {
        int i;

        switch (s.charAt(0)) {
            case GraphSail.URI_PREFIX:
                return store.valueFactory.createURI(s.substring(2));
            case GraphSail.BLANK_NODE_PREFIX:
                return store.valueFactory.createBNode(s.substring(2));
            case GraphSail.PLAIN_LITERAL_PREFIX:
                return store.valueFactory.createLiteral(s.substring(2));
            case GraphSail.TYPED_LITERAL_PREFIX:
                i = s.indexOf(GraphSail.SEPARATOR, 2);
                return store.valueFactory.createLiteral(s.substring(i + 1), store.valueFactory.createURI(s.substring(2, i)));
            case GraphSail.LANGUAGE_TAG_LITERAL_PREFIX:
                i = s.indexOf(GraphSail.SEPARATOR, 2);
                return store.valueFactory.createLiteral(s.substring(i + 1), s.substring(2, i));
            case GraphSail.NULL_CONTEXT_PREFIX:
                return null;
            default:
                throw new IllegalStateException();
        }
    }

    private String valueToNative(final Value value) {
        if (value instanceof Resource) {
            return resourceToNative((Resource) value);
        } else if (value instanceof Literal) {
            return literalToNative((Literal) value);
        } else {
            throw new IllegalStateException("Value has unfamiliar type: " + value);
        }
    }

    private String resourceToNative(final Resource value) {
        if (value instanceof URI) {
            return uriToNative((URI) value);
        } else if (value instanceof BNode) {
            return bnodeToNative((BNode) value);
        } else {
            throw new IllegalStateException("Resource has unfamiliar type: " + value);
        }
    }

    private String uriToNative(final URI value) {
        return GraphSail.URI_PREFIX + GraphSail.SEPARATOR + value.toString();
    }

    private String bnodeToNative(final BNode value) {
        return GraphSail.BLANK_NODE_PREFIX + GraphSail.SEPARATOR + value.getID();
    }

    private String literalToNative(final Literal literal) {
        URI datatype = literal.getDatatype();

        if (null == datatype) {
            String language = literal.getLanguage();

            if (null == language) {
                return GraphSail.PLAIN_LITERAL_PREFIX + GraphSail.SEPARATOR + literal.getLabel();
            } else {
                return GraphSail.LANGUAGE_TAG_LITERAL_PREFIX + GraphSail.SEPARATOR + language + GraphSail.SEPARATOR + literal.getLabel();
            }
        } else {
            // FIXME
            return "" + GraphSail.TYPED_LITERAL_PREFIX + GraphSail.SEPARATOR + datatype + GraphSail.SEPARATOR + literal.getLabel();
        }
    }
}
