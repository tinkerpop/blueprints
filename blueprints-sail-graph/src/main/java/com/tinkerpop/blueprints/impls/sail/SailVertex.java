package com.tinkerpop.blueprints.impls.sail;


import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexQuery;
import com.tinkerpop.blueprints.util.DefaultVertexQuery;
import com.tinkerpop.blueprints.util.ElementHelper;
import com.tinkerpop.blueprints.util.MultiIterable;
import com.tinkerpop.blueprints.util.StringFactory;
import com.tinkerpop.blueprints.util.VerticesFromEdgesIterable;
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
import java.util.Arrays;
import java.util.Collections;
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

    protected SailVertex(final Value rawVertex, final SailGraph graph) {
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
        } catch (SailException e) {
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

    public <T> T removeProperty(final String key) {
        if (this.rawVertex instanceof Resource) {
            throw new RuntimeException(URI_BLANK_NODE_PROPERTIES);
        } else {
            final Literal oldLiteral = (Literal) this.rawVertex;
            if (key.equals(SailTokens.DATATYPE) || key.equals(SailTokens.LANGUAGE)) {
                this.rawVertex = new LiteralImpl(oldLiteral.getLabel());
                this.updateLiteral(oldLiteral, (Literal) this.rawVertex);
            }
            if (key.equals(SailTokens.DATATYPE)) {
                return (T) oldLiteral.getDatatype().toString();
            } else if (key.equals(SailTokens.LANGUAGE)) {
                return (T) oldLiteral.getLanguage();
            }
        }
        return null;
    }

    public <T> T getProperty(final String key) {
        if (key.equals(SailTokens.KIND)) {
            if (this.rawVertex instanceof Literal)
                return (T) SailTokens.LITERAL;
            else if (this.rawVertex instanceof URI)
                return (T) SailTokens.URI;
            else
                return (T) SailTokens.BNODE;
        }

        if (this.rawVertex instanceof Literal) {
            final Literal literal = (Literal) rawVertex;
            if (key.equals(SailTokens.DATATYPE)) {
                if (null != literal.getDatatype())
                    return (T) literal.getDatatype().stringValue();
                else
                    return null;
            } else if (key.equals(SailTokens.LANGUAGE)) {
                return (T) literal.getLanguage();
            } else if (key.equals(SailTokens.VALUE)) {
                return (T) castLiteral(literal);
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

    public Iterable<Edge> getEdges(final Direction direction, final String... labels) {
        if (direction.equals(Direction.OUT)) {
            return this.getOutEdges(labels);
        } else if (direction.equals(Direction.IN))
            return this.getInEdges(labels);
        else {
            return new MultiIterable<Edge>(Arrays.asList(this.getInEdges(labels), this.getOutEdges(labels)));
        }
    }

    public Iterable<Vertex> getVertices(final Direction direction, final String... labels) {
        return new VerticesFromEdgesIterable(this, direction, labels);
    }

    private Iterable<Edge> getOutEdges(final String... labels) {
        if (this.rawVertex instanceof Resource) {
            if (labels.length == 0) {
                return new SailEdgeIterable((Resource) this.rawVertex, null, null, this.graph);
            } else if (labels.length == 1) {
                return new SailEdgeIterable((Resource) this.rawVertex, new URIImpl(this.graph.expandPrefix(labels[0])), null, this.graph);
            } else {
                final List<Iterable<Edge>> edges = new ArrayList<Iterable<Edge>>();
                for (final String label : labels) {
                    edges.add(new SailEdgeIterable((Resource) this.rawVertex, new URIImpl(this.graph.expandPrefix(label)), null, this.graph));
                }
                return new MultiIterable<Edge>(edges);
            }
        } else {
            return Collections.emptyList();
        }

    }


    private Iterable<Edge> getInEdges(final String... labels) {
        if (labels.length == 0) {
            return new SailEdgeIterable(null, null, this.rawVertex, this.graph);
        } else if (labels.length == 1) {
            return new SailEdgeIterable(null, new URIImpl(this.graph.expandPrefix(labels[0])), this.rawVertex, this.graph);
        } else {
            final List<Iterable<Edge>> edges = new ArrayList<Iterable<Edge>>();
            for (final String label : labels) {
                edges.add(new SailEdgeIterable(null, new URIImpl(this.graph.expandPrefix(label)), this.rawVertex, this.graph));
            }
            return new MultiIterable<Edge>(edges);
        }
    }

    public Edge addEdge(final String label, final Vertex vertex) {
        return this.graph.addEdge(null, this, vertex, label);
    }

    public VertexQuery query() {
        return new DefaultVertexQuery(this);
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

    public void remove() {
        this.graph.removeVertex(this);
    }

    public int hashCode() {
        return this.getId().hashCode();
    }

    public boolean equals(final Object object) {
        return ElementHelper.areEqual(this, object);
    }

    public Object getId() {
        return this.rawVertex.toString();
    }
}
