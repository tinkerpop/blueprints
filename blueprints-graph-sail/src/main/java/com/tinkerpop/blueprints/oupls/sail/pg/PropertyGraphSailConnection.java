package com.tinkerpop.blueprints.oupls.sail.pg;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import info.aduna.iteration.CloseableIteration;
import net.fortytwo.sesametools.SailConnectionTripleSource;
import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.UpdateExpr;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
class PropertyGraphSailConnection implements SailConnection {
    private static final Map<String, Namespace> namespaces = new HashMap<String, Namespace>();

    static {
        addNamespace("prop", PropertyGraphSail.PROPERTY_NS);
        addNamespace("pgm", PropertyGraphSail.ONTOLOGY_NS);
        addNamespace("vertex", PropertyGraphSail.VERTEX_NS);
        addNamespace("edge", PropertyGraphSail.EDGE_NS);
        addNamespace("rdf", RDF.NAMESPACE);
    }

    private final PropertyGraphSail.PropertyGraphContext context;

    private boolean open = true;

    private final VertexGenerator allVertexStatements;
    private final VertexGenerator vertexIds;
    private final VertexGenerator vertexProps;
    private final VertexGenerator vertexTypes;
    private final EdgeGenerator allEdgeStatements;
    private final EdgeGenerator labels;
    private final EdgeGenerator heads;
    private final EdgeGenerator tails;
    private final EdgeGenerator edgeTypes;
    private final EdgeGenerator edgeIds;
    private final EdgeGenerator edgeProps;

    public PropertyGraphSailConnection(final PropertyGraphSail.PropertyGraphContext context) {
        this.context = context;

        allVertexStatements = new VertexGenerator();
        allVertexStatements.setDoId(true);
        allVertexStatements.setDoProperties(true);
        allVertexStatements.setDoType(true);

        allEdgeStatements = new EdgeGenerator();
        allEdgeStatements.setDoId(true);
        allEdgeStatements.setDoProperties(true);
        allEdgeStatements.setDoType(true);
        allEdgeStatements.setDoHead(true);
        allEdgeStatements.setDoTail(true);
        allEdgeStatements.setDoLabel(true);

        vertexTypes = new VertexGenerator();
        vertexTypes.setDoType(true);

        edgeTypes = new EdgeGenerator();
        edgeTypes.setDoType(true);

        labels = new EdgeGenerator();
        labels.setDoLabel(true);

        heads = new EdgeGenerator();
        heads.setDoHead(true);

        tails = new EdgeGenerator();
        tails.setDoTail(true);

        vertexIds = new VertexGenerator();
        vertexIds.setDoId(true);

        edgeIds = new EdgeGenerator();
        edgeIds.setDoId(true);

        vertexProps = new VertexGenerator();
        vertexProps.setDoProperties(true);

        edgeProps = new EdgeGenerator();
        edgeProps.setDoProperties(true);
    }

    private static void addNamespace(final String prefix,
                                     final String uri) {
        Namespace n = new NamespaceImpl(prefix, uri);
        namespaces.put(prefix, n);
    }

    public boolean isOpen() throws SailException {
        return open;
    }

    public void close() throws SailException {
        open = false;
    }

    public CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluate(final TupleExpr query,
                                                                                       final Dataset dataset,
                                                                                       final BindingSet bindings,
                                                                                       final boolean includeInferred) throws SailException {
        try {
            TripleSource tripleSource = new SailConnectionTripleSource(this, context.valueFactory, includeInferred);
            EvaluationStrategyImpl strategy = new EvaluationStrategyImpl(tripleSource, dataset);
            return strategy.evaluate(query, bindings);
        } catch (QueryEvaluationException e) {
            throw new SailException(e);
        }
    }

    public void executeUpdate(UpdateExpr updateExpr, Dataset dataset, BindingSet bindingSet, boolean b) throws SailException {
        // Do nothing.
    }

    public CloseableIteration<? extends Resource, SailException> getContextIDs() throws SailException {
        throw new UnsupportedOperationException();
    }

    private boolean matchesNullContext(final Resource... contexts) {
        if (0 == contexts.length) {
            return true;
        } else {
            for (Resource c : contexts) {
                if (null == c) {
                    return true;
                }
            }

            return false;
        }
    }

    public CloseableIteration<? extends Statement, SailException> getStatements(final Resource subject,
                                                                                final URI predicate,
                                                                                final Value object,
                                                                                final boolean includeInferred,
                                                                                final Resource... contexts) throws SailException {
        // Statements exist only in the default context.
        if (!matchesNullContext(contexts)) {
            return new StatementIteration();
        }

        // TODO: elements embedded in URIs

        if (null == subject) {
            if (null == object) {
                if (null == predicate) {  // ? ? ?
                    return getStatements_xxx();
                } else {  // ? p ?
                    return getStatements_xPx(predicate);
                }
            } else {
                if (null == predicate) {  // ? ? o
                    return getStatements_xxO(object);
                } else {  // ? p o
                    return getStatements_xPO(predicate, object);
                }
            }
        } else {
            if (null == object) {
                if (null == predicate) {  // s ? ?
                    return getStatements_Sxx(subject);
                } else {  // s p ?
                    return getStatements_SPx(subject, predicate);
                }
            } else {
                if (null == predicate) {  // s ? o
                    return getStatements_SxO(subject, object);
                } else {  // s p o
                    return getStatements_SPO(subject, predicate, object);
                }
            }
        }
    }

    private CloseableIteration<Statement, SailException> getStatements_xxx() throws SailException {
        Iterator<Edge> edgeIterator = context.graph.getEdges().iterator();
        Iterator<Vertex> vertexIterator = context.graph.getVertices().iterator();
        Source<Edge> edges = new Source<Edge>(edgeIterator, allEdgeStatements);
        Source<Vertex> vertices = new Source<Vertex>(vertexIterator, allVertexStatements);
        return new StatementIteration(vertices, edges);
    }

    private CloseableIteration<Statement, SailException> getStatements_Sxx(final Resource subject) throws SailException {
        if (subject instanceof URI) {
            Vertex v = vertexForURI((URI) subject);
            if (null == v) {
                Edge e = edgeForURI((URI) subject);
                if (null == e) {
                    return new StatementIteration();
                } else {
                    Source<Edge> s = new Source<Edge>(new SingleItemIterator<Edge>(e), allEdgeStatements);
                    return new StatementIteration(s);
                }
            } else {
                Source<Vertex> s = new Source<Vertex>(new SingleItemIterator<Vertex>(v), allVertexStatements);
                return new StatementIteration(s);
            }
        } else {
            return new StatementIteration();
        }
    }

    private CloseableIteration<Statement, SailException> getStatements_SPx(final Resource subject,
                                                                           final URI predicate) throws SailException {
        if (subject instanceof URI) {
            if (predicate.equals(RDF.TYPE)) {
                Vertex v = vertexForURI((URI) subject);
                if (null == v) {
                    Edge e = edgeForURI((URI) subject);
                    if (null == e) {
                        return new StatementIteration();
                    } else {
                        Source<Edge> s = new Source<Edge>(new SingleItemIterator<Edge>(e), edgeTypes);
                        return new StatementIteration(s);
                    }
                } else {
                    Source<Vertex> s = new Source<Vertex>(new SingleItemIterator<Vertex>(v), vertexTypes);
                    return new StatementIteration(s);
                }
            }
            if (predicate.equals(PropertyGraphSail.ID)) {
                Vertex v = vertexForURI((URI) subject);
                if (null == v) {
                    Edge e = edgeForURI((URI) subject);
                    if (null == e) {
                        return new StatementIteration();
                    } else {
                        Source<Edge> s = new Source<Edge>(new SingleItemIterator<Edge>(e), edgeIds);
                        return new StatementIteration(s);
                    }
                } else {
                    Source<Vertex> s = new Source<Vertex>(new SingleItemIterator<Vertex>(v), vertexIds);
                    return new StatementIteration(s);
                }
            } else if (predicate.equals(PropertyGraphSail.LABEL)) {
                Edge e = edgeForURI((URI) subject);
                if (null == e) {
                    return new StatementIteration();
                } else {
                    Source<Edge> s = new Source<Edge>(new SingleItemIterator<Edge>(e), labels);
                    return new StatementIteration(s);
                }
            } else if (predicate.equals(PropertyGraphSail.HEAD)) {
                Edge e = edgeForURI((URI) subject);
                if (null == e) {
                    return new StatementIteration();
                } else {
                    Source<Edge> s = new Source<Edge>(new SingleItemIterator<Edge>(e), heads);
                    return new StatementIteration(s);
                }
            } else if (predicate.equals(PropertyGraphSail.TAIL)) {
                Edge e = edgeForURI((URI) subject);
                if (null == e) {
                    return new StatementIteration();
                } else {
                    Source<Edge> s = new Source<Edge>(new SingleItemIterator<Edge>(e), tails);
                    return new StatementIteration(s);
                }
            } else if (isPropertyPredicate(predicate)) {
                String key = keyFromPredicate(predicate);
                Vertex v = vertexForURI((URI) subject);
                if (null == v) {
                    Edge e = edgeForURI((URI) subject);
                    if (null == e) {
                        return new StatementIteration();
                    } else {
                        Source<Edge> s = new Source<Edge>(new SingleItemIterator<Edge>(e), edgePropertiesWithKey(key, predicate));
                        return new StatementIteration(s);
                    }
                } else {
                    Source<Vertex> s = new Source<Vertex>(new SingleItemIterator<Vertex>(v), vertexPropertiesWithKey(key, predicate));
                    return new StatementIteration(s);
                }
            } else {
                return new StatementIteration();
            }
        } else {
            return new StatementIteration();
        }
    }

    private CloseableIteration<Statement, SailException> getStatements_SxO(final Resource subject,
                                                                           final Value object) throws SailException {
        if (subject instanceof URI) {
            Collection<Source> sources = new LinkedList<Source>();

            Vertex v = vertexForURI((URI) subject);
            Edge e = edgeForURI((URI) subject);
            Object val = literalToObject(object);
            Vertex vObj = object instanceof URI ? vertexForURI((URI) object) : null;

            // id
            if (null != val) {
                if (null == v) {
                    if (null != e) {
                        if (e.getId().equals(val)) {
                            Source<Edge> s = new Source<Edge>(new SingleItemIterator<Edge>(e), edgeIds);
                            sources.add(s);
                        }
                    }
                } else {
                    if (v.getId().equals(val)) {
                        Source<Vertex> s = new Source<Vertex>(new SingleItemIterator<Vertex>(v), vertexIds);
                        sources.add(s);
                    }
                }
            }

            // label
            if (null != val && (val instanceof String)) {
                if (null != e && e.getLabel().equals(val)) {
                    Source<Edge> s = new Source<Edge>(new SingleItemIterator<Edge>(e), labels);
                    sources.add(s);
                }
            }

            // head
            if (null != e && null != vObj && e.getVertex(Direction.IN).equals(vObj)) {
                Source<Edge> s = new Source<Edge>(new SingleItemIterator<Edge>(e), heads);
                sources.add(s);
            }

            // tail
            if (null != e && null != vObj && e.getVertex(Direction.OUT).equals(vObj)) {
                Source<Edge> s = new Source<Edge>(new SingleItemIterator<Edge>(e), tails);
                sources.add(s);
            }

            // type
            if (null != v && object instanceof URI && ((URI) object).equals(PropertyGraphSail.VERTEX)) {
                Source<Vertex> s = new Source<Vertex>(new SingleItemIterator<Vertex>(v), vertexTypes);
                sources.add(s);
            }
            if (null != e && object instanceof URI && ((URI) object).equals(PropertyGraphSail.VERTEX)) {
                Source<Edge> s = new Source<Edge>(new SingleItemIterator<Edge>(e), edgeTypes);
                sources.add(s);
            }

            // properties
            if (null != val) {
                if (null != v) {
                    Source<Vertex> vertices = new Source<Vertex>(
                            new SingleItemIterator<Vertex>(v),
                            vertexPropertiesWithValue(val, (Literal) object));
                    sources.add(vertices);
                }

                if (null != e) {
                    Source<Edge> edges = new Source<Edge>(
                            new SingleItemIterator<Edge>(e),
                            edgePropertiesWithValue(val, (Literal) object));
                    sources.add(edges);
                }
            }

            if (sources.size() > 0) {
                Source[] s = new Source[sources.size()];
                sources.toArray(s);
                return new StatementIteration(s);
            } else {
                return new StatementIteration();
            }
        } else {
            return new StatementIteration();
        }
    }

    private CloseableIteration<Statement, SailException> getStatements_SPO(final Resource subject,
                                                                           final URI predicate,
                                                                           final Value object) throws SailException {
        if (predicate.equals(RDF.TYPE)) {
            if (subject instanceof URI) {
                Vertex v = vertexForURI((URI) subject);
                if (null == v) {
                    Edge e = edgeForURI((URI) subject);
                    if (null == e) {
                        return new StatementIteration();
                    } else {
                        if (object.equals(PropertyGraphSail.EDGE)) {
                            Source<Edge> s = new Source<Edge>(new SingleItemIterator<Edge>(e), edgeTypes);
                            return new StatementIteration(s);
                        } else {
                            return new StatementIteration();
                        }
                    }
                } else {
                    if (object.equals(PropertyGraphSail.VERTEX)) {
                        Source<Vertex> s = new Source<Vertex>(new SingleItemIterator<Vertex>(v), vertexTypes);
                        return new StatementIteration(s);
                    } else {
                        return new StatementIteration();
                    }
                }
            } else {
                return new StatementIteration();
            }
        } else if (predicate.equals(PropertyGraphSail.ID)) {
            Object id = literalToObject(object);
            if (null == id || !(subject instanceof URI)) {
                return new StatementIteration();
            } else {
                Vertex v = vertexForURI((URI) subject);
                if (null == v) {
                    Edge e = edgeForURI((URI) subject);
                    if (null == e) {
                        return new StatementIteration();
                    } else {
                        if (e.getId().equals(id)) {
                            Source<Edge> s = new Source<Edge>(new SingleItemIterator<Edge>(e), edgeIds);
                            return new StatementIteration(s);
                        } else {
                            return new StatementIteration();
                        }
                    }
                } else {
                    if (v.getId().equals(id)) {
                        Source<Vertex> s = new Source<Vertex>(new SingleItemIterator<Vertex>(v), vertexIds);
                        return new StatementIteration(s);
                    } else {
                        return new StatementIteration();
                    }
                }
            }
        } else if (predicate.equals(PropertyGraphSail.LABEL)) {
            Object label = literalToObject(object);
            if (null == label || !(label instanceof String) || !(subject instanceof URI)) {
                return new StatementIteration();
            } else {
                Edge e = edgeForURI((URI) subject);
                if (null == e || !e.getLabel().equals(label)) {
                    return new StatementIteration();
                } else {
                    Source<Edge> s = new Source<Edge>(new SingleItemIterator<Edge>(e), labels);
                    return new StatementIteration(s);
                }
            }
        } else if (predicate.equals(PropertyGraphSail.HEAD)) {
            if (!(subject instanceof URI) || !(object instanceof URI)) {
                return new StatementIteration();
            } else {
                Edge e = edgeForURI((URI) subject);
                Vertex v = vertexForURI((URI) object);
                if (null == e || null == v || !e.getVertex(Direction.IN).equals(v)) {
                    return new StatementIteration();
                } else {
                    Source<Edge> s = new Source<Edge>(new SingleItemIterator<Edge>(e), heads);
                    return new StatementIteration(s);
                }
            }
        } else if (predicate.equals(PropertyGraphSail.TAIL)) {
            if (!(subject instanceof URI) || !(object instanceof URI)) {
                return new StatementIteration();
            } else {
                Edge e = edgeForURI((URI) subject);
                Vertex v = vertexForURI((URI) object);
                if (null == e || null == v || !e.getVertex(Direction.OUT).equals(v)) {
                    return new StatementIteration();
                } else {
                    Source<Edge> s = new Source<Edge>(new SingleItemIterator<Edge>(e), tails);
                    return new StatementIteration(s);
                }
            }
        } else if (isPropertyPredicate(predicate)) {
            Object val = literalToObject(object);
            if (null == val || !(subject instanceof URI)) {
                return new StatementIteration();
            } else {
                String key = keyFromPredicate(predicate);
                Vertex v = vertexForURI((URI) subject);
                if (null == v) {
                    Edge e = edgeForURI((URI) subject);
                    if (null == e) {
                        return new StatementIteration();
                    } else {
                        Source<Edge> edges = new Source<Edge>(
                                new SingleItemIterator<Edge>(e),
                                edgePropertiesWithKeyAndValue(key, predicate, val, (Literal) object));
                        return new StatementIteration(edges);
                    }
                } else {
                    Source<Vertex> vertices = new Source<Vertex>(
                            new SingleItemIterator<Vertex>(v),
                            vertexPropertiesWithKeyAndValue(key, predicate, val, (Literal) object));
                    return new StatementIteration(vertices);
                }
            }
        } else {
            return new StatementIteration();
        }
    }

    private CloseableIteration<Statement, SailException> getStatements_xxO(final Value object) throws SailException {
        if (object instanceof URI) {
            Vertex v = vertexForURI((URI) object);
            if (null == v) {
                if (object.equals(PropertyGraphSail.VERTEX)) {
                    Source<Vertex> vertices = new Source<Vertex>(context.graph.getVertices().iterator(), vertexTypes);
                    return new StatementIteration(vertices);
                } else if (object.equals(PropertyGraphSail.EDGE)) {
                    Source<Edge> edges = new Source<Edge>(context.graph.getEdges().iterator(), edgeTypes);
                    return new StatementIteration(edges);
                } else {
                    return new StatementIteration();
                }
            } else {
                Source<Edge> ins = new Source<Edge>(v.getEdges(Direction.IN).iterator(), heads);
                Source<Edge> outs = new Source<Edge>(v.getEdges(Direction.OUT).iterator(), tails);
                return new StatementIteration(ins, outs);
            }
        } else {
            Object val = literalToObject(object);
            if (null == val) {
                return new StatementIteration();
            } else {
                Collection<Source> sources = new LinkedList<Source>();

                // id
                {
                    Vertex v = context.graph.getVertex(val);
                    Edge e = context.graph.getEdge(val);
                    if (null != v) {
                        sources.add(new Source<Vertex>(new SingleItemIterator<Vertex>(v), vertexIds));
                    }
                    if (null != e) {
                        sources.add(new Source<Edge>(new SingleItemIterator<Edge>(e), edgeIds));
                    }
                }

                // label
                {
                    // TODO: find matching edges faster using indices
                    if (val instanceof String) {
                        Source<Edge> s = new Source<Edge>(
                                context.graph.getEdges().iterator(),
                                matchingLabels((String) val, object));
                        sources.add(s);
                    }
                }

                // properties
                {
                    // TODO: find matching vertices and edges faster using indices
                    Source<Edge> edges = new Source<Edge>(
                            context.graph.getEdges().iterator(),
                            edgePropertiesWithValue(val, (Literal) object));
                    Source<Vertex> vertices = new Source<Vertex>(
                            context.graph.getVertices().iterator(),
                            vertexPropertiesWithValue(val, (Literal) object));
                    sources.add(edges);
                    sources.add(vertices);
                }

                if (sources.size() > 0) {
                    Source[] s = new Source[sources.size()];
                    sources.toArray(s);
                    return new StatementIteration(s);
                } else {
                    return new StatementIteration();
                }
            }
        }
    }

    private CloseableIteration<Statement, SailException> getStatements_xPO(final URI predicate,
                                                                           final Value object) throws SailException {
        if (predicate.equals(RDF.TYPE)) {
            if (object.equals(PropertyGraphSail.VERTEX)) {
                Source<Vertex> s = new Source<Vertex>(context.graph.getVertices().iterator(), vertexTypes);
                return new StatementIteration(s);
            } else if (object.equals(PropertyGraphSail.EDGE)) {
                Source<Edge> s = new Source<Edge>(context.graph.getEdges().iterator(), edgeTypes);
                return new StatementIteration(s);
            } else {
                return new StatementIteration();
            }
        }
        if (predicate.equals(PropertyGraphSail.ID)) {
            Object id = literalToObject(object);
            if (null == id) {
                return new StatementIteration();
            } else {
                Vertex v = context.graph.getVertex(id);
                Edge e = context.graph.getEdge(id);
                if (null == v && null == e) {
                    return new StatementIteration();
                } else {
                    Collection<Statement> s = new LinkedList<Statement>();
                    if (null != v) {
                        vertexIds.generate(v, s);
                    }
                    if (null != e) {
                        edgeIds.generate(e, s);
                    }
                    return new SimpleCloseableIteration<Statement, SailException>(s.iterator());
                }
            }
        } else if (predicate.equals(PropertyGraphSail.LABEL)) {
            // TODO: find edges faster using indices
            Object label = literalToObject(object);
            if (null == label || !(label instanceof String)) {
                return new StatementIteration();
            } else {
                Source<Edge> edges = new Source<Edge>(
                        context.graph.getEdges().iterator(),
                        matchingLabels((String) label, object));
                return new StatementIteration(edges);
            }
        } else if (predicate.equals(PropertyGraphSail.HEAD)) {
            Vertex v = object instanceof URI
                    ? vertexForURI((URI) object)
                    : null;
            if (null == v) {
                return new StatementIteration();
            } else {
                Iterator<Edge> edgeIterator = v.getEdges(Direction.IN).iterator();
                Source<Edge> edges = new Source<Edge>(edgeIterator, heads);
                return new StatementIteration(edges);
            }
        } else if (predicate.equals(PropertyGraphSail.TAIL)) {
            Vertex v = object instanceof URI
                    ? vertexForURI((URI) object)
                    : null;
            if (null == v) {
                return new StatementIteration();
            } else {
                Iterator<Edge> edgeIterator = v.getEdges(Direction.OUT).iterator();
                Source<Edge> edges = new Source<Edge>(edgeIterator, tails);
                return new StatementIteration(edges);
            }
        } else if (isPropertyPredicate(predicate)) {
            Object value = literalToObject(object);
            if (null == value) {
                return new StatementIteration();
            } else {
                // TODO: lookup matching vertices and edges faster using indices
                String key = keyFromPredicate(predicate);
                Source<Edge> edges = new Source<Edge>(
                        context.graph.getEdges().iterator(),
                        edgePropertiesWithKeyAndValue(key, predicate, value, (Literal) object));
                Source<Vertex> vertices = new Source<Vertex>(
                        context.graph.getVertices().iterator(),
                        vertexPropertiesWithKeyAndValue(key, predicate, value, (Literal) object));
                return new StatementIteration(vertices, edges);
            }
        } else {
            return new StatementIteration();
        }
    }

    private CloseableIteration<Statement, SailException> getStatements_xPx(final URI predicate) throws SailException {
        if (predicate.equals(RDF.TYPE)) {
            Source<Edge> edges = new Source<Edge>(context.graph.getEdges().iterator(), edgeTypes);
            Source<Vertex> vertices = new Source<Vertex>(context.graph.getVertices().iterator(), vertexTypes);
            return new StatementIteration(vertices, edges);
        } else if (predicate.equals(PropertyGraphSail.ID)) {
            Source<Edge> edges = new Source<Edge>(context.graph.getEdges().iterator(), edgeIds);
            Source<Vertex> vertices = new Source<Vertex>(context.graph.getVertices().iterator(), vertexIds);
            return new StatementIteration(vertices, edges);
        } else if (predicate.equals(PropertyGraphSail.LABEL)) {
            Iterator<Edge> edgeIterator = context.graph.getEdges().iterator();
            Source<Edge> edges = new Source<Edge>(edgeIterator, labels);
            return new StatementIteration(edges);
        } else if (predicate.equals(PropertyGraphSail.HEAD)) {
            Iterator<Edge> edgeIterator = context.graph.getEdges().iterator();
            Source<Edge> edges = new Source<Edge>(edgeIterator, heads);
            return new StatementIteration(edges);
        } else if (predicate.equals(PropertyGraphSail.TAIL)) {
            Iterator<Edge> edgeIterator = context.graph.getEdges().iterator();
            Source<Edge> edges = new Source<Edge>(edgeIterator, tails);
            return new StatementIteration(edges);
        } else if (isPropertyPredicate(predicate)) {
            // TODO: find elements faster using indices
            String key = keyFromPredicate(predicate);
            Source<Edge> edges = new Source<Edge>(
                    context.graph.getEdges().iterator(),
                    edgePropertiesWithKey(key, predicate));
            Source<Vertex> vertices = new Source<Vertex>(
                    context.graph.getVertices().iterator(),
                    vertexPropertiesWithKey(key, predicate));
            return new StatementIteration(vertices, edges);
        } else {
            return new StatementIteration();
        }
    }

    private boolean isPropertyPredicate(final URI predicate) {
        return predicate.stringValue().startsWith(PropertyGraphSail.PROPERTY_NS);
    }

    private String keyFromPredicate(final URI predicate) {
        return predicate.stringValue().substring(PropertyGraphSail.PROPERTY_NS.length());
    }

    public long size(final Resource... contexts) throws SailException {
        if (!matchesNullContext(contexts)) {
            return 0;
        }

        long count = 0;

        for (Edge e : context.graph.getEdges()) {
            count += 5  // type, id, label, head, tail
                    + e.getPropertyKeys().size();
        }

        for (Vertex v : context.graph.getVertices()) {
            count += 2 // type, id
                    + v.getPropertyKeys().size();
        }

        return count;
    }

    public void commit() throws SailException {
        // Do nothing.
    }

    public void rollback() throws SailException {
        // Do nothing.
    }

    public void addStatement(Resource resource, URI uri, Value value, Resource... resources) throws SailException {
        // Do nothing.
    }

    public void removeStatements(Resource resource, URI uri, Value value, Resource... resources) throws SailException {
        // Do nothing.
    }

    public void clear(Resource... resources) throws SailException {
        // Do nothing.
    }

    public CloseableIteration<? extends Namespace, SailException> getNamespaces() throws SailException {
        return new SimpleCloseableIteration<Namespace, SailException>(namespaces.values().iterator());
    }

    public String getNamespace(final String prefix) throws SailException {
        Namespace n = namespaces.get(prefix);
        return null == n ? null : n.getName();
    }

    public void setNamespace(String s, String s1) throws SailException {
        // Do nothing.
    }

    public void removeNamespace(String s) throws SailException {
        // Do nothing.
    }

    public void clearNamespaces() throws SailException {
        // Do nothing.
    }

    private static class SingleItemIterator<T> implements Iterator<T> {
        private T item;

        public SingleItemIterator(final T item) {
            this.item = item;
        }

        public boolean hasNext() {
            return null != item;
        }

        public T next() {
            T tmp = item;
            item = null;
            return tmp;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private Vertex vertexForURI(final URI uri) {
        String s = uri.stringValue();

        return s.startsWith(PropertyGraphSail.VERTEX_NS)
                ? context.graph.getVertex(idFromString(s.substring(PropertyGraphSail.VERTEX_NS.length())))
                : null;
    }

    private Edge edgeForURI(final URI uri) {
        String s = uri.stringValue();

        return s.startsWith(PropertyGraphSail.EDGE_NS)
                ? context.graph.getEdge(idFromString(s.substring(PropertyGraphSail.EDGE_NS.length())))
                : null;
    }

    private Object literalToObject(final Value v) {
        if (v instanceof Literal) {
            Literal l = (Literal) v;

            URI type = l.getDatatype();
            if (null == type) {
                return l.getLabel();
            } else {
                if (type.equals(XMLSchema.STRING)) {
                    return l.stringValue();
                } else if (type.equals(XMLSchema.LONG)) {
                    return l.longValue();
                } else if (type.equals(XMLSchema.INT)) {
                    return l.intValue();
                } else if (type.equals(XMLSchema.INTEGER)) {
                    return l.integerValue();
                } else if (type.equals(XMLSchema.BYTE)) {
                    return l.byteValue();
                } else if (type.equals(XMLSchema.BOOLEAN)) {
                    return l.booleanValue();
                } else if (type.equals(XMLSchema.SHORT)) {
                    return l.shortValue();
                } else if (type.equals(XMLSchema.FLOAT)) {
                    return l.floatValue();
                } else if (type.equals(XMLSchema.DOUBLE)) {
                    return l.doubleValue();
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    private URI uriForVertex(final Vertex v) {
        return context.valueFactory.createURI(PropertyGraphSail.VERTEX_NS + idToString(v.getId()));
    }

    private URI uriForEdge(final Edge e) {
        return context.valueFactory.createURI(PropertyGraphSail.EDGE_NS + idToString(e.getId()));
    }

    private Object idFromString(final String s) {
        return s;
    }

    private String idToString(final Object id) {
        return id.toString();
    }

    private static interface StatementGenerator<T> {
        void generate(T source, Collection<Statement> results);
    }

    private abstract class ElementGenerator<T> implements StatementGenerator<T> {
        protected boolean doId = false;
        protected boolean doType = false;
        protected boolean doProperties = false;
        protected String[] properties = null;

        public void setDoId(boolean doId) {
            this.doId = doId;
        }

        public void setDoType(boolean doType) {
            this.doType = doType;
        }

        public void setDoProperties(boolean doProperties) {
            this.doProperties = doProperties;
        }

        public void setProperties(String[] properties) {
            this.properties = properties;
        }

        protected void generateCommon(final Element source,
                                      final URI uri,
                                      final Collection<Statement> results) {
            if (doProperties) {
                if (null != properties) {
                    generatePropertyStatements(source, uri, results, properties);
                } else {
                    generatePropertyStatements(source, uri, results);
                }
            }

            if (doId) {
                generateIdStatement(source, uri, results);
            }
        }
    }

    private class VertexGenerator extends ElementGenerator<Vertex> {
        public void generate(Vertex source, Collection<Statement> results) {
            URI uri = uriForVertex(source);

            if (doType) {
                generateVertexTypeStatement(uri, results);
            }

            generateCommon(source, uri, results);
        }
    }

    private class EdgeGenerator extends ElementGenerator<Edge> {
        protected boolean doLabel;
        protected boolean doHead;
        protected boolean doTail;

        public void setDoLabel(boolean doLabel) {
            this.doLabel = doLabel;
        }

        public void setDoHead(boolean doHead) {
            this.doHead = doHead;
        }

        public void setDoTail(boolean doTail) {
            this.doTail = doTail;
        }

        public void generate(Edge source, Collection<Statement> results) {
            URI uri = uriForEdge(source);

            generateCommon(source, uri, results);

            if (doType) {
                generateEdgeTypeStatement(uri, results);
            }

            if (doLabel) {
                generateLabelStatement(source, uri, results);
            }

            if (doHead) {
                URI headUri = uriForVertex(source.getVertex(Direction.IN));
                generateHeadStatement(uri, headUri, results);
            }

            if (doTail) {
                URI tailUri = uriForVertex(source.getVertex(Direction.OUT));
                generateTailStatement(uri, tailUri, results);
            }
        }
    }

    private StatementGenerator<Edge> matchingLabels(final String label,
                                                    final Value object) {
        return new StatementGenerator<Edge>() {
            public void generate(Edge source, Collection<Statement> results) {
                if (source.getLabel().equals(label)) {
                    Statement s = context.valueFactory.createStatement(uriForEdge(source), PropertyGraphSail.LABEL, object);
                    results.add(s);
                }
            }
        };
    }

    private StatementGenerator<Vertex> vertexPropertiesWithKey(final String key,
                                                               final URI pred) {
        return new StatementGenerator<Vertex>() {
            public void generate(Vertex source, Collection<Statement> results) {
                Object o = source.getProperty(key);
                if (null != o) {
                    Literal object = toLiteral(o);
                    if (null != object) {
                        Statement s = context.valueFactory.createStatement(uriForVertex(source), pred, object);
                        results.add(s);
                    }
                }
            }
        };
    }

    private StatementGenerator<Edge> edgePropertiesWithKey(final String key,
                                                           final URI pred) {
        return new StatementGenerator<Edge>() {
            public void generate(Edge source, Collection<Statement> results) {
                Object o = source.getProperty(key);
                if (null != o) {
                    Literal object = toLiteral(o);
                    if (null != object) {
                        Statement s = context.valueFactory.createStatement(uriForEdge(source), pred, object);
                        results.add(s);
                    }
                }
            }
        };
    }

    private StatementGenerator<Vertex> vertexPropertiesWithValue(final Object value,
                                                                 final Literal object) {
        return new StatementGenerator<Vertex>() {
            public void generate(Vertex source, Collection<Statement> results) {
                for (String key : source.getPropertyKeys()) {
                    Object v = source.getProperty(key);
                    if (null != v && v.equals(value)) {
                        URI predicate = predicateForPropertyKey(key);
                        Statement s = context.valueFactory.createStatement(uriForVertex(source), predicate, object);
                        results.add(s);
                    }
                }
            }
        };
    }

    private StatementGenerator<Edge> edgePropertiesWithValue(final Object value,
                                                             final Literal object) {
        return new StatementGenerator<Edge>() {
            public void generate(Edge source, Collection<Statement> results) {
                for (String key : source.getPropertyKeys()) {
                    Object v = source.getProperty(key);
                    if (null != v && v.equals(value)) {
                        URI predicate = predicateForPropertyKey(key);
                        Statement s = context.valueFactory.createStatement(uriForEdge(source), predicate, object);
                        results.add(s);
                    }
                }
            }
        };
    }

    private StatementGenerator<Vertex> vertexPropertiesWithKeyAndValue(final String key,
                                                                       final URI pred,
                                                                       final Object value,
                                                                       final Literal object) {
        return new StatementGenerator<Vertex>() {
            public void generate(Vertex source, Collection<Statement> results) {
                Object o = source.getProperty(key);
                if (null != o && o.equals(value)) {
                    Statement s = context.valueFactory.createStatement(uriForVertex(source), pred, object);
                    results.add(s);
                }
            }
        };
    }

    private StatementGenerator<Edge> edgePropertiesWithKeyAndValue(final String key,
                                                                   final URI pred,
                                                                   final Object value,
                                                                   final Literal object) {
        return new StatementGenerator<Edge>() {
            public void generate(Edge source, Collection<Statement> results) {
                Object o = source.getProperty(key);
                if (null != o && o.equals(value)) {
                    Statement s = context.valueFactory.createStatement(uriForEdge(source), pred, object);
                    results.add(s);
                }
            }
        };
    }

    private URI predicateForPropertyKey(final String key) {
        return context.valueFactory.createURI(PropertyGraphSail.PROPERTY_NS + key);
    }

    private void generateVertexTypeStatement(final URI uri,
                                             final Collection<Statement> results) {
        Statement s = context.valueFactory.createStatement(uri, RDF.TYPE, PropertyGraphSail.VERTEX);
        results.add(s);
    }

    private void generateEdgeTypeStatement(final URI uri,
                                           final Collection<Statement> results) {
        Statement s = context.valueFactory.createStatement(uri, RDF.TYPE, PropertyGraphSail.EDGE);
        results.add(s);
    }

    private void generatePropertyStatements(final Element e,
                                            final URI uri,
                                            final Collection<Statement> results,
                                            final String... keys) {
        if (0 == keys.length) {
            for (String k : e.getPropertyKeys()) {
                Object v = e.getProperty(k);
                Statement s = context.valueFactory.createStatement(
                        uri,
                        context.valueFactory.createURI(PropertyGraphSail.PROPERTY_NS + k),
                        toLiteral(v));
                results.add(s);
            }
        } else {
            for (String k : keys) {
                Object v = e.getProperty(k);
                if (null != v) {
                    Statement s = context.valueFactory.createStatement(
                            uri,
                            context.valueFactory.createURI(PropertyGraphSail.PROPERTY_NS + k),
                            toLiteral(v));
                    results.add(s);
                }
            }
        }
    }

    private void generateIdStatement(final Element e,
                                     final URI uri,
                                     final Collection<Statement> results) {
        Statement s = context.valueFactory.createStatement(
                uri,
                PropertyGraphSail.ID,
                toLiteral(e.getId()));
        results.add(s);
    }

    private void generateLabelStatement(final Edge e,
                                        final URI uri,
                                        final Collection<Statement> results) {
        Statement s = context.valueFactory.createStatement(
                uri,
                PropertyGraphSail.LABEL,
                context.valueFactory.createLiteral(e.getLabel()));
        results.add(s);
    }

    private void generateHeadStatement(final URI edgeUri,
                                       final URI headUri,
                                       final Collection<Statement> results) {
        Statement s = context.valueFactory.createStatement(
                edgeUri,
                PropertyGraphSail.HEAD,
                headUri);
        results.add(s);
    }

    private void generateTailStatement(final URI edgeUri,
                                       final URI tailUri,
                                       final Collection<Statement> results) {
        Statement s = context.valueFactory.createStatement(
                edgeUri,
                PropertyGraphSail.TAIL,
                tailUri);
        results.add(s);
    }

    private Literal toLiteral(final Object o) {
        if (o instanceof String) {
            return context.valueFactory.createLiteral((String) o);
        } else if (o instanceof Integer) {
            return context.valueFactory.createLiteral((Integer) o);
        } else if (o instanceof Long) {
            return context.valueFactory.createLiteral((Long) o);
        } else if (o instanceof Boolean) {
            return context.valueFactory.createLiteral((Boolean) o);
        } else if (o instanceof Byte) {
            return context.valueFactory.createLiteral((Byte) o);
        } else if (o instanceof Short) {
            return context.valueFactory.createLiteral((Short) o);
        } else if (o instanceof Double) {
            return context.valueFactory.createLiteral((Double) o);
        } else if (o instanceof Float) {
            return context.valueFactory.createLiteral((Float) o);
        } else {
            return null;
            //throw new IllegalArgumentException("object has unsupported datatype: " + o);
        }
    }

    private class Source<T> {
        private final Iterator<T> iterator;
        private final StatementGenerator<T> generator;

        public Source(final Iterator<T> iterator,
                      final StatementGenerator<T> generator) {
            this.iterator = iterator;
            this.generator = generator;
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public void generateNext(final Collection<Statement> results) {
            generator.generate(iterator.next(), results);
        }
    }

    private class StatementIteration implements CloseableIteration<Statement, SailException> {
        private final Source[] sources;
        private int i = -1;
        private Collection<Statement> buffer = new LinkedList<Statement>();
        private Iterator<Statement> iter;
        private Source currentSource;

        public StatementIteration(final Source... sources) {
            this.sources = sources;

            advanceSource();
            advanceBuffer();
        }

        private boolean advanceSource() {
            i++;
            if (i >= sources.length) {
                return false;
            } else {
                currentSource = sources[i];
                return true;
            }
        }

        private void advanceBuffer() {
            buffer.clear();

            do {
                if (null != currentSource && currentSource.hasNext()) {
                    currentSource.generateNext(buffer);
                    iter = buffer.iterator();
                } else if (!advanceSource()) {
                    iter = null;
                    return;
                }
            } while (buffer.isEmpty());
        }

        public void close() throws SailException {
            // Do nothing.
        }

        public boolean hasNext() throws SailException {
            return null != iter;
        }

        public Statement next() throws SailException {
            Statement s = iter.next();

            if (!iter.hasNext()) {
                advanceBuffer();
            }

            return s;
        }

        public void remove() throws SailException {
            throw new UnsupportedOperationException();
        }
    }

    private class SimpleCloseableIteration<T, E extends Exception> implements CloseableIteration<T, E> {
        private final Iterator<T> wrapped;

        public SimpleCloseableIteration(Iterator<T> wrapped) {
            this.wrapped = wrapped;
        }

        public void close() throws E {
            // Do nothing.
        }

        public boolean hasNext() throws E {
            return wrapped.hasNext();
        }

        public T next() throws E {
            return wrapped.next();
        }

        public void remove() throws E {
            wrapped.remove();
        }
    }
}