package com.tinkerpop.blueprints.pgm.oupls.sail;

import com.tinkerpop.blueprints.pgm.CloseableSequence;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import info.aduna.iteration.CloseableIteration;
import net.fortytwo.sesametools.CompoundCloseableIteration;
import net.fortytwo.sesametools.SailConnectionTripleSource;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.DefaultSailChangedEvent;
import org.openrdf.sail.helpers.NotifyingSailConnectionBase;
import org.openrdf.sail.inferencer.InferencerConnection;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * A stateful connection to a BlueprintsSail RDF store interface.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class GraphSailConnection extends NotifyingSailConnectionBase implements InferencerConnection {
    private static final Resource[] NULL_CONTEXT_ARRAY = {null};

    private final GraphSail.DataStore store;

    private final Collection<WriteAction> writeBuffer = new LinkedList<WriteAction>();

    private boolean statementsAdded;
    private boolean statementsRemoved;

    public GraphSailConnection(final GraphSail.DataStore store) {
        super(store.sail);
        this.store = store;

        if (store.manualTransactions) {
            ((TransactionalGraph) store.graph).startTransaction();
        }
    }

    protected void startTransactionInternal() throws SailException {
        statementsAdded = false;
        statementsRemoved = false;
    }

    public void commitInternal() throws SailException {
        if (store.manualTransactions) {
            ((TransactionalGraph) store.graph).stopTransaction(TransactionalGraph.Conclusion.SUCCESS);
            ((TransactionalGraph) store.graph).startTransaction();
        }

        if (statementsAdded || statementsRemoved) {
            DefaultSailChangedEvent e = new DefaultSailChangedEvent(store.sail);
            e.setStatementsAdded(statementsAdded);
            e.setStatementsRemoved(statementsRemoved);
            store.sail.notifySailChanged(e);
        }
    }

    public void rollbackInternal() throws SailException {
        if (store.manualTransactions) {
            ((TransactionalGraph) store.graph).stopTransaction(TransactionalGraph.Conclusion.FAILURE);
            ((TransactionalGraph) store.graph).startTransaction();
        }
    }

    public void closeInternal() throws SailException {
        // Roll back any uncommitted operations.
        if (store.manualTransactions) {
            ((TransactionalGraph) store.graph).stopTransaction(TransactionalGraph.Conclusion.FAILURE);
        }
    }

    public CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluateInternal(final TupleExpr query,
                                                                                               final Dataset dataset,
                                                                                               final BindingSet bindings,
                                                                                               final boolean includeInferred) throws SailException {
        try {
            TripleSource tripleSource = new SailConnectionTripleSource(this, store.valueFactory, includeInferred);
            EvaluationStrategyImpl strategy = new EvaluationStrategyImpl(tripleSource, dataset);
            return strategy.evaluate(query, bindings);
        } catch (QueryEvaluationException e) {
            throw new SailException(e);
        }
    }

    public CloseableIteration<? extends Resource, SailException> getContextIDsInternal() throws SailException {
        throw new UnsupportedOperationException("the getContextIDs operation is not yet supported");
    }

    public CloseableIteration<? extends Statement, SailException> getStatementsInternal(final Resource subject,
                                                                                        final URI predicate,
                                                                                        final Value object,
                                                                                        final boolean includeInferred,
                                                                                        final Resource... contexts) throws SailException {
        //System.out.println("getting: " + subject + ", " + predicate + ", " + object + ", " + includeInferred + ", " + contexts); System.out.flush();
        int index = 0;

        if (null != subject) {
            index |= 0x1;
        }

        if (null != predicate) {
            index |= 0x2;
        }

        if (null != object) {
            index |= 0x4;
        }

        if (0 == contexts.length) {
            return createIteration(store.matchers[index].match(subject, predicate, object, null));
        } else {
            Collection<CloseableIteration<Statement, SailException>> iterations = new LinkedList<CloseableIteration<Statement, SailException>>();

            // TODO: as an optimization, filter on multiple contexts simultaneously (when context is not used in the matcher), rather than trying each context consecutively.
            for (Resource context : contexts) {
                index |= 0x8;

                Matcher m = store.matchers[index];
                iterations.add(createIteration(m.match(subject, predicate, object, context)));
            }

            return new CompoundCloseableIteration<Statement, SailException>(iterations);
        }
    }

    public long sizeInternal(final Resource... contexts) throws SailException {
        if (0 == contexts.length) {
            return countIterator(store.matchers[0x0].match(null, null, null, null));
        } else {
            int count = 0;

            for (Resource context : contexts) {
                count += countIterator(store.matchers[0x8].match(null, null, null, context));
            }

            return count;
        }
    }

    private int countIterator(final CloseableSequence i) {
        try {
            int count = 0;
            while (i.hasNext()) {
                count++;
                i.next();
            }
            return count;
        } finally {
            i.close();
        }
    }

    public void addStatementInternal(final Resource subject,
                                     final URI predicate,
                                     final Value object,
                                     final Resource... contexts) throws SailException {
        addStatementInternal(false, subject, predicate, object, contexts);
    }

    private void addStatementInternal(final boolean inferred,
                                      final Resource subject,
                                      final URI predicate,
                                      final Value object,
                                      final Resource... contexts) throws SailException {
        //System.out.println("adding (" + inferred + "): " + subject + ", " + predicate + ", " + object + ", " + contexts); System.out.flush();

        if (!canWrite()) {
            WriteAction a = new WriteAction(ActionType.ADD);
            a.inferred = inferred;
            a.subject = subject;
            a.predicate = predicate;
            a.object = object;
            a.contexts = contexts;

            queueUpdate(a);
            return;
        }

        if (null == subject || null == predicate || null == object) {
            throw new IllegalArgumentException("null part-of-speech for to-be-added statement");
        }

        if (store.uniqueStatements) {
            if (0 == contexts.length) {
                removeStatementsInternal(inferred, subject, predicate, object, (Resource) null);
                if (!inferred) {
                    removeStatementsInternal(true, subject, predicate, object, (Resource) null);
                }
            } else {
                removeStatementsInternal(inferred, subject, predicate, object, contexts);
                if (!inferred) {
                    removeStatementsInternal(true, subject, predicate, object, contexts);
                }
            }
        }

        for (Resource context : ((0 == contexts.length) ? NULL_CONTEXT_ARRAY : contexts)) {
            String c = null == context ? GraphSail.NULL_CONTEXT_NATIVE : store.resourceToNative(context);

            Vertex out = getOrCreateVertex(subject);
            Vertex in = getOrCreateVertex(object);
            Edge edge = store.graph.addEdge(null, out, in, predicate.stringValue());
            if (inferred) {
                edge.setProperty(GraphSail.INFERRED, inferred);
            }

            for (IndexingMatcher m : store.indexers) {
                //System.out.println("\t\tindexing with: " + m);
                m.indexStatement(edge, subject, predicate, object, c);
            }

            if (hasConnectionListeners()) {
                Statement s = store.valueFactory.createStatement(subject, predicate, object, context);
                notifyStatementAdded(s);
            }

            //System.out.println("added (s: " + s + ", p: " + p + ", o: " + o + ", c: " + c + ")");
            //System.out.print("\t--> ");
            //BlueprintsSail.debugEdge(edge);
        }

        statementsAdded = true;
        //System.out.println("\tdone adding");
    }

    private Vertex getOrCreateVertex(final Value value) {
        Vertex v = store.findVertex(value);
        if (null == v) {
            v = store.addVertex(value);
        }
        return v;
    }

    public void removeStatementsInternal(final Resource subject, final URI predicate, final Value object, final Resource... contexts) throws SailException {
        removeStatementsInternal(false, subject, predicate, object, contexts);
    }

    private void removeStatementsInternal(final boolean inferred,
                                          final Resource subject,
                                          final URI predicate,
                                          final Value object,
                                          final Resource... contexts) throws SailException {
        //System.out.println("removing (" + inferred + "): " + subject + ", " + predicate + ", " + object + ", " + contexts); System.out.flush();

        if (!canWrite()) {
            WriteAction a = new WriteAction(ActionType.REMOVE);
            a.inferred = inferred;
            a.subject = subject;
            a.predicate = predicate;
            a.object = object;
            a.contexts = contexts;

            queueUpdate(a);
            return;
        }

        Collection<Edge> edgesToRemove = new LinkedList<Edge>();

        int index = 0;

        if (null != subject) {
            index |= 0x1;
        }

        if (null != predicate) {
            index |= 0x2;
        }

        if (null != object) {
            index |= 0x4;
        }

        if (0 == contexts.length) {
            CloseableSequence<Edge> i = store.matchers[index].match(subject, predicate, object, null);
            try {
                while (i.hasNext()) {
                    edgesToRemove.add(i.next());
                }
            } finally {
                i.close();
            }
        } else {
            // TODO: as an optimization, filter on multiple contexts simultaneously (when context is not used in the matcher), rather than trying each context consecutively.
            for (Resource context : contexts) {
                index |= 0x8;

                //System.out.println("matcher: " + indexes.matchers[index]);
                CloseableSequence<Edge> i = store.matchers[index].match(subject, predicate, object, context);
                try {
                    while (i.hasNext()) {
                        Edge e = i.next();
                        Boolean b = (Boolean) e.getProperty(GraphSail.INFERRED);
                        if ((!inferred && null == b)
                                || (inferred && null != b && b)) {
                            edgesToRemove.add(e);
                        }
                    }
                } finally {
                    i.close();
                }
            }
        }

        for (Edge e : edgesToRemove) {
            SimpleStatement s;
            if (hasConnectionListeners()) {
                s = new SimpleStatement();
                fillStatement(s, e);
            } else {
                s = null;
            }

            //System.out.println("removing this edge: " + e);
            removeEdge(e);

            if (null != s) {
                notifyStatementRemoved(s);
            }
        }

        if (0 < edgesToRemove.size()) {
            statementsRemoved = true;
        }

        //System.out.println("\tdone removing");
    }

    public void clearInternal(final Resource... contexts) throws SailException {
        clearInternal(false, contexts);
    }

    private void clearInternal(final boolean inferred,
                               final Resource... contexts) throws SailException {
        //System.out.println("clearing (" + inferred + "): " + contexts); System.out.flush();

        if (!canWrite()) {
            WriteAction a = new WriteAction(ActionType.CLEAR);
            a.inferred = inferred;
            a.contexts = contexts;

            queueUpdate(a);
            return;
        }

        if (0 == contexts.length) {
            deleteEdgesInIterator(inferred, store.matchers[0x0].match(null, null, null, null));
        } else {
            for (Resource context : contexts) {
                // Note: order of operands to the "or" is important here
                deleteEdgesInIterator(inferred, store.matchers[0x8].match(null, null, null, context));
            }
        }
    }

    private void deleteEdgesInIterator(final boolean inferred,
                                       final CloseableSequence<Edge> i) {
        try {
            while (i.hasNext()) {
                Edge e = i.next();

                Boolean b = (Boolean) e.getProperty(GraphSail.INFERRED);
                if ((!inferred && null == b)
                        || (inferred && null != b && b)) {
                    SimpleStatement s;
                    if (hasConnectionListeners()) {
                        s = new SimpleStatement();
                        fillStatement(s, e);
                    } else {
                        s = null;
                    }

                    try {
                        i.remove();
                    } catch (UnsupportedOperationException x) {
                        // TODO: it so happens that Neo4jGraph, the only IndexableGraph implementation so far tested whose
                        // iterators don't support remove(), does *not* throw ConcurrentModificationExceptions when you
                        // delete an edge in an iterator currently being traversed.  So for now, just ignore the
                        // UnsupportedOperationException and proceed to delete the edge from the graph.
                    }

                    removeEdge(e);

                    if (null != s) {
                        notifyStatementRemoved(s);
                    }

                    statementsRemoved = true;
                }
            }
        } finally {
            i.close();
        }
    }

    private void removeEdge(final Edge edge) {
        Vertex h = edge.getInVertex();
        Vertex t = edge.getOutVertex();
        store.graph.removeEdge(edge);
        if (!h.getInEdges().iterator().hasNext() && !h.getOutEdges().iterator().hasNext()) {
            try {
                store.graph.removeVertex(h);
            } catch (IllegalStateException ex) {
                // Just keep going.  This is a hack for Neo4j vertices which appear in more than
                // one to-be-deleted edge.
            }
        }
        if (!t.getOutEdges().iterator().hasNext() && !t.getInEdges().iterator().hasNext()) {
            try {
                store.graph.removeVertex(t);
            } catch (IllegalStateException ex) {
                // Just keep going.  This is a hack for Neo4j vertices which appear in more than
                // one to-be-deleted edge.
            }
        }
    }

    public CloseableIteration<? extends Namespace, SailException> getNamespacesInternal() throws SailException {
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

    public String getNamespaceInternal(final String prefix) throws SailException {
        return (String) store.namespaces.getProperty(prefix);
    }

    public void setNamespaceInternal(final String prefix, final String uri) throws SailException {
        store.namespaces.setProperty(prefix, uri);
    }

    public void removeNamespaceInternal(final String prefix) throws SailException {
        store.namespaces.removeProperty(prefix);
    }

    public void clearNamespacesInternal() throws SailException {
        throw new UnsupportedOperationException("The clearNamespaces operation is not yet supported");
    }

    // write lock //////////////////////////////////////////////////////////////

    private int writeSemaphore = 0;

    private boolean canWrite() {
        return 0 == writeSemaphore;
    }

    private void writeSemaphoreUp() {
        writeSemaphore++;
    }

    private void writeSemaphoreDown() throws SailException {
        writeSemaphore--;

        if (0 == writeSemaphore) {
            flushWrites();
        }
    }

    private void queueUpdate(final WriteAction a) throws SailException {
        if (0 == writeSemaphore) {
            a.execute();
        } else {
            writeBuffer.add(a);
        }
    }

    private void flushWrites() throws SailException {
        for (WriteAction a : writeBuffer) {
            switch (a.type) {
                case ADD:
                    addStatementInternal(true, a.subject, a.predicate, a.object, a.contexts);
                    break;
                case REMOVE:
                    removeStatementsInternal(true, a.subject, a.predicate, a.object, a.contexts);
                    break;
                case CLEAR:
                    clearInternal(true, a.contexts);
                    break;
            }
        }

        writeBuffer.clear();
    }

    // inference ///////////////////////////////////////////////////////////////

    private enum ActionType {ADD, REMOVE, CLEAR}

    private class WriteAction {
        public final ActionType type;

        public WriteAction(final ActionType type) {
            this.type = type;
        }

        public Resource subject;
        public URI predicate;
        public Value object;
        public Resource[] contexts;
        public boolean inferred = true;

        public void execute() throws SailException {
            switch (type) {
                case ADD:
                    addStatementInternal(inferred, subject, predicate, object, contexts);
                    break;
                case REMOVE:
                    removeStatementsInternal(inferred, subject, predicate, object, contexts);
                    break;
                case CLEAR:
                    clearInternal(inferred, contexts);
                    break;
            }
        }
    }

    @Override
    public boolean addInferredStatement(final Resource subject,
                                        final URI predicate,
                                        final Value object,
                                        final Resource... contexts) throws SailException {
        for (Resource context : (0 == contexts.length ? NULL_CONTEXT_ARRAY : contexts)) {
            boolean doAdd = true;
            if (store.uniqueStatements) {
                CloseableIteration<?, SailException> i
                        = getStatementsInternal(subject, predicate, object, true, context);
                try {
                    if (i.hasNext()) {
                        doAdd = false;
                    }
                } finally {
                    i.close();
                }
            }

            if (doAdd) {
                addStatementInternal(true, subject, predicate, object, context);
            }
        }

        // Note: the meaning of the return value is not documented (in the Sesame 2.3.2 JavaDocs)
        return false;
    }

    @Override
    public boolean removeInferredStatement(final Resource subject,
                                           final URI predicate,
                                           final Value object,
                                           final Resource... contexts) throws SailException {
        removeStatementsInternal(true, subject, predicate, object, contexts);

        // Note: the meaning of the return value is not documented (in the Sesame 2.3.2 JavaDocs)
        return false;
    }

    @Override
    public void clearInferred(final Resource... contexts) throws SailException {
        clearInternal(true, contexts);
    }

    @Override
    public void flushUpdates() throws SailException {
        // No-op
    }

    // statement iteration /////////////////////////////////////////////////////

    private CloseableIteration<Statement, SailException> createIteration(final CloseableSequence<Edge> iterator) {
        return store.volatileStatements
                ? new VolatileStatementIteration(iterator)
                : new StableStatementIteration(iterator);
    }

    private class StableStatementIteration implements CloseableIteration<Statement, SailException> {
        private final CloseableSequence<Edge> iterator;
        private boolean closed = false;

        public StableStatementIteration(final CloseableSequence<Edge> iterator) {
            writeSemaphoreUp();
            this.iterator = iterator;
        }

        public void close() throws SailException {
            if (!closed) {
                closed = true;
                iterator.close();
                writeSemaphoreDown();
            }
        }

        public boolean hasNext() throws SailException {
            // Note: this used to throw an IllegalStateException if the iteration had already been closed,
            // but such is not the behavior of Aduna's LookAheadIteration, which simply does not provide any more
            // elements if the iteration has already been closed.
            // The CloseableIteration API says nothing about what to expect from a closed iteration,
            // so the behavior of LookAheadIteration will be taken as normative.
            return !closed && iterator.hasNext();
        }

        public Statement next() throws SailException {
            if (closed) {
                throw new IllegalStateException("already closed");
            }

            Edge e = iterator.next();

            SimpleStatement s = new SimpleStatement();
            fillStatement(s, e);

            return s;
        }

        public void remove() throws SailException {
            throw new UnsupportedOperationException();
        }
    }

    private void fillStatement(final SimpleStatement s,
                               final Edge e) {
        s.subject = (Resource) toSesame(e.getOutVertex());
        s.predicate = (URI) toSesame(((String) e.getProperty(GraphSail.PREDICATE_PROP)));
        s.object = toSesame(e.getInVertex());
        s.context = (Resource) toSesame(((String) e.getProperty(GraphSail.CONTEXT_PROP)));
    }

    private class VolatileStatementIteration implements CloseableIteration<Statement, SailException> {
        private final SimpleStatement s = new SimpleStatement();
        private final CloseableSequence<Edge> iterator;

        public VolatileStatementIteration(final CloseableSequence<Edge> iterator) {
            this.iterator = iterator;
        }

        public void close() throws SailException {
            iterator.close();
        }

        public boolean hasNext() throws SailException {
            return iterator.hasNext();
        }

        public Statement next() throws SailException {
            Edge e = iterator.next();

            fillStatement(s, e);

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

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("(").append(subject).append(", ").append(predicate).append(", ").append(object);
            if (null != context) {
                sb.append(", ").append(context);
            }
            sb.append(")");
            return sb.toString();
        }
    }

    // value conversion ////////////////////////////////////////////////////////

    private Value toSesame(final Vertex v) {
        String value = (String) v.getProperty(GraphSail.VALUE);
        String kind = (String) v.getProperty(GraphSail.KIND);
        if (kind.equals(GraphSail.URI)) {
            return store.valueFactory.createURI(value);
        } else if (kind.equals(GraphSail.LITERAL)) {
            String datatype = (String) v.getProperty(GraphSail.TYPE);
            String lang = (String) v.getProperty(GraphSail.LANG);
            return null != datatype
                    ? store.valueFactory.createLiteral(value, store.valueFactory.createURI(datatype))
                    : null != lang
                    ? store.valueFactory.createLiteral(value, lang)
                    : store.valueFactory.createLiteral(value);
        } else if (kind.equals(GraphSail.BNODE)) {
            return store.valueFactory.createBNode(value);
        } else {
            throw new IllegalStateException("unexpected resource kind: " + kind);
        }
    }

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
}
