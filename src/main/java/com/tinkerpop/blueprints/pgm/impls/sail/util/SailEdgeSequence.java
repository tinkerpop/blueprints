package com.tinkerpop.blueprints.pgm.impls.sail.util;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.impls.sail.SailEdge;
import com.tinkerpop.blueprints.pgm.impls.sail.SailGraph;
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
    private final SailGraph graph;

    public SailEdgeSequence(final CloseableIteration<? extends Statement, SailException> statements, final SailGraph graph) {
        this.statements = statements;
        this.graph = graph;
    }

    public SailEdgeSequence() {
        this.statements = null;
        this.graph = null;
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
            if (this.statements.hasNext())
                return true;
            else {
                this.statements.close();
                return false;
            }
        } catch (SailException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Edge next() {
        if (null == this.statements)
            throw new NoSuchElementException();

        try {
            return new SailEdge(this.statements.next(), this.graph);
        } catch (SailException e) {
            throw new RuntimeException(e.getMessage());
        } catch (NoSuchElementException e) {
            try {
                this.statements.close();
            } catch (SailException e2) {
                throw new RuntimeException(e2.getMessage());
            }
            throw e;
        }
    }
}
