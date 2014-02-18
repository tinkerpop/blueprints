package com.tinkerpop.blueprints.oupls.sail;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import info.aduna.iteration.CloseableIteration;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.sparql.SPARQLParser;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import java.io.File;
import java.net.URL;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public abstract class GraphSailTest extends SailTest {
    protected abstract KeyIndexableGraph createGraph() throws Exception;

    protected KeyIndexableGraph graph;

    protected Sail createSail() throws Exception {
        // Flip this flag in order to disable "unique statements" behavior
        uniqueStatements = true;

        graph = createGraph();
        GraphSail<KeyIndexableGraph> g = new GraphSail<KeyIndexableGraph>(graph);
        g.enforceUniqueStatements(uniqueStatements);

        return g;
    }

    protected void before() throws Exception {
        // Nothing to do.
    }

    protected void after() throws Exception {
        // Nothing to do.
    }

    /*protected static void deleteDirectory(final File directory) {
        if (directory.exists()) {
            for (File file : directory.listFiles()) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
            directory.delete();
        }
    }*/


    @Test
    public void testOrphanVerticesAutomaticallyDeleted() throws Exception {
        String ex = "http://example.org/ns#";
        URI ref = new URIImpl(ex + "Ref");

        clear();
        int edgesBefore, verticesBefore;

        SailConnection sc = sail.getConnection();
        try {
            sc.begin();
            edgesBefore = countEdges();
            verticesBefore = countVertices();
        } finally {
            sc.commit();
            sc.close();
        }

        addFile(SailTest.class.getResourceAsStream("graph-example-bnodes.trig"), RDFFormat.TRIG);

        sc = sail.getConnection();
        //showStatements(sc, null, null, null);
        try {
            sc.begin();
            assertEquals(14, countStatements(sc, null, null, null, false));
            assertEquals(edgesBefore + 14, countEdges());
            assertEquals(verticesBefore + 10, countVertices());

            sc.removeStatements(ref, null, null);
            sc.commit();
            sc.begin();

            assertEquals(13, countStatements(sc, null, null, null, false));
            assertEquals(edgesBefore + 13, countEdges());
            assertEquals(verticesBefore + 9, countVertices());

            sc.clear();
            sc.commit();
            sc.begin();

            assertEquals(0, countStatements(sc, null, null, null, false));
            assertEquals(0, countEdges());
            // Namespaces vertex is still present.
            assertEquals(verticesBefore, countVertices());
        } finally {
            sc.rollback();
            sc.close();
        }
    }

    @Test
    public void testBlankNodesUnique() throws Exception {
        String ex = "http://example.org/ns#";
        URI class1 = new URIImpl(ex + "Class1");

        clear();
        int edgesBefore, verticesBefore;

        SailConnection sc = sail.getConnection();
        try {
            sc.begin();
            edgesBefore = countEdges();
            verticesBefore = countVertices();
        } finally {
            sc.rollback();
            sc.close();
        }

        // Load a file once.
        addFile(SailTest.class.getResourceAsStream("graph-example-bnodes.trig"), RDFFormat.TRIG);

        sc = sail.getConnection();
        try {
            assertEquals(3, countStatements(sc, class1, RDFS.SUBCLASSOF, null, false));
            assertEquals(edgesBefore + 14, countEdges());
            assertEquals(verticesBefore + 10, countVertices());
        } finally {
            sc.rollback();
            sc.close();
        }

        // Load the file again.
        // Loading the same file twice results in extra vertices and edges,
        // since blank nodes assume different identities on each load.
        addFile(SailTest.class.getResourceAsStream("graph-example-bnodes.trig"), RDFFormat.TRIG);

        sc = sail.getConnection();
        try {
            assertEquals(5, countStatements(sc, class1, RDFS.SUBCLASSOF, null, false));
            assertEquals(edgesBefore + 23, countEdges());
            assertEquals(verticesBefore + 12, countVertices());
        } finally {
            sc.rollback();
            sc.close();
        }
    }

    @Test
    public void testIndexPatterns() throws Exception {
        assertTriplePattern("spoc", true);
        assertTriplePattern("poc", true);
        assertTriplePattern("p", true);
        assertTriplePattern("", true);

        assertTriplePattern("xpoc", false);
        assertTriplePattern("sspo", false);
    }

    @Test
    public void testCodePlay() throws Exception {
        Sail sail = new GraphSail(new TinkerGraph());
        sail.initialize();
        try {
            SailConnection sc = sail.getConnection();
            try {
                sc.begin();
                ValueFactory vf = sail.getValueFactory();
                sc.addStatement(vf.createURI("http://tinkerpop.com#1"), vf.createURI("http://tinkerpop.com#knows"), vf.createURI("http://tinkerpop.com#3"), vf.createURI("http://tinkerpop.com"));
                sc.addStatement(vf.createURI("http://tinkerpop.com#1"), vf.createURI("http://tinkerpop.com#name"), vf.createLiteral("marko"), vf.createURI("http://tinkerpop.com"));
                sc.addStatement(vf.createURI("http://tinkerpop.com#3"), vf.createURI("http://tinkerpop.com#name"), vf.createLiteral("josh"), vf.createURI("http://tinkerpop.com"));
                CloseableIteration<? extends Statement, SailException> results = sc.getStatements(null, null, null, false);
                try {
                    System.out.println("get statements: ?s ?p ?o ?g");
                    while (results.hasNext()) {
                        System.out.println(results.next());
                    }
                } finally {
                    results.close();
                }

                System.out.println("\nget statements: http://tinkerpop.com#3 ?p ?o ?g");
                results = sc.getStatements(vf.createURI("http://tinkerpop.com#3"), null, null, false);
                try {
                    while (results.hasNext()) {
                        System.out.println(results.next());
                    }
                } finally {
                    results.close();
                }

                SPARQLParser parser = new SPARQLParser();
                CloseableIteration<? extends BindingSet, QueryEvaluationException> sparqlResults;
                String queryString = "SELECT ?x ?y WHERE { ?x <http://tinkerpop.com#knows> ?y }";
                ParsedQuery query = parser.parseQuery(queryString, "http://tinkerPop.com");

                System.out.println("\nSPARQL: " + queryString);
                sparqlResults = sc.evaluate(query.getTupleExpr(), query.getDataset(), new EmptyBindingSet(), false);
                try {
                    while (sparqlResults.hasNext()) {
                        System.out.println(sparqlResults.next());
                    }
                } finally {
                    sparqlResults.close();
                }

                Graph graph = ((GraphSail) sail).getBaseGraph();
                System.out.println();
                for (Vertex v : graph.getVertices()) {
                    System.out.println("------");
                    System.out.println(v);
                    for (String key : v.getPropertyKeys()) {
                        System.out.println(key + "=" + v.getProperty(key));
                    }
                }
                for (Edge e : graph.getEdges()) {
                    System.out.println("------");
                    System.out.println(e);
                    for (String key : e.getPropertyKeys()) {
                        System.out.println(key + "=" + e.getProperty(key));
                    }
                }
            } finally {
                sc.rollback();
                sc.close();
            }
        } finally {
            sail.shutDown();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////

    private void assertTriplePattern(final String pattern, final boolean isValid) {
        boolean m = GraphSail.INDEX_PATTERN.matcher(pattern).matches();
        assertTrue(isValid ? m : !m);
    }

    private int countVertices() {
        int count = 0;

        for (Vertex v : graph.getVertices()) {
            count++;
        }

        return count;
    }

    private int countEdges() {
        int count = 0;

        for (Edge e : graph.getEdges()) {
            count++;
        }

        return count;
    }

    protected static void deleteDirectory(final File directory) {
        if (directory.exists()) {
            for (File file : directory.listFiles()) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
            directory.delete();
        }
    }

    protected File computeTestDataRoot() {
        final String clsUri = this.getClass().getName().replace('.', '/') + ".class";
        final URL url = this.getClass().getClassLoader().getResource(clsUri);
        final String clsPath = url.getPath();
        final File root = new File(clsPath.substring(0, clsPath.length() - clsUri.length()));
        return new File(root.getParentFile(), "test-data");
    }
}
