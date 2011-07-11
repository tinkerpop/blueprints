package com.tinkerpop.blueprints.pgm.oupls.sail;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;
import info.aduna.iteration.CloseableIteration;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.sparql.SPARQLParser;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public abstract class GraphSailTest extends SailTest {
    protected abstract IndexableGraph createGraph();

    protected IndexableGraph graph;

    protected Sail createSail() throws Exception {
        // Flip this flag in order to test "unique statements" behavior
        uniqueStatements = false;

        graph = createGraph();
        GraphSail g = new GraphSail(graph);
        if (uniqueStatements) {
            g.enforceUniqueStatements(true);
        }

        return g;
    }

    protected void before() throws Exception {
        // Nothing to do.
    }

    protected void after() throws Exception {
        // Nothing to do.
    }

    public void testIndexPatterns() throws Exception {
        assertTriplePattern("spoc", true);
        assertTriplePattern("poc", true);
        assertTriplePattern("p", true);
        assertTriplePattern("", true);

        assertTriplePattern("xpoc", false);
        assertTriplePattern("sspo", false);
    }

    public void testCodePlay() throws Exception {
        Sail sail = new GraphSail(new TinkerGraph());
        sail.initialize();
        try {
            SailConnection sc = sail.getConnection();
            try {
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

                Graph graph = ((GraphSail) sail).getGraph();
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
}
