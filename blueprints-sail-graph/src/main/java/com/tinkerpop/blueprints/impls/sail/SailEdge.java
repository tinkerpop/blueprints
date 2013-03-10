package com.tinkerpop.blueprints.impls.sail;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.ElementHelper;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class SailEdge implements Edge {

    protected Statement rawEdge;
    protected SailGraph graph;

    private static final String NAMED_GRAPH_PROPERTY = "RDF graph edges can only have context/named graph (ng) properties";

    protected SailEdge(final Statement rawEdge, final SailGraph graph) {
        this.rawEdge = rawEdge;
        this.graph = graph;
    }

    public Statement getRawEdge() {
        return this.rawEdge;
    }

    public String getLabel() {
        return this.rawEdge.getPredicate().stringValue();
    }

    public String getNamedGraph() {
        return (String) this.getProperty(SailTokens.NAMED_GRAPH);
    }

    public void setNamedGraph(final String namedGraph) {
        this.setProperty(SailTokens.NAMED_GRAPH, namedGraph);
    }

    public boolean hasNamedGraph() {
        return this.getProperty(SailTokens.NAMED_GRAPH) != null;
    }

    public Set<String> getPropertyKeys() {
        Set<String> keys = new HashSet<String>();
        if (null != this.rawEdge.getContext())
            keys.add(SailTokens.NAMED_GRAPH);
        return keys;
    }

    public <T> T getProperty(final String key) {
        if (key.equals(SailTokens.NAMED_GRAPH)) {
            Resource resource = this.rawEdge.getContext();
            if (null == resource)
                return null;
            else
                return (T) resource.stringValue();
        } else
            return null;
    }

    public void setProperty(final String key, final Object value) {
        if (key.equals(SailTokens.NAMED_GRAPH)) {
            try {
                URI namedGraph = new URIImpl(this.graph.expandPrefix(value.toString()));
                SailHelper.removeStatement(this.rawEdge, this.graph.getSailConnection().get());
                this.rawEdge = new ContextStatementImpl(this.rawEdge.getSubject(), this.rawEdge.getPredicate(), this.rawEdge.getObject(), namedGraph);
                SailHelper.addStatement(this.rawEdge, this.graph.getSailConnection().get());
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        } else {
            throw new IllegalArgumentException(NAMED_GRAPH_PROPERTY);
        }
    }

    public <T> T removeProperty(final String key) {
        if (key.equals(SailTokens.NAMED_GRAPH)) {
            try {
                Resource ng = this.rawEdge.getContext();
                SailHelper.removeStatement(this.rawEdge, this.graph.getSailConnection().get());
                this.rawEdge = new StatementImpl(this.rawEdge.getSubject(), this.rawEdge.getPredicate(), this.rawEdge.getObject());
                SailHelper.addStatement(this.rawEdge, this.graph.getSailConnection().get());
                return (T) ng;
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        } else {
            throw new IllegalArgumentException(NAMED_GRAPH_PROPERTY);
        }
    }

    public Vertex getVertex(final Direction direction) {
        if (direction.equals(Direction.OUT))
            return new SailVertex(this.rawEdge.getSubject(), this.graph);
        else if (direction.equals(Direction.IN))
            return new SailVertex(this.rawEdge.getObject(), this.graph);
        else
            throw ExceptionFactory.bothIsNotSupported();
    }

    public String toString() {
        final String outVertex = this.graph.prefixNamespace(this.rawEdge.getSubject().stringValue());
        final String edgeLabel = this.graph.prefixNamespace(this.rawEdge.getPredicate().stringValue());
        String inVertex;
        if (this.rawEdge.getObject() instanceof Resource)
            inVertex = this.graph.prefixNamespace(this.rawEdge.getObject().stringValue());
        else
            inVertex = literalString((Literal) this.rawEdge.getObject());

        String namedGraph = null;
        if (null != this.rawEdge.getContext()) {
            namedGraph = this.graph.prefixNamespace(this.rawEdge.getContext().stringValue());
        }

        String edgeString = "e[" + outVertex + " - " + edgeLabel + " -> " + inVertex + "]";
        if (null != namedGraph) {
            edgeString = edgeString + "<" + namedGraph + ">";
        }

        return edgeString;
    }

    private String literalString(final Literal literal) {
        final String language = literal.getLanguage();
        final URI datatype = literal.getDatatype();
        if (null != datatype) {
            return "\"" + literal.getLabel() + "\"^^<" + this.graph.prefixNamespace(datatype.stringValue()) + ">";
        } else if (null != language) {
            return "\"" + literal.getLabel() + "\"@" + language;
        } else {
            return "\"" + literal.getLabel() + "\"";
        }
    }

    public void remove() {
        this.graph.removeEdge(this);
    }

    public boolean equals(final Object object) {
        return ElementHelper.areEqual(this, object);
    }

    public int hashCode() {
        return this.getId().hashCode();
    }

    public Object getId() {
        if (null != this.rawEdge.getContext())
            return "(" + this.rawEdge.getSubject() + ", " + this.rawEdge.getPredicate() + ", " + this.rawEdge.getObject() + ") [" + this.rawEdge.getContext() + "]";
        else
            return "(" + this.rawEdge.getSubject() + ", " + this.rawEdge.getPredicate() + ", " + this.rawEdge.getObject() + ")";
    }
}
