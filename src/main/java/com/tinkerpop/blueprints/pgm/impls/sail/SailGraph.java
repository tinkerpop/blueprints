package com.tinkerpop.blueprints.pgm.impls.sail;


import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.sail.util.SailEdgeSequence;
import info.aduna.iteration.CloseableIteration;
import org.apache.log4j.PropertyConfigurator;
import org.openrdf.model.*;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.impl.MapBindingSet;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.sparql.SPARQLParser;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import java.io.InputStream;
import java.util.*;

/**
 * A Blueprints implementation of the RDF-based Sail interfaces by Aduna (http://openrdf.org).
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class SailGraph implements TransactionalGraph {

    protected Sail rawGraph;
    protected SailConnection sailConnection;
    protected boolean inTransaction;
    private Mode mode = Mode.AUTOMATIC;
    private static final String LOG4J_PROPERTIES = "log4j.properties";

    public SailGraph(final Sail rawGraph) {
        this.startSail(rawGraph);
    }

    public SailGraph() {

    }

    protected void startSail(final Sail sail) {
        try {
            PropertyConfigurator.configure(SailGraph.class.getResource(LOG4J_PROPERTIES));
        } catch (Exception e) {
        }
        try {
            this.rawGraph = sail;
            //this.rawGraph.initialize();
            this.sailConnection = sail.getConnection();
            this.addNamespace(SailTokens.RDF_PREFIX, SailTokens.RDF_NS);
            this.addNamespace(SailTokens.RDFS_PREFIX, SailTokens.RDFS_NS);
            this.addNamespace(SailTokens.OWL_PREFIX, SailTokens.OWL_NS);
            this.addNamespace(SailTokens.XSD_PREFIX, SailTokens.XSD_NS);
            this.addNamespace(SailTokens.FOAF_PREFIX, SailTokens.FOAF_NS);
        } catch (SailException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Sail getRawGraph() {
        return this.rawGraph;
    }

    private Vertex createVertex(String resource) {
        Literal literal;
        if (SailHelper.isBNode(resource)) {
            return new SailVertex(new BNodeImpl(resource.substring(2)), this);
        } else if ((literal = SailHelper.makeLiteral(resource, this.sailConnection)) != null) {
            return new SailVertex(literal, this);
        } else if (resource.contains(SailTokens.NAMESPACE_SEPARATOR) || resource.contains(SailTokens.FORWARD_SLASH) || resource.contains(SailTokens.POUND)) {
            resource = prefixToNamespace(resource, this.sailConnection);
            return new SailVertex(new URIImpl(resource), this);
        } else {
            throw new RuntimeException(resource + " is not a valid URI, blank node, or literal value");
        }
        //return new SailVertex(NTriplesUtil.parseValue(resource, new ValueFactoryImpl()), this.sailConnection);

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
            return new SailEdgeSequence(this.sailConnection.getStatements(null, null, null, false), this);
        } catch (SailException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void removeVertex(final Vertex vertex) {
        Value vertexValue = ((SailVertex) vertex).getRawVertex();
        try {
            if (vertexValue instanceof Resource) {
                this.sailConnection.removeStatements((Resource) vertexValue, null, null);
            }
            this.sailConnection.removeStatements(null, null, vertexValue);
            this.autoStopTransaction(Conclusion.SUCCESS);
        } catch (SailException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        Value outVertexValue = ((SailVertex) outVertex).getRawVertex();
        Value inVertexValue = ((SailVertex) inVertex).getRawVertex();

        if (!(outVertexValue instanceof Resource)) {
            throw new RuntimeException(outVertex.toString() + " is not a legal URI or blank node");
        }

        URI labelURI = new URIImpl(prefixToNamespace(label, this.sailConnection));
        Statement statement = new StatementImpl((Resource) outVertexValue, labelURI, inVertexValue);
        SailHelper.addStatement(statement, this.sailConnection);
        this.autoStopTransaction(Conclusion.SUCCESS);
        return new SailEdge(statement, this);
    }

    public void removeEdge(final Edge edge) {
        Statement statement = ((SailEdge) edge).getRawEdge();
        SailHelper.removeStatement(statement, this.sailConnection);
        this.autoStopTransaction(Conclusion.SUCCESS);
    }

    public SailConnection getSailConnection() {
        return this.sailConnection;
    }


    public void addNamespace(final String prefix, final String namespace) {
        try {
            this.sailConnection.setNamespace(prefix, namespace);
            this.sailConnection.commit();
        } catch (SailException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void removeNamespace(final String prefix) {
        try {
            this.sailConnection.removeNamespace(prefix);
            this.sailConnection.commit();
        } catch (SailException e) {
            throw new RuntimeException(e.getMessage(), e);
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
            throw new RuntimeException(e.getMessage(), e);
        }
        return namespaces;
    }

    public String expandPrefix(final String uri) {
        return SailGraph.prefixToNamespace(uri, this.sailConnection);
    }

    public String prefixNamespace(final String uri) {
        return SailGraph.namespaceToPrefix(uri, this.sailConnection);
    }

    public void loadRDF(final InputStream input, final String baseURI, final String format, final String baseGraph) {
        Repository repository = new SailRepository(this.rawGraph);
        try {

            RepositoryConnection connection = repository.getConnection();
            if (null != baseGraph)
                connection.add(input, baseURI, SailTokens.getFormat(format), new URIImpl(baseGraph));
            else
                connection.add(input, baseURI, SailTokens.getFormat(format));

            connection.commit();
            connection.close();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void clear() {
        try {
            this.sailConnection.clear();
            this.sailConnection.commit();
        } catch (SailException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void shutdown() {
        try {
            this.sailConnection.commit();
            this.sailConnection.close();
            this.rawGraph.shutDown();
        } catch (SailException e) {
            throw new RuntimeException(e.getMessage(), e);
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
            throw new RuntimeException(e.getMessage(), e);
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
            throw new RuntimeException(e.getMessage(), e);
        }
        return uri;
    }

    public void startTransaction() {
        if (Mode.AUTOMATIC == this.mode)
            throw new RuntimeException(TransactionalGraph.TURN_OFF_MESSAGE);
        if (this.inTransaction)
            throw new RuntimeException(TransactionalGraph.NESTED_MESSAGE);
        this.inTransaction = true;
    }

    public void stopTransaction(final Conclusion conclusion) {
        if (Mode.AUTOMATIC == this.mode)
            throw new RuntimeException(TransactionalGraph.TURN_OFF_MESSAGE);

        try {
            this.inTransaction = false;
            if (Conclusion.SUCCESS == conclusion) {
                this.sailConnection.commit();
            } else {
                this.sailConnection.rollback();
            }
        } catch (SailException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected void autoStopTransaction(Conclusion conclusion) {
        if (this.mode == Mode.AUTOMATIC) {
            try {
                this.inTransaction = false;
                if (conclusion == Conclusion.SUCCESS)
                    this.sailConnection.commit();
                else
                    this.sailConnection.rollback();
            } catch (SailException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    public void setTransactionMode(Mode mode) {
        try {
            this.sailConnection.commit();
        } catch (SailException e) {
        }
        this.inTransaction = false;
        this.mode = mode;
    }

    public Mode getTransactionMode() {
        return this.mode;
    }


    public String toString() {
        String type = this.rawGraph.getClass().getSimpleName().toLowerCase();
        return "sailgraph[" + type + "]";
    }

    private String getPrefixes() {
        String prefixString = "";
        Map<String, String> namespaces = this.getNamespaces();
        for (Map.Entry<String, String> entry : namespaces.entrySet()) {
            prefixString = prefixString + SailTokens.PREFIX_SPACE + entry.getKey() + SailTokens.COLON_LESSTHAN + entry.getValue() + SailTokens.GREATERTHAN_NEWLINE;
        }
        return prefixString;
    }

    public List<Map<String, Vertex>> executeSparql(String sparqlQuery) throws RuntimeException {
        try {
            sparqlQuery = getPrefixes() + sparqlQuery;
            final SPARQLParser parser = new SPARQLParser();
            final ParsedQuery query = parser.parseQuery(sparqlQuery, null);
            boolean includeInferred = false;
            final CloseableIteration<? extends BindingSet, QueryEvaluationException> results = this.sailConnection.evaluate(query.getTupleExpr(), query.getDataset(), new MapBindingSet(), includeInferred);
            final List<Map<String, Vertex>> returnList = new ArrayList<Map<String, Vertex>>();
            try {
                while (results.hasNext()) {
                    BindingSet bs = results.next();
                    Map<String, Vertex> returnMap = new HashMap<String, Vertex>();
                    for (Binding b : bs) {
                        returnMap.put(b.getName(), this.getVertex(b.getValue().toString()));
                    }
                    returnList.add(returnMap);
                }
            } finally {
                results.close();
            }
            return returnList;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


}
