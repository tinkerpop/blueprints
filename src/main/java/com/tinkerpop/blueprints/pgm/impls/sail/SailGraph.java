package com.tinkerpop.blueprints.pgm.impls.sail;


import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.sail.util.SailEdgeSequence;
import info.aduna.iteration.CloseableIteration;
import org.apache.log4j.PropertyConfigurator;
import org.openrdf.model.*;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class SailGraph implements Graph {

    private Sail sail;
    private SailConnection sailConnection;

    private static final String LOG4J_PROPERTIES = "log4j.properties";

    private Vertex createVertex(String resource) {
        Literal literal;
        if (SailHelper.isBNode(resource)) {
            return new SailVertex(new BNodeImpl(resource.substring(2)), this.sailConnection);
        } else if ((literal = SailHelper.makeLiteral(resource, this.sailConnection)) != null) {
            return new SailVertex(literal, this.sailConnection);
        } else if (resource.contains(SailTokens.NAMESPACE_SEPARATOR) || resource.contains(SailTokens.FORWARD_SLASH) || resource.contains(SailTokens.POUND)) {
            resource = prefixToNamespace(resource, this.sailConnection);
            return new SailVertex(new URIImpl(resource), this.sailConnection);
        } else {
            throw new RuntimeException(resource + " is not a valid URI, blank node, or literal value");
        }
        //return new SailVertex(NTriplesUtil.parseValue(resource, new ValueFactoryImpl()), this.sailConnection);

    }

    public SailGraph(final Sail sail) {
        try {
            PropertyConfigurator.configure(SailGraph.class.getResource(LOG4J_PROPERTIES));
        } catch (Exception e) {
        }
        try {
            this.sail = sail;
            this.sail.initialize();
            this.sailConnection = sail.getConnection();
            this.addNamespace(SailTokens.RDF_PREFIX, SailTokens.RDF_NS);
            this.addNamespace(SailTokens.RDFS_PREFIX, SailTokens.RDFS_NS);
            this.addNamespace(SailTokens.OWL_PREFIX, SailTokens.OWL_NS);
            this.addNamespace(SailTokens.XSD_PREFIX, SailTokens.XSD_NS);
            this.addNamespace(SailTokens.FOAF_PREFIX, SailTokens.FOAF_NS);
        } catch (SailException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Sail getRawGraph() {
        return this.sail;
    }

    public Vertex addVertex(Object id) {
        if (null == id)
            id = SailTokens.URN_UUID_PREFIX + UUID.randomUUID().toString();

        return createVertex(id.toString());
    }

    public Vertex getVertex(final Object id) {
        return createVertex(id.toString());
    }

    public Edge getEdge(final Object id) {
        throw new UnsupportedOperationException();
    }

    public Iterable<Vertex> getVertices() {
        throw new UnsupportedOperationException();
    }

    public Iterable<Edge> getEdges() {
        try {
            return new SailEdgeSequence(this.sailConnection.getStatements(null, null, null, false), this.sailConnection);
        } catch (SailException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void removeVertex(final Vertex vertex) {
        Value vertexValue = ((SailVertex) vertex).getRawVertex();
        try {
            if (vertexValue instanceof Resource) {
                this.sailConnection.removeStatements((Resource) vertexValue, null, null);
            }
            this.sailConnection.removeStatements(null, null, vertexValue);
        } catch (SailException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        try {
            Value outVertexValue = ((SailVertex) outVertex).getRawVertex();
            Value inVertexValue = ((SailVertex) inVertex).getRawVertex();

            if (!(outVertexValue instanceof Resource)) {
                throw new RuntimeException(outVertex.toString() + " is not a legal URI or blank node");
            }

            URI labelURI = new URIImpl(prefixToNamespace(label, this.sailConnection));
            Statement statement = new StatementImpl((Resource) outVertexValue, labelURI, inVertexValue);
            SailHelper.addStatement(statement, this.sailConnection);
            this.sailConnection.commit();
            return new SailEdge(statement, this.sailConnection);
        } catch (SailException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void removeEdge(final Edge edge) {
        Statement statement = ((SailEdge) edge).getRawEdge();
        try {
            SailHelper.removeStatement(statement, this.sailConnection);
            this.sailConnection.commit();
        } catch (SailException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public SailConnection getSailConnection() {
        return this.sailConnection;
    }

    public Sail getSail() {
        return this.sail;
    }

    public void addNamespace(final String prefix, final String namespace) {
        try {
            this.sailConnection.setNamespace(prefix, namespace);
            this.sailConnection.commit();
        } catch (SailException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void removeNamespace(final String prefix) {
        try {
            this.sailConnection.removeNamespace(prefix);
            this.sailConnection.commit();
        } catch (SailException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Map<String, String> getNamespaces() {
        Map<String, String> namespaces = new HashMap<String, String>();
        try {
            CloseableIteration<? extends Namespace, SailException> results = this.sailConnection.getNamespaces();
            while (results.hasNext()) {
                Namespace namespace = results.next();
                namespaces.put(namespace.getPrefix(), namespace.getName());
            }
            results.close();
        } catch (SailException e) {
            throw new RuntimeException(e.getMessage());
        }
        return namespaces;
    }

    public String expandPrefix(final String uri) {
        return SailGraph.prefixToNamespace(uri, this.sailConnection);
    }

    public String prefixNamespace(final String uri) {
        return SailGraph.namespaceToPrefix(uri, this.sailConnection);
    }

    public Index getIndex() {
        throw new UnsupportedOperationException();
    }

    public void loadRDF(final InputStream input, final String baseURI, final String format, final String baseGraph) {
        Repository repository = new SailRepository(this.getSail());
        try {

            RepositoryConnection connection = repository.getConnection();
            if (null != baseGraph)
                connection.add(input, baseURI, SailTokens.getFormat(format), new URIImpl(baseGraph));
            else
                connection.add(input, baseURI, SailTokens.getFormat(format));

            connection.commit();
            connection.close();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void clear() {
        try {
            this.sailConnection.clear();
            this.sailConnection.commit();
        } catch (SailException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void shutdown() {
        try {
            this.sailConnection.commit();
            this.sailConnection.close();
            this.sail.shutDown();
        } catch (SailException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static String prefixToNamespace(String uri, final SailConnection sailConnection) {
        try {
            if (uri.contains(SailTokens.NAMESPACE_SEPARATOR)) {
                String namespace = sailConnection.getNamespace(uri.substring(0, uri.indexOf(SailTokens.NAMESPACE_SEPARATOR)));
                if (null != namespace)
                    uri = namespace + uri.substring(uri.indexOf(SailTokens.NAMESPACE_SEPARATOR) + 1);
            }
        } catch (SailException e) {
            throw new RuntimeException(e.getMessage());
        }
        return uri;
    }

    public static String namespaceToPrefix(String uri, final SailConnection sailConnection) {

        try {
            CloseableIteration<? extends Namespace, SailException> namespaces = sailConnection.getNamespaces();
            while (namespaces.hasNext()) {
                Namespace namespace = namespaces.next();
                if (uri.contains(namespace.getName()))
                    uri = uri.replace(namespace.getName(), namespace.getPrefix() + SailTokens.NAMESPACE_SEPARATOR);
            }
            namespaces.close();
        } catch (SailException e) {
            throw new RuntimeException(e.getMessage());
        }
        return uri;
    }


    public String toString() {
        String type = this.sail.getClass().getSimpleName().toLowerCase();
        return "sailgraph[" + type + "]";
    }
}
