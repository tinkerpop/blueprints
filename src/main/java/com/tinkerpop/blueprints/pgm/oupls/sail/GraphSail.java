package com.tinkerpop.blueprints.pgm.oupls.sail;

import com.tinkerpop.blueprints.pgm.*;
import com.tinkerpop.blueprints.pgm.oupls.GraphSource;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

/**
 * An RDF storage interface for any graph database with a Blueprints IndexableGraph implementation.  It models
 * RDF graphs as property graphs which can be easily traversed and manipulated with other Blueprints-compatible tools.
 * At the same time, it can be used with OpenRDF-based tools to power a SPARQL endpoint, an RDF reasoner, etc.
 * <p/>
 * RDF resources are stored as vertices, RDF statements as edges using the Blueprints default (automatic) indices.
 * Namespaces are stored at a special vertex with the id "urn:com.tinkerpop.blueprints.sail:namespaces".
 * <p/>
 * This Sail is as transactional as the underlying graph database: if the provided Graph implements TransactionalGraph
 * and is in manual transaction mode, then the SailConnection's commit and rollback methods will be used correspondingly.
 * <p/>
 * Retrieval of RDF statements from the store involves both "index-based" and "graph-based" matching, as follows.
 * For each new statement edge which is added to the store, "p" (predicate), "c" (context), and "pc" (predicate and context)
 * property values are added and indexed.  These allow the statement to be quickly retrieved in a query where only the
 * predicate and/or context is specified.  However, BlueprintsSail will additionally index on any triple pattern which
 * is supplied to the constructor, boosting query reactivity at the expense of additional storage overhead.
 * For example, if a "so" pattern is supplied, each new statement edge will also receive an "so" property value which stores the
 * combination of subject and object of the statement.  A subsequent call such as <code>getStatements(john, null, jane)</code> will
 * match both values simultaneously.  This may succeed more quickly than the corresponding graph-based match, which picks
 * either the john or jane vertex as a starting point and filters on adjacent edges.  Graph-based matches are used for
 * all of the triple patterns s,o,sp,so,sc,po,oc,spo,spc,soc,poc,spoc which have not been explicitly flagged for
 * index-based matching.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class GraphSail implements Sail, GraphSource {
    public static final String SEPARATOR = " ";

    public static final String PREDICATE_PROP = "p", CONTEXT_PROP = "c";

    public static final char URI_PREFIX = 'U', BLANK_NODE_PREFIX = 'B', PLAIN_LITERAL_PREFIX = 'P', TYPED_LITERAL_PREFIX = 'T', LANGUAGE_TAG_LITERAL_PREFIX = 'L', NULL_CONTEXT_PREFIX = 'N';

    public static final Pattern INDEX_PATTERN = Pattern.compile("s?p?o?c?");

    // Allow for OrientDB, in which manual vertex IDs are not possible.
    private static boolean FAKE_VERTEX_IDS = true;
    private static final String VALUE = "value";

    private static final String[][] ALTERNATIVES = {{"s", ""}, {"p", ""}, {"o", ""}, {"c", ""}, {"sp", "s", "p"}, {"so", "s", "o"}, {"sc", "s", "c"}, {"po", "o", "p"}, {"pc", "p", "c"}, {"oc", "o", "c"}, {"spo", "so", "sp", "po"}, {"spc", "sc", "sp", "pc"}, {"soc", "so", "sc", "oc"}, {"poc", "po", "oc", "pc"}, {"spoc", "spo", "soc", "spc", "poc"},};

    private static final String NAMESPACES_VERTEX_ID = "urn:com.tinkerpop.blueprints.pgm.oupls.sail:namespaces";

    private final DataStore store = new DataStore();

    /**
     * Create a new RDF store using the provided Blueprints graph.  Default edge indices will be used.
     *
     * @param graph the storage layer.  If the provided graph implements TransactionalGraph and is in manual transaction
     *              mode, then this Sail will also be transactional.
     */
    public GraphSail(final IndexableGraph graph) {
        this(graph, "p,c,pc");
        //this(graph, "s,p,o,c,sp,so,sc,po,pc,oc,spo,spc,soc,poc,spoc");
    }

    /**
     * Create a new RDF store using the provided Blueprints graph.  Additionally, create edge indices for the provided
     * triple patterns (potentially speeding up certain queries, while increasing storage overhead).
     *
     * @param graph           the storage layer.  If the provided graph implements TransactionalGraph and is in manual transaction
     *                        mode, then this Sail will also be transactional.
     * @param indexedPatterns a comma-delimited list of triple patterns for index-based statement matching.  Only p,c are required,
     *                        while the default patterns are p,c,pc.
     */
    public GraphSail(final IndexableGraph graph, final String indexedPatterns) {
        //if (graph instanceof TransactionalGraph)
        //    ((TransactionalGraph) graph).setTransactionMode(TransactionalGraph.Mode.AUTOMATIC);
        //printGraphInfo(graph);

        store.graph = graph;

        // For now, use the default EDGES and VERTICES indices, which *must exist* in Blueprints and are automatically indexed.
        // Think harder about collision issues (if someone hands a non-empty, non-Sail graph to this constructor) later on.
        store.edges = graph.getIndex(Index.EDGES, Edge.class);
        store.vertices = graph.getIndex(Index.VERTICES, Vertex.class);

        store.manualTransactions = store.graph instanceof TransactionalGraph
                && TransactionalGraph.Mode.MANUAL == ((TransactionalGraph) store.graph).getTransactionMode();

        store.namespaces = store.getVertex(NAMESPACES_VERTEX_ID);
        if (null == store.namespaces) {
            if (store.manualTransactions) {
                ((TransactionalGraph) graph).startTransaction();
            }
            try {
                // FIXME: with FAKE_VERTEX_IDS, an extra "namespace" called "value" is present.  Perhaps namespaces
                // should be given individual nodes, rather than being encapsulated in properties of the namespaces node.
                store.namespaces = store.addVertex(NAMESPACES_VERTEX_ID);
            } finally {
                if (store.manualTransactions) {
                    ((TransactionalGraph) graph).stopTransaction(TransactionalGraph.Conclusion.SUCCESS);
                }
            }
        }

        store.matchers[0] = new TrivialMatcher(graph);

        parseTripleIndices(indexedPatterns);
        assignUnassignedTriplePatterns();

        //if (graph instanceof TransactionalGraph)
        //    ((TransactionalGraph) graph).setTransactionMode(TransactionalGraph.Mode.MANUAL);

        //for (int i = 0; i < 16; i++) {
        //    System.out.println("matcher " + i + ": " + indexes.matchers[i]);
        //}
    }

    private void printGraphInfo(final IndexableGraph graph) {
        boolean trans = graph instanceof TransactionalGraph;

        StringBuilder sb = new StringBuilder("graph ").append(graph).append("\n");
        sb.append("\ttransactional: ").append(trans).append("\n");
        if (trans) {
            sb.append("\tmode: ").append(((TransactionalGraph) graph).getTransactionMode()).append("\n");
        }

        System.out.println(sb.toString());
    }

    public Graph getGraph() {
        return this.store.getGraph();
    }

    public void setDataDir(final File file) {
        throw new UnsupportedOperationException();
    }

    public File getDataDir() {
        throw new UnsupportedOperationException();
    }

    public void initialize() throws SailException {
        // Do nothing.
    }

    public void shutDown() throws SailException {
        store.graph.shutdown();
    }

    public boolean isWritable() throws SailException {
        // For now, we assume the store is writable.
        return true;
    }

    public SailConnection getConnection() throws SailException {
        return new GraphSailConnection(store);
    }

    public ValueFactory getValueFactory() {
        return store.valueFactory;
    }

    public String toString() {
        String type = store.graph.getClass().getSimpleName().toLowerCase();
        return "graphsail[" + type + "]";
    }

    ////////////////////////////////////////////////////////////////////////////

    /**
     * A context object which is shared between the Blueprints Sail and its connections.
     */
    public class DataStore {
        public IndexableGraph graph;

        // We don't need a special ValueFactory implementation.
        public final ValueFactory valueFactory = new ValueFactoryImpl();

        public final Collection<IndexingMatcher> indexers = new LinkedList<IndexingMatcher>();

        // A triple pattern matcher for each spoc combination
        public final Matcher[] matchers = new Matcher[16];

        public boolean manualTransactions;

        public Index<Vertex> vertices;
        public Index<Edge> edges;

        public Vertex namespaces;

        public Vertex getVertex(final String value) {
            if (FAKE_VERTEX_IDS) {
                //System.out.println("value = " + value);
                Iterator<Vertex> i = vertices.get(VALUE, value).iterator();
                return i.hasNext() ? i.next() : null;
            } else {
                return graph.getVertex(value);
            }
        }

        public Vertex addVertex(final String id) {
            if (FAKE_VERTEX_IDS) {
                Vertex v = graph.addVertex(null);
                //vertices.put(VALUE, id, store.namespaces);
                v.setProperty(VALUE, id);
                return v;
            } else {
                return graph.addVertex(id);
            }
        }

        public String getValueOf(final Vertex v) {
            if (FAKE_VERTEX_IDS) {
                return (String) v.getProperty(VALUE);
            } else {
                return (String) v.getId();
            }
        }

        public IndexableGraph getGraph() {
            return this.graph;
        }
    }

    private void parseTripleIndices(final String tripleIndexes) {
        if (null == tripleIndexes) {
            throw new IllegalArgumentException("index list, if supplied, must be non-null");
        }

        Set<String> u = new HashSet<String>();

        String[] a = tripleIndexes.split(",");
        if (0 == a.length) {
            throw new IllegalArgumentException("index list, if supplied, must be non-empty");
        }
        for (String s : a) {
            u.add(s.trim());
        }

        // These two patterns are required for efficient operation.
        u.add("p");
        u.add("c");

        for (String s : u) {
            createIndexingMatcher(s);
        }
    }

    private void assignUnassignedTriplePatterns() {
        // As a first pass, fill in all suitable patterns (those containing
        // subject and/or object) not already assigned to indexing matchers,
        // with graph-based matchers.
        for (int i = 0; i < 16; i++) {
            if (null == store.matchers[i] && ((0 != (i & 0x1)) || (0 != (i & 0x4)))) {
                store.matchers[i] = new GraphBasedMatcher((0 != (i & 0x1)), (0 != (i & 0x2)), (0 != (i & 0x4)), (0 != (i & 0x8)), store);
            }
        }

        // Now fill in any remaining patterns with alternative indexing matchers.
        Matcher[] n = new Matcher[16];
        n[0] = store.matchers[0];
        for (String[] alts : ALTERNATIVES) {
            String p = alts[0];
            int i = indexFor(p);

            Matcher m = store.matchers[i];

            // if no matcher has been assigned for this pattern
            if (null == m) {
                // try primary alternatives in the order they are specified
                for (int j = 1; j < alts.length; j++) {
                    m = store.matchers[indexFor(alts[j])];
                    if (null != m) {
                        break;
                    }
                }

                // if no primary alternatives are assigned, choose the first secondary alternative
                if (null == m) {
                    m = n[1];
                }
            }

            n[i] = m;
        }

        System.arraycopy(n, 0, store.matchers, 0, 16);
    }

    private int indexFor(final boolean s, final boolean p, final boolean o, final boolean c) {
        int index = 0;

        if (s) {
            index |= 0x1;
        }
        if (p) {
            index |= 0x2;
        }
        if (o) {
            index |= 0x4;
        }
        if (c) {
            index |= 0x8;
        }

        return index;
    }

    private int indexFor(final String pattern) {
        boolean s = false, p = false, o = false, c = false;
        for (byte ch : pattern.getBytes()) {
            switch (ch) {
                case 's':
                    s = true;
                    break;
                case 'p':
                    p = true;
                    break;
                case 'o':
                    o = true;
                    break;
                case 'c':
                    c = true;
                    break;
                default:
                    throw new IllegalStateException();
            }
        }

        return indexFor(s, p, o, c);
    }

    private void createIndexingMatcher(final String pattern) {
        boolean s = false, p = false, o = false, c = false;
        for (byte ch : pattern.getBytes()) {
            switch (ch) {
                case 's':
                    s = true;
                    break;
                case 'p':
                    p = true;
                    break;
                case 'o':
                    o = true;
                    break;
                case 'c':
                    c = true;
                    break;
                default:
                    throw new IllegalStateException();
            }
        }

        int index = indexFor(s, p, o, c);
        IndexingMatcher m = new IndexingMatcher(s, p, o, c, store);
        store.matchers[index] = m;
        store.indexers.add(m);
    }

    private static void debugEdge(final Edge e) {
        System.out.println("edge " + e + ":");
        for (String key : e.getPropertyKeys()) {
            System.out.println("\t" + key + ":\t'" + e.getProperty(key) + "'");
        }
        System.out.println("\t[in vertex]: " + e.getInVertex());
        System.out.println("\t[out vertex]: " + e.getOutVertex());
    }

    private static void debugVertex(final Vertex v) {
        System.out.println("vertex " + v + ":");
        for (String key : v.getPropertyKeys()) {
            System.out.println("\t" + key + ":\t'" + v.getProperty(key) + "'");
        }
        Iterator<Edge> i;
        i = v.getInEdges().iterator();
        System.out.println("\t[in edges]:");
        while (i.hasNext()) {
            System.out.println("\t\t" + i.next());
        }
        i = v.getOutEdges().iterator();
        System.out.println("\t[out edges]:");
        while (i.hasNext()) {
            System.out.println("\t\t" + i.next());
        }
    }
}
