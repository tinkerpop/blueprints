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
import org.openrdf.rio.*;
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

    public static final Map<String, RDFFormat> formats = new HashMap<String, RDFFormat>();

    static {
        formats.put("rdf-xml", RDFFormat.RDFXML);
        formats.put("n-triples", RDFFormat.NTRIPLES);
        formats.put("turtle", RDFFormat.TURTLE);
        formats.put("n3", RDFFormat.N3);
        formats.put("trix", RDFFormat.TRIX);
        formats.put("trig", RDFFormat.TRIG);
    }

    public static RDFFormat getFormat(final String format) {
        RDFFormat ret = formats.get(format);
        if (null == ret)
            throw new RuntimeException(format + " is an unsupported RDF file format. Use rdf-xml, n-triples, turtle, n3, trix, or trig");
        else
            return ret;
    }

    protected Sail rawGraph;
    protected SailConnection sailConnection;
    protected boolean inTransaction;
    private final ThreadLocal<Mode> txMode = new ThreadLocal<Mode>() {
        protected Mode initialValue() {
            return Mode.AUTOMATIC;
        }
    };
    private static final String LOG4J_PROPERTIES = "log4j.properties";

    /**
     * Construct a new SailGraph with an uninitialized Sail.
     *
     * @param rawGraph a not-yet-initialized Sail instance
     */
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
            sail.initialize();
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

    /**
     * Get the Sail connection currently being used by the graph.
     *
     * @return the Sail connection
     */
    public SailConnection getSailConnection() {
        return this.sailConnection;
    }

    /**
     * Add a prefix-to-namespace mapping to the Sail connection of this graph.
     *
     * @param prefix    the prefix (e.g. tg)
     * @param namespace the namespace (e.g. http://tinkerpop.com#)
     */
    public void addNamespace(final String prefix, final String namespace) {
        try {
            this.sailConnection.setNamespace(prefix, namespace);
            this.autoStopTransaction(TransactionalGraph.Conclusion.SUCCESS);
        } catch (SailException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Remove a prefix-to-namespace mapping from the Sail connection of this graph.
     *
     * @param prefix the prefix of the prefix-to-namespace mapping to remove
     */
    public void removeNamespace(final String prefix) {
        try {
            this.sailConnection.removeNamespace(prefix);
            this.autoStopTransaction(TransactionalGraph.Conclusion.SUCCESS);
        } catch (SailException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Get all the prefix-to-namespace mappings of the graph.
     *
     * @return a map of the prefix-to-namespace mappings
     */
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

    /**
     * Given a URI, expand it to its full URI.
     *
     * @param uri the compressed URI (e.g. tg:knows)
     * @return the expanded URI (e.g. http://tinkerpop.com#knows)
     */
    public String expandPrefix(final String uri) {
        return SailGraph.prefixToNamespace(uri, this.sailConnection);
    }

    /**
     * Given a URI, compress it to its prefixed URI.
     *
     * @param uri the expanded URI (e.g. http://tinkerpop.com#knows)
     * @return the compressed URI (e.g. tg:knows)
     */
    public String prefixNamespace(final String uri) {
        return SailGraph.namespaceToPrefix(uri, this.sailConnection);
    }

    /**
     * Load RDF data into the SailGraph. Supported formats include rdf-xml, n-triples, turtle, n3, trix, or trig.
     *
     * @param input     the InputStream of RDF data
     * @param baseURI   the baseURI for RDF data
     * @param format    supported formats include rdf-xml, n-triples, turtle, n3, trix, or trig
     * @param baseGraph the baseGraph to insert the data into
     */
    public void loadRDF(final InputStream input, final String baseURI, final String format, final String baseGraph) {
        try {
            final SailConnection c = this.rawGraph.getConnection();
            try {
                RDFParser p = Rio.createParser(getFormat(format));
                RDFHandler h = null == baseGraph
                        ? new SailAdder(c)
                        : new SailAdder(c, new URIImpl(baseGraph));
                p.setRDFHandler(h);
                p.parse(input, baseURI);
                c.commit();
            } finally {
                c.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        /*
        Repository repository = new SailRepository(this.rawGraph);
        try {

            RepositoryConnection connection = repository.getConnection();
            if (null != baseGraph)
                connection.add(input, baseURI, getFormat(format), new URIImpl(baseGraph));
            else
                connection.add(input, baseURI, getFormat(format));

            connection.commit();
            connection.close();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }  */
    }

    public void clear() {
        try {
            this.sailConnection.clear();
            this.autoStopTransaction(Conclusion.SUCCESS);
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

    protected static String prefixToNamespace(String uri, final SailConnection sailConnection) {
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

    protected static String namespaceToPrefix(String uri, final SailConnection sailConnection) {
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
        if (Mode.AUTOMATIC == txMode.get())
            throw new RuntimeException(TransactionalGraph.TURN_OFF_MESSAGE);
        if (this.inTransaction)
            throw new RuntimeException(TransactionalGraph.NESTED_MESSAGE);
        this.inTransaction = true;
    }

    public void stopTransaction(final Conclusion conclusion) {
        if (Mode.AUTOMATIC == txMode.get())
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
        if (txMode.get() == Mode.AUTOMATIC) {
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
        txMode.set(mode);
    }

    public Mode getTransactionMode() {
        return txMode.get();
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

    /**
     * Evaluate a SPARQL query against the SailGraph (http://www.w3.org/TR/rdf-sparql-query/). The result is a mapping between the ?-bindings and the bound URI, blank node, or literal represented as a Vertex.
     *
     * @param sparqlQuery the SPARQL query to evaluate
     * @return the mapping between a ?-binding and the URI, blank node, or literal as a Vertex
     * @throws RuntimeException if an error occurs in the SPARQL query engine
     */
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

    private class SailAdder implements RDFHandler {
        private final SailConnection c;
        private final Resource[] contexts;

        public SailAdder(final SailConnection c,
                         final Resource... contexts) {
            this.c = c;
            this.contexts = contexts;
        }

        public void startRDF() throws RDFHandlerException {
        }

        public void endRDF() throws RDFHandlerException {
        }

        public void handleNamespace(final String prefix,
                                    final String uri) throws RDFHandlerException {
            try {
                c.setNamespace(prefix, uri);
            } catch (SailException e) {
                throw new RDFHandlerException(e);
            }
        }

        public void handleStatement(final Statement s) throws RDFHandlerException {
            try {
                if (1 <= contexts.length) {
                    for (Resource x : contexts) {
                        c.addStatement(s.getSubject(), s.getPredicate(), s.getObject(), x);
                    }
                } else {
                    c.addStatement(s.getSubject(), s.getPredicate(), s.getObject(), s.getContext());
                }
            } catch (SailException e) {
                throw new RDFHandlerException(e);
            }
        }

        public void handleComment(String s) throws RDFHandlerException {
        }
    }
}
