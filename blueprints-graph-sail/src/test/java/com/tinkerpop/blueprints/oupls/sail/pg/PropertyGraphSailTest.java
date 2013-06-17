package com.tinkerpop.blueprints.oupls.sail.pg;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReader;
import info.aduna.iteration.CloseableIteration;
import net.fortytwo.sesametools.StatementComparator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.sparql.SPARQLParser;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class PropertyGraphSailTest {
    private final PropertyGraphSail sail;
    private final ValueFactory vf;

    private final URI edge, head, id, label, tail, vertex;
    private final URI age, josh, lang, lop, marko, name, peter, ripple, vadas, weight;
    private final URI created, knows;

    private final URI markoKnowsVadas, markoKnowsJosh, markoCreatedLop, joshCreatedRipple, joshCreatedLop, peterCreatedLop;

    private SailConnection sc;

    public PropertyGraphSailTest() throws Exception {
        Graph g = new TinkerGraph();
        GraphMLReader r = new GraphMLReader(g);
        r.inputGraph(GraphMLReader.class.getResourceAsStream("graph-example-1.xml"));

        sail = new PropertyGraphSail(g);
        sail.initialize();

        vf = sail.getValueFactory();

        edge = vf.createURI(PropertyGraphSail.ONTOLOGY_NS + "Edge");
        head = vf.createURI(PropertyGraphSail.ONTOLOGY_NS + "head");
        id = vf.createURI(PropertyGraphSail.ONTOLOGY_NS + "id");
        label = vf.createURI(PropertyGraphSail.ONTOLOGY_NS + "label");
        tail = vf.createURI(PropertyGraphSail.ONTOLOGY_NS + "tail");
        vertex = vf.createURI(PropertyGraphSail.ONTOLOGY_NS + "Vertex");

        age = vf.createURI(PropertyGraphSail.PROPERTY_NS + "age");
        lang = vf.createURI(PropertyGraphSail.PROPERTY_NS + "lang");
        name = vf.createURI(PropertyGraphSail.PROPERTY_NS + "name");
        weight = vf.createURI(PropertyGraphSail.PROPERTY_NS + "weight");

        josh = vf.createURI(PropertyGraphSail.VERTEX_NS + "4");
        lop = vf.createURI(PropertyGraphSail.VERTEX_NS + "3");
        marko = vf.createURI(PropertyGraphSail.VERTEX_NS + "1");
        peter = vf.createURI(PropertyGraphSail.VERTEX_NS + "6");
        ripple = vf.createURI(PropertyGraphSail.VERTEX_NS + "5");
        vadas = vf.createURI(PropertyGraphSail.VERTEX_NS + "2");

        markoKnowsVadas = vf.createURI(PropertyGraphSail.EDGE_NS + "7");
        markoKnowsJosh = vf.createURI(PropertyGraphSail.EDGE_NS + "8");
        markoCreatedLop = vf.createURI(PropertyGraphSail.EDGE_NS + "9");
        joshCreatedRipple = vf.createURI(PropertyGraphSail.EDGE_NS + "10");
        joshCreatedLop = vf.createURI(PropertyGraphSail.EDGE_NS + "11");
        peterCreatedLop = vf.createURI(PropertyGraphSail.EDGE_NS + "12");

        created = vf.createURI(PropertyGraphSail.RELATION_NS + "created");
        knows = vf.createURI(PropertyGraphSail.RELATION_NS + "knows");
    }

    @Before
    public void setUp() throws Exception {
        sc = sail.getConnection();
    }

    @After
    public void tearDown() throws Exception {
        sc.close();
    }

    @Test
    public void testSize() throws Exception {
        assertEquals(60, sc.size());
    }

    @Test
    public void testNamespaces() throws Exception {
        assertEquals(5, count(sc.getNamespaces()));

        assertEquals("http://tinkerpop.com/pgm/property/", sc.getNamespace("prop"));
        assertEquals("http://tinkerpop.com/pgm/ontology#", sc.getNamespace("pgm"));
        assertEquals("http://tinkerpop.com/pgm/vertex/", sc.getNamespace("vertex"));
        assertEquals("http://tinkerpop.com/pgm/edge/", sc.getNamespace("edge"));
        assertEquals("http://www.w3.org/1999/02/22-rdf-syntax-ns#", sc.getNamespace("rdf"));
    }

    @Test
    public void testVertexProperties() throws Exception {
        // s p o
        assertExpected(get(marko, name, vf.createLiteral("marko")),
                vf.createStatement(marko, name, vf.createLiteral("marko")));
        // s p ?
        assertExpected(get(marko, name, null),
                vf.createStatement(marko, name, vf.createLiteral("marko")));
        // ? p o
        assertExpected(get(null, name, vf.createLiteral("marko")),
                vf.createStatement(marko, name, vf.createLiteral("marko")));
        // ? ? o
        assertExpected(get(null, null, vf.createLiteral("marko")),
                vf.createStatement(marko, name, vf.createLiteral("marko")));
        // ? p ?
        assertExpected(get(null, name, null),
                vf.createStatement(josh, name, vf.createLiteral("josh")),
                vf.createStatement(lop, name, vf.createLiteral("lop")),
                vf.createStatement(marko, name, vf.createLiteral("marko")),
                vf.createStatement(peter, name, vf.createLiteral("peter")),
                vf.createStatement(ripple, name, vf.createLiteral("ripple")),
                vf.createStatement(vadas, name, vf.createLiteral("vadas")));
        // s ? o
        assertExpected(get(marko, null, vf.createLiteral("marko")),
                vf.createStatement(marko, name, vf.createLiteral("marko")));
    }

    @Test
    public void testEdgeProperties() throws Exception {
        // s p o
        assertExpected(get(joshCreatedRipple, weight, vf.createLiteral(1.0f)),
                vf.createStatement(joshCreatedRipple, weight, vf.createLiteral(1.0f)));
        // s p ?
        assertExpected(get(joshCreatedRipple, weight, null),
                vf.createStatement(joshCreatedRipple, weight, vf.createLiteral(1.0f)));
        // ? p o
        assertExpected(get(null, weight, vf.createLiteral(1.0f)),
                vf.createStatement(joshCreatedRipple, weight, vf.createLiteral(1.0f)),
                vf.createStatement(markoKnowsJosh, weight, vf.createLiteral(1.0f)));
        // ? ? o
        assertExpected(get(null, null, vf.createLiteral(1.0f)),
                vf.createStatement(joshCreatedRipple, weight, vf.createLiteral(1.0f)),
                vf.createStatement(markoKnowsJosh, weight, vf.createLiteral(1.0f)));
        // ? p ?
        assertExpected(get(null, weight, null),
                vf.createStatement(joshCreatedRipple, weight, vf.createLiteral(1.0f)),
                vf.createStatement(markoKnowsVadas, weight, vf.createLiteral(0.5f)),
                vf.createStatement(markoCreatedLop, weight, vf.createLiteral(0.4f)),
                vf.createStatement(joshCreatedLop, weight, vf.createLiteral(0.4f)),
                vf.createStatement(peterCreatedLop, weight, vf.createLiteral(0.2f)),
                vf.createStatement(markoKnowsJosh, weight, vf.createLiteral(1.0f)));
        // s ? o
        assertExpected(get(joshCreatedRipple, null, vf.createLiteral(1.0f)),
                vf.createStatement(joshCreatedRipple, weight, vf.createLiteral(1.0f)));
    }

    @Test
    public void testIds() throws Exception {
        // s p o
        assertExpected(get(marko, id, vf.createLiteral("1")),
                vf.createStatement(marko, id, vf.createLiteral("1")));
        // s p ?
        assertExpected(get(marko, id, null),
                vf.createStatement(marko, id, vf.createLiteral("1")));
        // ? p o
        assertExpected(get(null, id, vf.createLiteral("1")),
                vf.createStatement(marko, id, vf.createLiteral("1")));
        // ? ? o
        assertExpected(get(null, null, vf.createLiteral("1")),
                vf.createStatement(marko, id, vf.createLiteral("1")));
        // ? p ?
        assertExpected(get(null, id, null),
                vf.createStatement(marko, id, vf.createLiteral("1")),
                vf.createStatement(vadas, id, vf.createLiteral("2")),
                vf.createStatement(lop, id, vf.createLiteral("3")),
                vf.createStatement(josh, id, vf.createLiteral("4")),
                vf.createStatement(ripple, id, vf.createLiteral("5")),
                vf.createStatement(peter, id, vf.createLiteral("6")),
                vf.createStatement(markoKnowsVadas, id, vf.createLiteral("7")),
                vf.createStatement(markoKnowsJosh, id, vf.createLiteral("8")),
                vf.createStatement(markoCreatedLop, id, vf.createLiteral("9")),
                vf.createStatement(joshCreatedRipple, id, vf.createLiteral("10")),
                vf.createStatement(joshCreatedLop, id, vf.createLiteral("11")),
                vf.createStatement(peterCreatedLop, id, vf.createLiteral("12")));
        // s ? o
        assertExpected(get(marko, null, vf.createLiteral("1")),
                vf.createStatement(marko, id, vf.createLiteral("1")));
    }

    @Test
    public void testLabels() throws Exception {
        // s p o
        assertExpected(get(markoKnowsVadas, label, vf.createLiteral("knows")),
                vf.createStatement(markoKnowsVadas, label, vf.createLiteral("knows")));
        // s p ?
        assertExpected(get(markoKnowsVadas, label, null),
                vf.createStatement(markoKnowsVadas, label, vf.createLiteral("knows")));
        // ? p o
        assertExpected(get(null, label, vf.createLiteral("knows")),
                vf.createStatement(markoKnowsVadas, label, vf.createLiteral("knows")),
                vf.createStatement(markoKnowsJosh, label, vf.createLiteral("knows")));
        // ? ? o
        assertExpected(get(null, null, vf.createLiteral("knows")),
                vf.createStatement(markoKnowsVadas, label, vf.createLiteral("knows")),
                vf.createStatement(markoKnowsJosh, label, vf.createLiteral("knows")));
        // ? p ?
        assertExpected(get(null, label, null),
                vf.createStatement(markoCreatedLop, label, vf.createLiteral("created")),
                vf.createStatement(joshCreatedLop, label, vf.createLiteral("created")),
                vf.createStatement(joshCreatedRipple, label, vf.createLiteral("created")),
                vf.createStatement(peterCreatedLop, label, vf.createLiteral("created")),
                vf.createStatement(markoKnowsVadas, label, vf.createLiteral("knows")),
                vf.createStatement(markoKnowsJosh, label, vf.createLiteral("knows")));
        // s ? o
        assertExpected(get(markoKnowsVadas, null, vf.createLiteral("knows")),
                vf.createStatement(markoKnowsVadas, label, vf.createLiteral("knows")));
    }

    @Test
    public void testHeads() throws Exception {
        // s p o
        assertExpected(get(markoCreatedLop, head, lop),
                vf.createStatement(markoCreatedLop, head, lop));
        // s p ?
        assertExpected(get(markoCreatedLop, head, null),
                vf.createStatement(markoCreatedLop, head, lop));
        // ? p o
        assertExpected(get(null, head, lop),
                vf.createStatement(markoCreatedLop, head, lop),
                vf.createStatement(joshCreatedLop, head, lop),
                vf.createStatement(peterCreatedLop, head, lop));
        // ? ? o
        assertExpected(get(null, null, lop),
                vf.createStatement(markoCreatedLop, head, lop),
                vf.createStatement(joshCreatedLop, head, lop),
                vf.createStatement(peterCreatedLop, head, lop));
        // ? p ?
        assertExpected(get(null, head, null),
                vf.createStatement(markoKnowsJosh, head, josh),
                vf.createStatement(markoKnowsVadas, head, vadas),
                vf.createStatement(markoCreatedLop, head, lop),
                vf.createStatement(joshCreatedRipple, head, ripple),
                vf.createStatement(joshCreatedLop, head, lop),
                vf.createStatement(peterCreatedLop, head, lop));
        // s ? o
        assertExpected(get(markoCreatedLop, null, lop),
                vf.createStatement(markoCreatedLop, head, lop));
    }

    @Test
    public void testTails() throws Exception {
        // s p o
        assertExpected(get(markoCreatedLop, tail, marko),
                vf.createStatement(markoCreatedLop, tail, marko));
        // s p ?
        assertExpected(get(markoCreatedLop, tail, null),
                vf.createStatement(markoCreatedLop, tail, marko));
        // ? p o
        assertExpected(get(null, tail, marko),
                vf.createStatement(markoCreatedLop, tail, marko),
                vf.createStatement(markoKnowsJosh, tail, marko),
                vf.createStatement(markoKnowsVadas, tail, marko));
        // ? ? o
        assertExpected(get(null, null, marko),
                vf.createStatement(markoCreatedLop, tail, marko),
                vf.createStatement(markoKnowsJosh, tail, marko),
                vf.createStatement(markoKnowsVadas, tail, marko));
        // ? p ?
        assertExpected(get(null, tail, null),
                vf.createStatement(markoKnowsJosh, tail, marko),
                vf.createStatement(markoKnowsVadas, tail, marko),
                vf.createStatement(markoCreatedLop, tail, marko),
                vf.createStatement(joshCreatedRipple, tail, josh),
                vf.createStatement(joshCreatedLop, tail, josh),
                vf.createStatement(peterCreatedLop, tail, peter));
        // s ? o
        assertExpected(get(markoCreatedLop, null, marko),
                vf.createStatement(markoCreatedLop, tail, marko));
    }

    @Test
    public void testTypes() throws Exception {
        // s p o
        assertExpected(get(marko, RDF.TYPE, vertex),
                vf.createStatement(marko, RDF.TYPE, vertex));
        // s p ?
        assertExpected(get(marko, RDF.TYPE, null),
                vf.createStatement(marko, RDF.TYPE, vertex));
        // ? p o
        assertExpected(get(null, RDF.TYPE, vertex),
                vf.createStatement(marko, RDF.TYPE, vertex),
                vf.createStatement(vadas, RDF.TYPE, vertex),
                vf.createStatement(lop, RDF.TYPE, vertex),
                vf.createStatement(josh, RDF.TYPE, vertex),
                vf.createStatement(ripple, RDF.TYPE, vertex),
                vf.createStatement(peter, RDF.TYPE, vertex));
        // ? ? o
        assertExpected(get(null, null, vertex),
                vf.createStatement(marko, RDF.TYPE, vertex),
                vf.createStatement(vadas, RDF.TYPE, vertex),
                vf.createStatement(lop, RDF.TYPE, vertex),
                vf.createStatement(josh, RDF.TYPE, vertex),
                vf.createStatement(ripple, RDF.TYPE, vertex),
                vf.createStatement(peter, RDF.TYPE, vertex));
        // ? p ?
        assertExpected(get(null, RDF.TYPE, null),
                vf.createStatement(markoKnowsJosh, RDF.TYPE, edge),
                vf.createStatement(markoKnowsVadas, RDF.TYPE, edge),
                vf.createStatement(markoCreatedLop, RDF.TYPE, edge),
                vf.createStatement(joshCreatedRipple, RDF.TYPE, edge),
                vf.createStatement(joshCreatedLop, RDF.TYPE, edge),
                vf.createStatement(peterCreatedLop, RDF.TYPE, edge),
                vf.createStatement(marko, RDF.TYPE, vertex),
                vf.createStatement(vadas, RDF.TYPE, vertex),
                vf.createStatement(lop, RDF.TYPE, vertex),
                vf.createStatement(josh, RDF.TYPE, vertex),
                vf.createStatement(ripple, RDF.TYPE, vertex),
                vf.createStatement(peter, RDF.TYPE, vertex));
        // s ? o
        assertExpected(get(marko, null, vertex),
                vf.createStatement(marko, RDF.TYPE, vertex));
    }

    @Test
    public void testxxx() throws Exception {
        assertEquals(60, get(null, null, null).size());
    }

    @Test
    public void testSxx() throws Exception {
        assertExpected(get(ripple, null, null),
                vf.createStatement(ripple, id, vf.createLiteral("5")),
                vf.createStatement(ripple, RDF.TYPE, vertex),
                vf.createStatement(ripple, lang, vf.createLiteral("java")),
                vf.createStatement(ripple, name, vf.createLiteral("ripple")));
    }

    @Test
    public void testPropertyTypeSensitivity() throws Exception {
        assertExpected(get(joshCreatedRipple, weight, vf.createLiteral(1.0f)),
                vf.createStatement(joshCreatedRipple, weight, vf.createLiteral(1.0f)));

        assertExpected(get(joshCreatedRipple, weight, vf.createLiteral(1.0)));
    }

    @Test
    public void testRDFDump() throws Exception {
        Repository repo = new SailRepository(sail);
        RepositoryConnection rc = repo.getConnection();
        try {
            RDFWriter w = Rio.createWriter(RDFFormat.TURTLE, System.out);
            rc.export(w);
        } finally {
            rc.close();
        }
    }

    @Test
    public void testSPARQL() throws Exception {
        int count;
        String queryStr = "PREFIX pgm: <" + PropertyGraphSail.ONTOLOGY_NS + ">\n" +
                "PREFIX prop: <" + PropertyGraphSail.PROPERTY_NS + ">\n" +
                "SELECT ?project ?name WHERE {\n" +
                "   ?marko prop:name \"marko\".\n" +
                "   ?e1 pgm:label \"knows\".\n" +
                "   ?e1 pgm:tail ?marko.\n" +
                "   ?e1 pgm:head ?friend.\n" +
                "   ?e2 pgm:label \"created\".\n" +
                "   ?e2 pgm:tail ?friend.\n" +
                "   ?e2 pgm:head ?project.\n" +
                "   ?project prop:name ?name.\n" +
                "}";
        System.out.println(queryStr);
        ParsedQuery query = new SPARQLParser().parseQuery(queryStr, "http://example.org/bogus/");
        CloseableIteration<? extends BindingSet, QueryEvaluationException> results
                = sc.evaluate(query.getTupleExpr(), query.getDataset(), new EmptyBindingSet(), false);
        try {
            count = 0;
            while (results.hasNext()) {
                count++;
                BindingSet set = results.next();
                URI project = (URI) set.getValue("project");
                Literal name = (Literal) set.getValue("name");
                assertNotNull(project);
                assertNotNull(name);
                System.out.println("project = " + project + ", name = " + name);
            }
        } finally {
            results.close();
        }
        assertEquals(2, count);
    }

    @Test
    public void testSimpleEdges() throws Exception {
        sail.setFirstClassEdges(false);
        sc.close();
        sc = sail.getConnection();

        for (Statement st : get(null, null, null)) {
            System.out.println("st: " + st);
        }

        assertEquals(30, sc.size());
        assertEquals(30, count(null, null, null));

        assertEquals(6, count(null, RDF.TYPE, vertex));
        assertEquals(0, count(null, RDF.TYPE, edge));

        assertEquals(0, count(null, label, null));
        assertEquals(0, count(null, head, null));
        assertEquals(0, count(null, tail, null));

        // ... absence of other patterns could be tested, as well

        // s ? ?
        assertExpected(get(marko, null, null),
                vf.createStatement(marko, id, vf.createLiteral("1")),
                vf.createStatement(marko, RDF.TYPE, vertex),
                vf.createStatement(marko, age, vf.createLiteral(29)),
                vf.createStatement(marko, name, vf.createLiteral("marko")),
                vf.createStatement(marko, knows, vadas),
                vf.createStatement(marko, created, lop),
                vf.createStatement(marko, knows, josh));

        // s p ?
        assertExpected(get(marko, knows, null),
                vf.createStatement(marko, knows, vadas),
                vf.createStatement(marko, knows, josh));

        // s ? o
        assertExpected(get(marko, null, josh),
                vf.createStatement(marko, knows, josh));

        // s p o
        assertExpected(get(marko, knows, josh),
                vf.createStatement(marko, knows, josh));
        assertExpected(get(marko, name, vf.createLiteral("marko")),
                vf.createStatement(marko, name, vf.createLiteral("marko")));

        // ? ? o
        assertExpected(get(null, null, josh),
                vf.createStatement(marko, knows, josh));

        // ? p o
        assertExpected(get(null, knows, josh),
                vf.createStatement(marko, knows, josh));

        // ? p ?
        assertExpected(get(null, knows, null),
                vf.createStatement(marko, knows, vadas),
                vf.createStatement(marko, knows, josh));
    }

    private long count(final CloseableIteration iter) throws Exception {
        long count = 0;
        try {
            while (iter.hasNext()) {
                count++;
                iter.next();
            }
        } finally {
            iter.close();
        }

        return count;
    }

    private void assertExpected(final Collection<Statement> graph,
                                final Statement... expectedStatements) throws Exception {
        Set<Statement> expected = new TreeSet<Statement>(new StatementComparator());
        for (Statement st : expectedStatements) {
            expected.add(st);
        }
        Set<Statement> actual = new TreeSet<Statement>(new StatementComparator());
        for (Statement st : graph) {
            actual.add(st);
        }
        for (Statement t : expected) {
            if (!actual.contains(t)) {
                fail("expected statement not found: " + t);
            }
        }
        for (Statement t : actual) {
            if (!expected.contains(t)) {
                fail("unexpected statement found: " + t);
            }
        }
    }

    private Collection<Statement> get(final Resource subject,
                                      final URI predicate,
                                      final Value object) throws SailException {
        return toCollection(sc.getStatements(subject, predicate, object, false));
    }

    private int count(final Resource subject,
                                      final URI predicate,
                                      final Value object) throws SailException {
        return toCollection(sc.getStatements(subject, predicate, object, false)).size();
    }

    private Collection<Statement> toCollection(final CloseableIteration<? extends Statement, SailException> i) throws SailException {
        try {
            Collection<Statement> set = new LinkedList<Statement>();
            while (i.hasNext()) {
                set.add(i.next());
            }
            return set;
        } finally {
            i.close();
        }
    }
}
