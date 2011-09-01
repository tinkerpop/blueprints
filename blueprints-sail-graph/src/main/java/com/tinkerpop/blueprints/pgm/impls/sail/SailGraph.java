package com.tinkerpop.blueprints.pgm.impls.sail;


import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.sail.util.SailEdgeSequence;
import info.aduna.iteration.CloseableIteration;
import net.fortytwo.sesametools.nquads.NQuadsFormat;
import org.apache.log4j.PropertyConfigurator;
import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.impl.MapBindingSet;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.sparql.SPARQLParser;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A Blueprints implementation of the RDF-based Sail interfaces by Aduna (http://openrdf.org).
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Joshua Shinavier (http://fortytwo.net)
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
        formats.put("n-quads", NQuadsFormat.NQUADS);
    }

    public static RDFFormat getFormat(final String format) {
        RDFFormat ret = formats.get(format);
        if (null == ret)
            throw new RuntimeException(format + " is an unsupported RDF file format. Use rdf-xml, n-triples, n-quads, turtle, n3, trix, or trig");
        else
            return ret;
    }

    private final List<SailConnection> connections = new LinkedList<SailConnection>();

    protected final Sail rawGraph;

    protected final ThreadLocal<SailConnection> sailConnection = new ThreadLocal<SailConnection>() {
        protected SailConnection initialValue() {
            SailConnection sc = null;
            try {
                sc = createConnection();
            } catch (SailException e) {
                e.printStackTrace(System.err);
            }
            return sc;
        }
    };

    private final ThreadLocal<Boolean> inTransaction = new ThreadLocal<Boolean>() {
        protected Boolean initialValue() {
            return Boolean.FALSE;
        }
    };
    private final ThreadLocal<Integer> txBuffer = new ThreadLocal<Integer>() {
        protected Integer initialValue() {
            return 1;
        }
    };
    private final ThreadLocal<Integer> txCounter = new ThreadLocal<Integer>() {
        protected Integer initialValue() {
            return 0;
        }
    };

    private static final String LOG4J_PROPERTIES = "log4j.properties";

    /**
     * Construct a new SailGraph with an uninitialized Sail.
     *
     * @param sail a not-yet-initialized Sail instance
     */
    public SailGraph(final Sail sail) {
        try {
            PropertyConfigurator.configure(SailGraph.class.getResource(LOG4J_PROPERTIES));
        } catch (Exception e) {
            // TODO: uncomment this?
            //e.printStackTrace(System.err);
        }
        try {
            this.rawGraph = sail;
            sail.initialize();
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
        } else if ((literal = SailHelper.makeLiteral(resource, this)) != null) {
            return new SailVertex(literal, this);
        } else if (resource.contains(SailTokens.NAMESPACE_SEPARATOR) || resource.contains(SailTokens.FORWARD_SLASH) || resource.contains(SailTokens.POUND)) {
            resource = this.expandPrefix(resource);
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
        if (null == id)
            return null;
        return createVertex(id.toString());
    }

    public Edge getEdge(final Object id) {
        throw new UnsupportedOperationException();
    }

    public Iterable<Vertex> getVertices() {
        throw new UnsupportedOperationException("RDF is an edge based graph model");
    }

    public Iterable<Edge> getEdges() {
        try {
            return new SailEdgeSequence(this.sailConnection.get().getStatements(null, null, null, false), this);
        } catch (SailException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void removeVertex(final Vertex vertex) {
        Value vertexValue = ((SailVertex) vertex).getRawVertex();
        try {
            if (vertexValue instanceof Resource) {
                this.sailConnection.get().removeStatements((Resource) vertexValue, null, null);
            }
            this.sailConnection.get().removeStatements(null, null, vertexValue);
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

        URI labelURI = new URIImpl(this.expandPrefix(label));
        Statement statement = new StatementImpl((Resource) outVertexValue, labelURI, inVertexValue);
        SailHelper.addStatement(statement, this.sailConnection.get());
        this.autoStopTransaction(Conclusion.SUCCESS);
        return new SailEdge(statement, this);
    }

    public void removeEdge(final Edge edge) {
        Statement statement = ((SailEdge) edge).getRawEdge();
        SailHelper.removeStatement(statement, this.sailConnection.get());
        this.autoStopTransaction(Conclusion.SUCCESS);
    }

    /**
     * Get the Sail connection currently being used by the graph.
     *
     * @return the Sail connection
     */
    public ThreadLocal<SailConnection> getSailConnection() {
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
            this.sailConnection.get().setNamespace(prefix, namespace);
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
            this.sailConnection.get().removeNamespace(prefix);
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
            CloseableIteration<? extends Namespace, SailException> results = this.sailConnection.get().getNamespaces();
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
    }

    public void clear() {
        try {
            this.sailConnection.get().clear();
            this.autoStopTransaction(Conclusion.SUCCESS);
        } catch (SailException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private synchronized SailConnection createConnection() throws SailException {
        cleanupConnections();

        SailConnection sc = rawGraph.getConnection();

        connections.add(sc);

        return sc;
    }

    private void cleanupConnections() throws SailException {
        Collection<SailConnection> toRemove = new LinkedList<SailConnection>();

        for (SailConnection sc : connections) {
            if (!sc.isOpen()) {
                toRemove.add(sc);
            }
        }

        for (SailConnection sc : toRemove) {
            connections.remove(sc);
        }
    }

    private void closeAllConnections() throws SailException {
        for (SailConnection sc : connections) {
            if (null != sc) {
                if (sc.isOpen()) {
                    sc.rollback();
                    sc.close();
                }
            }
        }
    }

    public synchronized void shutdown() {
        try {
            closeAllConnections();
            this.rawGraph.shutDown();
        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Given a URI, expand it to its full URI.
     *
     * @param uri the compressed URI (e.g. tg:knows)
     * @return the expanded URI (e.g. http://tinkerpop.com#knows)
     */
    public String expandPrefix(String uri) {
        try {
            if (uri.contains(SailTokens.NAMESPACE_SEPARATOR)) {
                String namespace = this.sailConnection.get().getNamespace(uri.substring(0, uri.indexOf(SailTokens.NAMESPACE_SEPARATOR)));
                if (null != namespace)
                    uri = namespace + uri.substring(uri.indexOf(SailTokens.NAMESPACE_SEPARATOR) + 1);
            }
        } catch (SailException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return uri;
    }

    /**
     * Given a URI, compress it to its prefixed URI.
     *
     * @param uri the expanded URI (e.g. http://tinkerpop.com#knows)
     * @return the prefixed URI (e.g. tg:knows)
     */
    public String prefixNamespace(String uri) {
        try {
            CloseableIteration<? extends Namespace, SailException> namespaces = this.sailConnection.get().getNamespaces();
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
        if (inTransaction.get())
            throw new RuntimeException(TransactionalGraph.NESTED_MESSAGE);
        inTransaction.set(Boolean.TRUE);
        this.txCounter.set(0);
    }

    public void stopTransaction(final Conclusion conclusion) {
        try {
            inTransaction.set(Boolean.FALSE);
            if (Conclusion.SUCCESS == conclusion) {
                this.sailConnection.get().commit();
            } else {
                this.sailConnection.get().rollback();
            }
            this.txCounter.set(0);
        } catch (SailException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected void autoStopTransaction(final Conclusion conclusion) {
        if (txBuffer.get() > 0) {
            try {
                txCounter.set(txCounter.get() + 1);
                if (conclusion == Conclusion.FAILURE) {
                    this.sailConnection.get().commit();
                    txCounter.set(0);
                    inTransaction.set(Boolean.FALSE);
                } else if (this.txBuffer.get() == 0 || (this.txCounter.get() % this.txBuffer.get() == 0)) {
                    this.sailConnection.get().commit();
                    txCounter.set(0);
                    inTransaction.set(Boolean.FALSE);
                }
            } catch (SailException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    public void setMaxBufferSize(final int bufferSize) {
        try {
            this.sailConnection.get().commit();
        } catch (SailException e) {
        }
        inTransaction.set(Boolean.FALSE);
        this.txBuffer.set(bufferSize);
    }

    public int getMaxBufferSize() {
        return this.txBuffer.get();
    }

    public int getCurrentBufferSize() {
        return this.txCounter.get();
    }


    public String toString() {
        String type = this.rawGraph.getClass().getSimpleName().toLowerCase();
        return "sailgraph[" + type + "]";
    }

    private String getPrefixes() {
        String prefixString = "";
        final Map<String, String> namespaces = this.getNamespaces();
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
            final CloseableIteration<? extends BindingSet, QueryEvaluationException> results = this.sailConnection.get().evaluate(query.getTupleExpr(), query.getDataset(), new MapBindingSet(), includeInferred);
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
