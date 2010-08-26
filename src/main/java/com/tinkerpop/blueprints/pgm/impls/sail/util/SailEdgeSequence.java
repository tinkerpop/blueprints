package com.tinkerpop.blueprints.pgm.impls.sail.util;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.impls.sail.SailEdge;
import info.aduna.iteration.CloseableIteration;
import org.openrdf.model.Statement;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class SailEdgeSequence implements Iterable<Edge>, Iterator<Edge> {

    private final CloseableIteration<? extends Statement, SailException> statements;
    private final SailConnection sailConnection;

    public SailEdgeSequence(final CloseableIteration<? extends Statement, SailException> statements, final SailConnection sailConnection) {
        this.statements = statements;
        this.sailConnection = sailConnection;
    }

    public SailEdgeSequence() {
        this.statements = null;
        this.sailConnection = null;
    }

    public Iterator<Edge> iterator() {
        return this;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public boolean hasNext() {
        if (null == this.statements)
            return false;

        try {
            return this.statements.hasNext();
        } catch (SailException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Edge next() {
        if (null == this.statements)
            throw new NoSuchElementException();

        try {
            return new SailEdge(this.statements.next(), this.sailConnection);
        } catch (SailException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
