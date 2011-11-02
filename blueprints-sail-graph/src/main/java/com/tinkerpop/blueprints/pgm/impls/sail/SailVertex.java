package com.tinkerpop.blueprints.pgm.impls.sail;


import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.MultiIterable;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.impls.sail.util.SailEdgeSequence;
import info.aduna.iteration.CloseableIteration;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.sail.SailException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class SailVertex implements Vertex {

    protected Value rawVertex;
    protected SailGraph graph;

    private static final String URI_BLANK_NODE_PROPERTIES = "RDF graph URI and blank node vertices can not have properties";
    private static Map<String, String> dataTypeToClass = new HashMap<String, String>();

    static {
        dataTypeToClass.put(SailTokens.XSD_NS + "string", "java.lang.String");
        dataTypeToClass.put(SailTokens.XSD_NS + "int", "java.lang.Integer");
        dataTypeToClass.put(SailTokens.XSD_NS + "integer", "java.lang.Integer");
        dataTypeToClass.put(SailTokens.XSD_NS + "float", "java.lang.Float");
        dataTypeToClass.put(SailTokens.XSD_NS + "double", "java.lang.Double");
    }

    public SailVertex(final Value rawVertex, final SailGraph graph) {
        this.rawVertex = rawVertex;
        this.graph = graph;
    }

    public Value getRawVertex() {
        return this.rawVertex;
    }

    private void updateLiteral(final Literal oldLiteral, final Literal newLiteral) {
        try {
            final Set<Statement> statements = new HashSet<Statement>();
            final CloseableIteration<? extends Statement, SailException> results = this.graph.getSailConnection().get().getStatements(null, null, oldLiteral, false);
            while (results.hasNext()) {
                statements.add(results.next());
            }
            results.close();
            this.graph.getSailConnection().get().removeStatements(null, null, oldLiteral);
            for (Statement statement : statements) {
                SailHelper.addStatement(statement.getSubject(), statement.getPredicate(), newLiteral, statement.getContext(), this.graph.getSailConnection().get());
            }
            this.graph.autoStopTransaction(TransactionalGraph.Conclusion.SUCCESS);

        } catch (SailException e) {
            this.graph.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void setProperty(final String key, final Object value) {
        if (this.rawVertex instanceof Resource) {
            throw new RuntimeException(URI_BLANK_NODE_PROPERTIES);
        } else {
            boolean update = false;
            final Literal oldLiteral = (Literal) this.rawVertex;
            if (key.equals(SailTokens.DATATYPE)) {
                this.rawVertex = new LiteralImpl(oldLiteral.getLabel(), new URIImpl(this.graph.expandPrefix(value.toString())));
                update = true;
            } else if (key.equals(SailTokens.LANGUAGE)) {
                this.rawVertex = new LiteralImpl(oldLiteral.getLabel(), value.toString());
                update = true;
            }
            if (update) {
                this.updateLiteral(oldLiteral, (Literal) this.rawVertex);
            }
        }
    }

    public Object removeProperty(final String key) {
        if (this.rawVertex instanceof Resource) {
            throw new RuntimeException(URI_BLANK_NODE_PROPERTIES);
        } else {
            final Literal oldLiteral = (Literal) this.rawVertex;
            if (key.equals(SailTokens.DATATYPE) || key.equals(SailTokens.LANGUAGE)) {
                this.rawVertex = new LiteralImpl(oldLiteral.getLabel());
                this.updateLiteral(oldLiteral, (Literal) this.rawVertex);
            }
            if (key.equals(SailTokens.DATATYPE)) {
                return oldLiteral.getDatatype().toString();
            } else if (key.equals(SailTokens.LANGUAGE)) {
                return oldLiteral.getLanguage();
            }
        }
        return null;
    }

    public Object getProperty(final String key) {
        if (key.equals(SailTokens.KIND)) {
            if (this.rawVertex instanceof Literal)
                return SailTokens.LITERAL;
            else if (this.rawVertex instanceof URI)
                return SailTokens.URI;
            else
                return SailTokens.BNODE;
        }

        if (this.rawVertex instanceof Literal) {
            final Literal literal = (Literal) rawVertex;
            if (key.equals(SailTokens.DATATYPE)) {
                if (null != literal.getDatatype())
                    return literal.getDatatype().stringValue();
                else
                    return null;
            } else if (key.equals(SailTokens.LANGUAGE)) {
                return literal.getLanguage();
            } else if (key.equals(SailTokens.VALUE)) {
                return castLiteral(literal);
            }
        }
        return null;
    }

    public Set<String> getPropertyKeys() {
        final Set<String> keys = new HashSet<String>();
        if (this.rawVertex instanceof Literal) {
            if (null != this.getProperty(SailTokens.DATATYPE)) {
                keys.add(SailTokens.DATATYPE);
            } else if (null != this.getProperty(SailTokens.LANGUAGE)) {
                keys.add(SailTokens.LANGUAGE);
            }
            keys.add(SailTokens.VALUE);
        }
        keys.add(SailTokens.KIND);
        return keys;
    }

    public Iterable<Edge> getOutEdges(final String... labels) {
        if (this.rawVertex instanceof Resource) {
            try {
                if (labels.length == 0) {
                    return new SailEdgeSequence(this.graph.getSailConnection().get().getStatements((Resource) this.rawVertex, null, null, false), this.graph);
                } else if (labels.length == 1) {
                    return new SailEdgeSequence(this.graph.getSailConnection().get().getStatements((Resource) this.rawVertex, new URIImpl(this.graph.expandPrefix(labels[0])), null, false), this.graph);
                } else {
                    final List<Iterable<Edge>> edges = new ArrayList<Iterable<Edge>>();
                    for (final String label : labels) {
                        edges.add(new SailEdgeSequence(this.graph.getSailConnection().get().getStatements((Resource) this.rawVertex, new URIImpl(this.graph.expandPrefix(label)), null, false), this.graph));
                    }
                    return new MultiIterable<Edge>(edges);
                }
            } catch (SailException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        } else {
            return new SailEdgeSequence();
        }

    }


    public Iterable<Edge> getInEdges(final String... labels) {
        try {
            if (labels.length == 0) {
                return new SailEdgeSequence(this.graph.getSailConnection().get().getStatements(null, null, this.rawVertex, false), this.graph);
            } else if (labels.length == 1) {
                return new SailEdgeSequence(this.graph.getSailConnection().get().getStatements(null, new URIImpl(this.graph.expandPrefix(labels[0])), (Resource) this.rawVertex, false), this.graph);
            } else {
                final List<Iterable<Edge>> edges = new ArrayList<Iterable<Edge>>();
                for (final String label : labels) {
                    edges.add(new SailEdgeSequence(this.graph.getSailConnection().get().getStatements(null, new URIImpl(this.graph.expandPrefix(label)), (Resource) this.rawVertex, false), this.graph));
                }
                return new MultiIterable<Edge>(edges);
            }
        } catch (SailException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    public String toString() {
        return StringFactory.vertexString(this);
    }

    protected static Object castLiteral(final Literal literal) {
        if (null != literal.getDatatype()) {
            String className = dataTypeToClass.get(literal.getDatatype().stringValue());
            if (null == className)
                return literal.getLabel();
            else {
                try {
                    Class c = Class.forName(className);
                    if (c == String.class) {
                        return literal.getLabel();
                    } else if (c == Float.class) {
                        return Float.valueOf(literal.getLabel());
                    } else if (c == Integer.class) {
                        return Integer.valueOf(literal.getLabel());
                    } else if (c == Double.class) {
                        return Double.valueOf(literal.getLabel());
                    } else {
                        return literal.getLabel();
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    return literal.getLabel();
                }
            }
        } else {
            return literal.getLabel();
        }
    }

    public int hashCode() {
        return this.getId().hashCode();
    }

    public boolean equals(final Object object) {
        return object instanceof SailVertex && ((SailVertex) object).getId().equals(this.getId());
    }

    public Object getId() {
        return this.rawVertex.toString();
    }
}
