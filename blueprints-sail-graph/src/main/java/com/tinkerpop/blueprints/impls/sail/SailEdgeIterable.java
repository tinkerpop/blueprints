package com.tinkerpop.blueprints.impls.sail;

import com.tinkerpop.blueprints.Edge;
import info.aduna.iteration.CloseableIteration;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sail.SailException;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
class SailEdgeIterable implements Iterable<Edge> {


    private final SailGraph graph;
    private final Resource subject;
    private final URI predicate;
    private final Value object;

    public SailEdgeIterable(Resource subject, URI predicate, Value object, final SailGraph graph) {
        this.subject = subject;
        this.object = object;
        this.predicate = predicate;
        this.graph = graph;
    }

    public Iterator<Edge> iterator() {
        return new SailEdgeIterator();
    }

    private class SailEdgeIterator implements Iterator<Edge> {
        private final CloseableIteration<? extends Statement, SailException> statements;

        public SailEdgeIterator() {
            try {
                this.statements = graph.getSailConnection().get().getStatements(subject, predicate, object, false);
            } catch (SailException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public boolean hasNext() {
            try {
                if (this.statements.hasNext())
                    return true;
                else {
                    this.statements.close();
                    return false;
                }
            } catch (SailException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        public Edge next() {
            try {
                return new SailEdge(this.statements.next(), graph);
            } catch (SailException e) {
                throw new RuntimeException(e.getMessage());
            } catch (NoSuchElementException e) {
                try {
                    this.statements.close();
                } catch (SailException e2) {
                    throw new RuntimeException(e2.getMessage(), e2);
                }
                throw e;
            }
        }
    }


}
