package com.tinkerpop.blueprints.pgm.impls.sail;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
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

    private static final String NAMED_GRAPH_PROPERTY = "RDF graph edges can only have named graph (ng) properties";

    public SailEdge(final Statement rawEdge, final SailGraph graph) {
        this.rawEdge = rawEdge;
        this.graph = graph;
    }

    public Statement getRawEdge() {
        return this.rawEdge;
    }

    public String getLabel() {
        return this.rawEdge.getPredicate().stringValue();
    }

    public Set<String> getPropertyKeys() {
        Set<String> keys = new HashSet<String>();
        if (null != this.rawEdge.getContext())
            keys.add(SailTokens.NAMED_GRAPH);
        return keys;
    }

    public Object getProperty(final String key) {
        if (key.equals(SailTokens.NAMED_GRAPH))
            return this.rawEdge.getContext().stringValue();
        else
            return null;
    }

    public void setProperty(final String key, final Object value) {
        if (key.equals(SailTokens.NAMED_GRAPH)) {
            try {
                URI namedGraph = new URIImpl(this.graph.expandPrefix(value.toString()));
                SailHelper.removeStatement(this.rawEdge, this.graph.getSailConnection().get());
                this.rawEdge = new ContextStatementImpl(this.rawEdge.getSubject(), this.rawEdge.getPredicate(), this.rawEdge.getObject(), namedGraph);
                SailHelper.addStatement(this.rawEdge, this.graph.getSailConnection().get());
                this.graph.autoStopTransaction(TransactionalGraph.Conclusion.SUCCESS);
            } catch (Exception e) {
                this.graph.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
                throw new RuntimeException(e.getMessage(), e);
            }
        } else {
            throw new RuntimeException(NAMED_GRAPH_PROPERTY);
        }
    }

    public Object removeProperty(final String key) {
        if (key.equals(SailTokens.NAMED_GRAPH)) {
            try {
                Resource ng = this.rawEdge.getContext();
                SailHelper.removeStatement(this.rawEdge, this.graph.getSailConnection().get());
                this.rawEdge = new StatementImpl(this.rawEdge.getSubject(), this.rawEdge.getPredicate(), this.rawEdge.getObject());
                SailHelper.addStatement(this.rawEdge, this.graph.getSailConnection().get());
                this.graph.autoStopTransaction(TransactionalGraph.Conclusion.SUCCESS);
                return ng;
            } catch (Exception e) {
                this.graph.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
                throw new RuntimeException(e.getMessage(), e);
            }
        } else {
            throw new RuntimeException(NAMED_GRAPH_PROPERTY);
        }
    }

    public Vertex getInVertex() {
        return new SailVertex(this.rawEdge.getObject(), this.graph);
    }

    public Vertex getOutVertex() {
        return new SailVertex(this.rawEdge.getSubject(), this.graph);
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

    public boolean equals(final Object object) {
        return object instanceof SailEdge && ((SailEdge) object).getId().equals(this.getId());
    }

    public int hashCode() {
        return this.getId().hashCode();
    }

    public Object getId() {
        //return this.statement.hashCode();
        if (null != this.rawEdge.getContext())
            return "(" + this.rawEdge.getSubject() + ", " + this.rawEdge.getPredicate() + ", " + this.rawEdge.getObject() + ") [" + this.rawEdge.getContext() + "]";
        else
            return "(" + this.rawEdge.getSubject() + ", " + this.rawEdge.getPredicate() + ", " + this.rawEdge.getObject() + ")";
    }
}
