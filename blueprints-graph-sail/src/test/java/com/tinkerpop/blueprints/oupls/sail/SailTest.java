package com.tinkerpop.blueprints.oupls.sail;

import info.aduna.iteration.CloseableIteration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.sparql.SPARQLParser;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailChangedEvent;
import org.openrdf.sail.SailChangedListener;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailConnectionListener;
import org.openrdf.sail.SailException;
import org.openrdf.sail.inferencer.InferencerConnection;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import static junit.framework.Assert.*;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public abstract class SailTest {
    protected Sail sail = null;
    protected ForwardChainingRDFSInferencer inferencer;

    protected boolean uniqueStatements = false;

    @Before
    public final void setUp() throws Exception {
        before();
        this.sail = createSail();
        sail.initialize();

        if (sail instanceof NotifyingSail) {
            SailConnection sc = sail.getConnection();
            try {
                if (sc instanceof InferencerConnection) {
                    inferencer = new ForwardChainingRDFSInferencer((NotifyingSail) sail);
                }
            } finally {
                sc.rollback();
                sc.close();
            }
        }

        addFile(SailTest.class.getResourceAsStream("graph-example-sail-test.trig"), RDFFormat.TRIG);
    }

    @After
    public final void tearDown() throws Exception {
        sail.shutDown();
        after();
    }

    protected abstract void before() throws Exception;

    protected abstract void after() throws Exception;

    protected abstract Sail createSail() throws Exception;

    // statement manipulation //////////////////////////////////////////////////

    @Test
    public void testGetStatementsS_POG() throws Exception {
        SailConnection sc = sail.getConnection();
        try {
            sc.begin();
            URI uriA = sail.getValueFactory().createURI("http://example.org/test/S_POG#a");
            URI uriB = sail.getValueFactory().createURI("http://example.org/test/S_POG#b");
            URI uriC = sail.getValueFactory().createURI("http://example.org/test/S_POG#c");
            URI uriD = sail.getValueFactory().createURI("http://example.org/test/S_POG#d");
            int before, after;

            // default context, different S,P,O
            sc.removeStatements(uriA, null, null);
            sc.commit();
            sc.begin();
            before = countStatements(sc, uriA, null, null, false);
            sc.addStatement(uriA, uriB, uriC);
            sc.commit();
            sc.begin();
            after = countStatements(sc, uriA, null, null, false);
            assertEquals(0, before);
            System.out.flush();
            assertEquals(1, after);

            // one specific context, different S,P,O
            sc.removeStatements(uriA, null, null, uriD);
            sc.commit();
            sc.begin();
            before = countStatements(sc, uriA, null, null, false, uriD);
            sc.addStatement(uriA, uriB, uriC, uriD);
            sc.commit();
            sc.begin();
            after = countStatements(sc, uriA, null, null, false, uriD);
            assertEquals(0, before);
            assertEquals(1, after);

            // one specific context, same S,P,O,G
            sc.removeStatements(uriA, null, null, uriA);
            sc.commit();
            sc.begin();
            before = countStatements(sc, uriA, null, null, false, uriA);
            sc.addStatement(uriA, uriB, uriC, uriA);
            sc.commit();
            sc.begin();
            after = countStatements(sc, uriA, null, null, false, uriA);
            assertEquals(0, before);
            assertEquals(1, after);

            // default context, same S,P,O
            sc.removeStatements(uriA, null, null);
            sc.commit();
            sc.begin();
            before = countStatements(sc, uriA, null, null, false);
            sc.addStatement(uriA, uriB, uriC);
            sc.commit();
            sc.begin();
            after = countStatements(sc, uriA, null, null, false);
            assertEquals(0, before);
            assertEquals(1, after);
        } finally {
            sc.rollback();
            sc.close();
        }
    }

    @Test
    public void testGetStatementsSP_OG() throws Exception {
        SailConnection sc = sail.getConnection();
        try {
            sc.begin();
            URI uriA = sail.getValueFactory().createURI("http://example.org/test/SP_OG#a");
            URI uriB = sail.getValueFactory().createURI("http://example.org/test/SP_OG#b");
            URI uriC = sail.getValueFactory().createURI("http://example.org/test/SP_OG#c");
            int before, after;

            // Add statement to the implicit null context.
            sc.removeStatements(null, null, null);
            before = countStatements(sc, uriA, uriB, null, false);
            sc.addStatement(uriA, uriB, uriC);
            sc.commit();
            sc.begin();
            after = countStatements(sc, uriA, uriB, null, false);
            assertEquals(0, before);
            assertEquals(1, after);
        } finally {
            sc.rollback();
            sc.close();
        }
    }

    @Test
    public void testGetStatementsO_SPG() throws Exception {
        SailConnection sc = sail.getConnection();
        try {
            sc.begin();
            URI uriA = sail.getValueFactory().createURI("http://example.org/test/O_SPG#a");
            URI uriB = sail.getValueFactory().createURI("http://example.org/test/O_SPG#b");
            URI uriC = sail.getValueFactory().createURI("http://example.org/test/O_SPG#c");
            Literal plainLitA = sail.getValueFactory().createLiteral("arbitrary plain literal 9548734867");
            Literal stringLitA = sail.getValueFactory().createLiteral("arbitrary string literal 8765", XMLSchema.STRING);
            int before, after;

            // Add statement to a specific context.
            sc.removeStatements(null, null, uriA, uriA);
            sc.commit();
            sc.begin();
            before = countStatements(sc, null, null, uriA, false);
            sc.addStatement(uriB, uriC, uriA);
            sc.commit();
            sc.begin();
            after = countStatements(sc, null, null, uriA, false);
            assertEquals(0, before);
            assertEquals(1, after);

            // Add plain literal statement to the default context.
            sc.removeStatements(null, null, plainLitA);
            sc.commit();
            sc.begin();
            before = countStatements(sc, null, null, plainLitA, false);
            sc.addStatement(uriA, uriA, plainLitA);
            sc.commit();
            sc.begin();
            after = countStatements(sc, null, null, plainLitA, false);
            assertEquals(0, before);
            assertEquals(1, after);

            // Add string-typed literal statement to the default context.
            sc.removeStatements(null, null, plainLitA);
            sc.commit();
            sc.begin();
            before = countStatements(sc, null, null, stringLitA, false);
            sc.addStatement(uriA, uriA, stringLitA);
            sc.commit();
            sc.begin();
            after = countStatements(sc, null, null, stringLitA, false);
            assertEquals(0, before);
            assertEquals(1, after);
        } finally {
            sc.rollback();
            sc.close();
        }
    }

    @Test
    public void testGetStatementsPO_SG() throws Exception {
        SailConnection sc = sail.getConnection();
        try {
            sc.begin();
            URI uriA = sail.getValueFactory().createURI("http://example.org/test/PO_SG#a");
            URI uriB = sail.getValueFactory().createURI("http://example.org/test/PO_SG#b");
            URI foo = sail.getValueFactory().createURI("http://example.org/ns#foo");
            URI firstName = sail.getValueFactory().createURI("http://example.org/ns#firstName");
            Literal plainLitA = sail.getValueFactory().createLiteral("arbitrary plain literal 8765675");
            Literal fooLabel = sail.getValueFactory().createLiteral("foo", XMLSchema.STRING);
            int before, after;

            // Add statement to the implicit null context.
            sc.removeStatements(null, null, null, uriA);
            sc.commit();
            sc.begin();
            before = countStatements(sc, null, uriA, uriB, false);
            sc.addStatement(uriA, uriA, uriB);
            sc.commit();
            sc.begin();
            after = countStatements(sc, null, uriA, uriB, false);
            assertEquals(0, before);
            assertEquals(1, after);

            // Add plain literal statement to the default context.
            sc.removeStatements(null, null, plainLitA);
            sc.commit();
            sc.begin();
            before = countStatements(sc, null, uriA, plainLitA, false);
            sc.addStatement(uriA, uriA, plainLitA);
            sc.addStatement(uriA, uriB, plainLitA);
            sc.addStatement(uriB, uriB, plainLitA);
            sc.commit();
            sc.begin();
            after = countStatements(sc, null, uriA, plainLitA, false);
            assertEquals(0, before);
            assertEquals(1, after);

            // Add string-typed literal statement to the default context.
            sc.removeStatements(null, null, fooLabel);
            sc.commit();
            sc.begin();
            before = countStatements(sc, null, firstName, fooLabel, false);
            sc.addStatement(foo, firstName, fooLabel);
            sc.commit();
            sc.begin();
            after = countStatements(sc, null, firstName, fooLabel, false);
            assertEquals(0, before);
            assertEquals(1, after);
            assertEquals(foo, toSet(sc.getStatements(null, firstName, fooLabel, false)).iterator().next().getSubject());

        } finally {
            sc.rollback();
            sc.close();
        }
    }

    @Test
    public void testGetStatementsSPO_G() throws Exception {
        SailConnection sc = sail.getConnection();
        try {
            sc.begin();
            URI uriA = sail.getValueFactory().createURI("http://example.org/test/S_POG#a");
            URI uriB = sail.getValueFactory().createURI("http://example.org/test/S_POG#b");
            URI uriC = sail.getValueFactory().createURI("http://example.org/test/S_POG#c");
            URI uriD = sail.getValueFactory().createURI("http://example.org/test/S_POG#d");
            int before, after;

            // default context, different S,P,O
            sc.removeStatements(uriA, null, null);
            sc.commit();
            sc.begin();
            before = countStatements(sc, uriA, uriB, uriC, false);
            sc.addStatement(uriA, uriB, uriC);
            sc.commit();
            sc.begin();
            after = countStatements(sc, uriA, uriB, uriC, false);
            assertEquals(0, before);
            assertEquals(1, after);

            // default context, same S,P,O
            sc.removeStatements(uriA, null, null);
            sc.commit();
            sc.begin();
            before = countStatements(sc, uriA, uriB, uriC, false);
            sc.addStatement(uriA, uriB, uriC);
            sc.commit();
            sc.begin();
            after = countStatements(sc, uriA, uriB, uriC, false);
            assertEquals(0, before);
            assertEquals(1, after);

            // one specific context, different S,P,O
            sc.removeStatements(uriA, null, null, uriD);
            sc.commit();
            sc.begin();
            before = countStatements(sc, uriA, uriB, uriC, false, uriD);
            sc.addStatement(uriA, uriB, uriC, uriD);
            sc.commit();
            sc.begin();
            after = countStatements(sc, uriA, uriB, uriC, false, uriD);
            assertEquals(0, before);
            assertEquals(1, after);

            // one specific context, same S,P,O,G
            sc.removeStatements(uriA, null, null, uriA);
            sc.commit();
            sc.begin();
            before = countStatements(sc, uriA, uriB, uriC, false, uriA);
            sc.addStatement(uriA, uriB, uriC, uriA);
            sc.commit();
            sc.begin();
            after = countStatements(sc, uriA, uriB, uriC, false, uriA);
            assertEquals(0, before);
            assertEquals(1, after);
        } finally {
            sc.rollback();
            sc.close();
        }
    }

    @Test
    public void testGetStatementsP_SOG() throws Exception {
        SailConnection sc = sail.getConnection();
        try {
            sc.begin();
            URI uriA = sail.getValueFactory().createURI("http://example.org/test/P_SOG#a");
            URI uriB = sail.getValueFactory().createURI("http://example.org/test/P_SOG#b");
            URI uriC = sail.getValueFactory().createURI("http://example.org/test/P_SOG#c");
            URI foo = sail.getValueFactory().createURI("http://example.org/ns#foo");
            URI firstName = sail.getValueFactory().createURI("http://example.org/ns#firstName");
            Literal plainLitA = sail.getValueFactory().createLiteral("arbitrary plain literal 238445");
            Literal fooLabel = sail.getValueFactory().createLiteral("foo", XMLSchema.STRING);
            int before, after;

            // Add statement to the implicit null context.
            sc.removeStatements(null, uriA, null);
            sc.commit();
            sc.begin();
            before = countStatements(sc, null, uriA, null, false);
            sc.addStatement(uriB, uriA, uriC);
            sc.commit();
            sc.begin();
            after = countStatements(sc, null, uriA, null, false);
            assertEquals(0, before);
            assertEquals(1, after);

            // Add plain literal statement to the default context.
            sc.removeStatements(null, uriA, null);
            sc.commit();
            sc.begin();
            before = countStatements(sc, null, uriA, null, false);
            sc.addStatement(uriA, uriA, plainLitA);
            sc.addStatement(uriA, uriB, plainLitA);
            sc.addStatement(uriB, uriB, plainLitA);
            sc.commit();
            sc.begin();
            after = countStatements(sc, null, uriA, null, false);
            assertEquals(0, before);
            assertEquals(1, after);

            // Add string-typed literal statement to the default context.
            sc.removeStatements(null, firstName, null);
            sc.commit();
            sc.begin();
            before = countStatements(sc, null, firstName, null, false);
            sc.addStatement(foo, firstName, fooLabel);
            sc.commit();
            sc.begin();
            after = countStatements(sc, null, firstName, null, false);
            assertEquals(0, before);
            assertEquals(1, after);
            assertEquals(foo, toSet(sc.getStatements(null, firstName, null, false)).iterator().next().getSubject());

            // Add statement to a non-null context.
            sc.removeStatements(null, uriA, null);
            sc.commit();
            sc.begin();
            before = countStatements(sc, null, uriA, null, false);
            sc.addStatement(uriB, uriA, uriC, uriA);
            sc.commit();
            sc.begin();
            after = countStatements(sc, null, uriA, null, false);
            assertEquals(0, before);
            assertEquals(1, after);

            sc.removeStatements(null, uriA, null);
            sc.commit();
            sc.begin();
            before = countStatements(sc, null, uriA, null, false);
            sc.addStatement(uriB, uriA, uriC, uriC);
            sc.addStatement(uriC, uriA, uriA, uriA);
            sc.commit();
            sc.begin();
            sc.addStatement(uriA, uriA, uriB, uriB);
            sc.commit();
            sc.begin();
            after = countStatements(sc, null, uriA, null, false);
            assertEquals(0, before);
            assertEquals(3, after);

        } finally {
            sc.rollback();
            sc.close();
        }
    }

    @Test
    public void testGetStatementsWithVariableContexts() throws Exception {
        SailConnection sc = sail.getConnection();
        try {
            sc.begin();
            URI uriA = sail.getValueFactory().createURI("http://example.org/uriA");
            URI uriB = sail.getValueFactory().createURI("http://example.org/uriB");
            URI uriC = sail.getValueFactory().createURI("http://example.org/uriC");
            int count;
            sc.clear();
            //sc.removeStatements(uriA, uriA, uriA);
            sc.commit();
            sc.begin();
            Resource[] contexts = {uriA, null};
            sc.addStatement(uriA, uriB, uriC, contexts);
            sc.commit();
            sc.begin();

            // Get statements from all contexts.
            count = countStatements(sc, uriA, null, null, false);
            assertEquals(2, count);

            // Get statements from a specific partition context.
            count = countStatements(sc, null, null, null, false, uriA);
            assertEquals(1, count);

            // Get statements from the null context.
            Resource[] c = {null};
            count = countStatements(sc, null, null, null, false, c);
            //assertTrue(count > 0);
            assertEquals(1, count);
            int countLast = count;

            // Get statements from more than one context.
            count = countStatements(sc, null, null, null, false, contexts);
            assertEquals(1 + countLast, count);

        } finally {
            sc.rollback();
            sc.close();
        }
    }

    @Test
    public void testRemoveStatements() throws Exception {
        SailConnection sc = sail.getConnection();
        try {
            sc.begin();
            URI uriA = sail.getValueFactory().createURI("http://example.org/uriA");
            URI uriB = sail.getValueFactory().createURI("http://example.org/uriB");
            URI uriC = sail.getValueFactory().createURI("http://example.org/uriC");
            Resource[] contexts = {uriA, null};
            int count;

            // Remove from all contexts.
            sc.removeStatements(uriA, null, null);
            sc.commit();
            sc.begin();
            count = countStatements(sc, uriA, null, null, false);
            assertEquals(0, count);
            sc.addStatement(uriA, uriB, uriC, contexts);
            sc.commit();
            sc.begin();
            count = countStatements(sc, uriA, null, null, false);
            assertEquals(2, count);
            sc.removeStatements(uriA, null, null);
            sc.commit();
            sc.begin();
            count = countStatements(sc, uriA, null, null, false);
            assertEquals(0, count);

            // Remove from one partition context.
            sc.removeStatements(uriA, null, null);
            sc.commit();
            sc.begin();
            count = countStatements(sc, uriA, null, null, false);
            assertEquals(0, count);
            sc.addStatement(uriA, uriB, uriC, contexts);
            sc.commit();
            sc.begin();
            count = countStatements(sc, uriA, null, null, false);
            assertEquals(2, count);
            Resource[] oneContext = {uriA};
            sc.removeStatements(uriA, null, null, oneContext);
            sc.commit();
            sc.begin();
            count = countStatements(sc, uriA, null, null, false);
            assertEquals(1, count);

            // Remove from the null context.
            sc.removeStatements(uriA, null, null);
            sc.commit();
            sc.begin();
            count = countStatements(sc, uriA, null, null, false);
            assertEquals(0, count);
            sc.addStatement(uriA, uriB, uriC, contexts);
            sc.commit();
            sc.begin();
            count = countStatements(sc, uriA, null, null, false);
            assertEquals(2, count);
            Resource[] nullContext = {null};
            sc.removeStatements(uriA, null, null, nullContext);
            sc.commit();
            sc.begin();
            count = countStatements(sc, uriA, null, null, false);
            assertEquals(1, count);

            // Remove from more than one context.
            sc.removeStatements(uriA, null, null);
            sc.commit();
            sc.begin();
            count = countStatements(sc, uriA, null, null, false);
            assertEquals(0, count);
            sc.addStatement(uriA, uriB, uriC, contexts);
            sc.commit();
            sc.begin();
            count = countStatements(sc, uriA, null, null, false);
            assertEquals(2, count);
            sc.removeStatements(uriA, null, null);
            sc.commit();
            sc.begin();
            count = countStatements(sc, uriA, null, null, false, contexts);
            assertEquals(0, count);

        } finally {
            sc.rollback();
            sc.close();
        }
    }

    @Test
    public void testClear() throws Exception {
        URI uriA = sail.getValueFactory().createURI("http://example.org/uriA");
        URI uriB = sail.getValueFactory().createURI("http://example.org/uriB");
        URI uriC = sail.getValueFactory().createURI("http://example.org/uriC");

        SailConnection sc = sail.getConnection();
        try {
            sc.begin();
            sc.clear();
            assertEquals(0L, sc.size());
            sc.addStatement(uriA, uriB, uriC, uriA);
            sc.addStatement(uriC, uriA, uriB, uriA);
            sc.addStatement(uriB, uriC, uriA, uriA);
            assertEquals(3L, sc.size(uriA));
            sc.addStatement(uriA, uriB, uriC, uriB);
            sc.addStatement(uriB, uriC, uriA, uriB);
            assertEquals(2L, sc.size(uriB));
            sc.addStatement(uriA, uriB, uriC);
            assertEquals(1L, sc.size((Resource) null));
            sc.addStatement(uriA, uriB, uriC, uriC);
            sc.addStatement(uriB, uriC, uriA, uriC);
            sc.addStatement(uriC, uriA, uriB, uriC);
            sc.addStatement(uriA, uriB, uriB, uriC);
            assertEquals(4L, sc.size(uriC));
            assertEquals(10L, sc.size());
            sc.clear(uriA, uriC);
            assertEquals(1L, sc.size((Resource) null));
            assertEquals(0L, sc.size(uriA));
            assertEquals(2L, sc.size(uriB));
            assertEquals(0L, sc.size(uriC));
            assertEquals(3L, sc.size());
            sc.clear();
            assertEquals(0L, sc.size());
            sc.commit();
        } finally {
            sc.rollback();
            sc.close();
        }
    }

    @Test
    public void testGetContextIDs() throws Exception {
        // TODO
    }

    @Test
    public void testSize() throws Exception {
        URI uriA = sail.getValueFactory().createURI("http://example.org/uriA");
        URI uriB = sail.getValueFactory().createURI("http://example.org/uriB");
        URI uriC = sail.getValueFactory().createURI("http://example.org/uriC");

        SailConnection sc = sail.getConnection();
        try {
            sc.begin();
            sc.removeStatements(null, null, null);

            assertEquals(0L, sc.size());
            sc.addStatement(uriA, uriB, uriC, uriA);
            // sc.commit();
            assertEquals(1L, sc.size());
            sc.addStatement(uriA, uriB, uriC, uriB);
            // sc.commit();
            assertEquals(2L, sc.size());
            sc.addStatement(uriB, uriB, uriC, uriB);
            // sc.commit();
            assertEquals(3L, sc.size());
            sc.addStatement(uriC, uriB, uriA);
            // sc.commit();
            assertEquals(4L, sc.size());
            assertEquals(1L, sc.size(uriA));
            assertEquals(2L, sc.size(uriB));
            assertEquals(0L, sc.size(uriC));
            assertEquals(1L, sc.size((URI) null));
            assertEquals(3L, sc.size(uriB, null));
            assertEquals(3L, sc.size(uriB, uriC, null));
            assertEquals(4L, sc.size(uriA, uriB, null));
            assertEquals(4L, sc.size(uriA, uriB, uriC, null));
            assertEquals(3L, sc.size(uriA, uriB));
            sc.commit();
        } finally {
            sc.rollback();
            sc.close();
        }
    }

    @Test
    public void testDuplicateStatements() throws Exception {
        if (uniqueStatements) {
            URI uriA = sail.getValueFactory().createURI("http://example.org/uriA");
            URI uriB = sail.getValueFactory().createURI("http://example.org/uriB");
            URI uriC = sail.getValueFactory().createURI("http://example.org/uriC");
            SailConnection sc = sail.getConnection();
            try {
                sc.begin();
                sc.clear();
                assertEquals(0, countStatements(sc, uriA, uriB, uriC, false));
                sc.addStatement(uriA, uriB, uriC);
                assertEquals(1, countStatements(sc, uriA, uriB, uriC, false));
                sc.addStatement(uriA, uriB, uriC);
                assertEquals(1, countStatements(sc, uriA, uriB, uriC, false));

                sc.addStatement(uriA, uriB, uriC, uriC);
                assertEquals(2, countStatements(sc, uriA, uriB, uriC, false));
                assertEquals(1, countStatements(sc, uriA, uriB, uriC, false, uriC));
                sc.commit();
            } finally {
                sc.rollback();
                sc.close();
            }
        }
    }

    // URIs ////////////////////////////////////////////////////////////////////

    // literals ////////////////////////////////////////////////////////////////

    // Note: this test will always pass as long as we're using ValueFactoryImpl

    @Test
    public void testCreateLiteralsThroughValueFactory() throws Exception {
        Literal l;
        ValueFactory vf = sail.getValueFactory();

        l = vf.createLiteral("a plain literal");
        assertNotNull(l);
        assertNull(l.getLanguage());
        assertEquals("a plain literal", l.getLabel());
        assertNull(l.getDatatype());
        l = vf.createLiteral("auf Deutsch, bitte", "de");
        assertNotNull(l);
        assertEquals("de", l.getLanguage());
        assertEquals("auf Deutsch, bitte", l.getLabel());
        assertNull(l.getDatatype());

        // Test data-typed createLiteral methods
        l = vf.createLiteral("foo", XMLSchema.STRING);
        assertNotNull(l);
        assertNull(l.getLanguage());
        assertEquals("foo", l.getLabel());
        assertEquals(XMLSchema.STRING, l.getDatatype());
        l = vf.createLiteral(42);
        assertNotNull(l);
        assertNull(l.getLanguage());
        assertEquals("42", l.getLabel());
        assertEquals(42, l.intValue());
        assertEquals(XMLSchema.INT, l.getDatatype());
        l = vf.createLiteral(42l);
        assertNotNull(l);
        assertNull(l.getLanguage());
        assertEquals("42", l.getLabel());
        assertEquals(42l, l.longValue());
        assertEquals(XMLSchema.LONG, l.getDatatype());
        l = vf.createLiteral((short) 42);
        assertNotNull(l);
        assertNull(l.getLanguage());
        assertEquals("42", l.getLabel());
        assertEquals((short) 42, l.shortValue());
        assertEquals(XMLSchema.SHORT, l.getDatatype());
        l = vf.createLiteral(true);
        assertNotNull(l);
        assertNull(l.getLanguage());
        assertEquals("true", l.getLabel());
        assertEquals(true, l.booleanValue());
        assertEquals(XMLSchema.BOOLEAN, l.getDatatype());
        l = vf.createLiteral((byte) 'c');
        assertNotNull(l);
        assertNull(l.getLanguage());
        assertEquals("99", l.getLabel());
        assertEquals((byte) 'c', l.byteValue());
        assertEquals(XMLSchema.BYTE, l.getDatatype());
        XMLGregorianCalendar calendar = XMLDatatypeUtil.parseCalendar("2002-10-10T12:00:00-05:00");
        l = vf.createLiteral(calendar);
        assertNotNull(l);
        assertNull(l.getLanguage());
        assertEquals("2002-10-10T12:00:00-05:00", l.getLabel());
        assertEquals(calendar, l.calendarValue());
        assertEquals(XMLSchema.DATETIME, l.getDatatype());
    }

    @Test
    public void testGetLiteralsFromTripleStore() throws Exception {
        Literal l;
        String prefix = "urn:com.tinkerpop.blueprints.pgm.oupls.sail.test/";
        XMLGregorianCalendar calendar;
        ValueFactory vf = sail.getValueFactory();
        SailConnection sc = sail.getConnection();

        // Get an actual plain literal from the triple store.
        URI ford = vf.createURI(prefix + "ford");
        l = (Literal) toSet(sc.getStatements(ford, RDFS.COMMENT, null, false)).iterator().next().getObject();
        assertNotNull(l);
        assertNull(l.getLanguage());
        assertEquals("he really knows where his towel is", l.getLabel());
        assertNull(l.getDatatype());
        URI thor = vf.createURI(prefix + "thor");

        // Get an actual language-tagged literal from the triple store.
        URI foafName = vf.createURI("http://xmlns.com/foaf/0.1/name");
        Iterator<Statement> iter = toSet(sc.getStatements(thor, foafName, null, false)).iterator();
        boolean found = false;
        while (iter.hasNext()) {
            l = (Literal) iter.next().getObject();
            if (l.getLanguage().equals("en")) {
                found = true;
                assertEquals("Thor", l.getLabel());
                assertNull(l.getDatatype());
            }
            // if (l.getLanguage().equals("is")) {
            // found = true;
            // assertEquals("?�r", l.getLabel());
            // }
        }
        assertTrue(found);

        // Get an actual data-typed literal from the triple-store.
        URI msnChatID = vf.createURI("http://xmlns.com/foaf/0.1/msnChatID");
        l = (Literal) toSet(sc.getStatements(thor, msnChatID, null, false)).iterator().next().getObject();
        assertNotNull(l);
        assertNull(l.getLanguage());
        assertEquals("Thorster123", l.getLabel());
        assertEquals(XMLSchema.STRING, l.getDatatype());

        // Test Literal.xxxValue() methods for Literals read from the triple
        // store
        URI valueUri, hasValueUri;
        hasValueUri = vf.createURI(prefix + "hasValue");
        valueUri = vf.createURI(prefix + "stringValue");
        l = (Literal) toSet(sc.getStatements(valueUri, hasValueUri, null, false)).iterator().next().getObject();
        assertNotNull(l);
        assertNull(l.getLanguage());
        assertEquals("foo", l.getLabel());
        assertEquals(XMLSchema.STRING, l.getDatatype());
        valueUri = vf.createURI(prefix + "byteValue");
        l = (Literal) toSet(sc.getStatements(valueUri, hasValueUri, null, false)).iterator().next().getObject();
        assertNotNull(l);
        assertNull(l.getLanguage());
        assertEquals("99", l.getLabel());
        assertEquals(XMLSchema.BYTE, l.getDatatype());
        assertEquals((byte) 'c', l.byteValue());
        valueUri = vf.createURI(prefix + "booleanValue");
        l = (Literal) toSet(sc.getStatements(valueUri, hasValueUri, null, false)).iterator().next().getObject();
        assertNotNull(l);
        assertNull(l.getLanguage());
        assertEquals("false", l.getLabel());
        assertEquals(XMLSchema.BOOLEAN, l.getDatatype());
        assertEquals(false, l.booleanValue());
        valueUri = vf.createURI(prefix + "intValue");
        l = (Literal) toSet(sc.getStatements(valueUri, hasValueUri, null, false)).iterator().next().getObject();
        assertNotNull(l);
        assertNull(l.getLanguage());
        assertEquals("42", l.getLabel());
        assertEquals(XMLSchema.INT, l.getDatatype());
        assertEquals(42, l.intValue());
        valueUri = vf.createURI(prefix + "shortValue");
        l = (Literal) toSet(sc.getStatements(valueUri, hasValueUri, null, false)).iterator().next().getObject();
        assertNotNull(l);
        assertNull(l.getLanguage());
        assertEquals("42", l.getLabel());
        assertEquals(XMLSchema.SHORT, l.getDatatype());
        assertEquals((short) 42, l.shortValue());
        valueUri = vf.createURI(prefix + "longValue");
        l = (Literal) toSet(sc.getStatements(valueUri, hasValueUri, null, false)).iterator().next().getObject();
        assertNotNull(l);
        assertNull(l.getLanguage());
        assertEquals("42", l.getLabel());
        assertEquals(XMLSchema.LONG, l.getDatatype());
        assertEquals(42l, l.longValue());
        valueUri = vf.createURI(prefix + "floatValue");
        l = (Literal) toSet(sc.getStatements(valueUri, hasValueUri, null, false)).iterator().next().getObject();
        assertNotNull(l);
        assertNull(l.getLanguage());
        assertEquals("3.1415926", l.getLabel());
        assertEquals(XMLSchema.FLOAT, l.getDatatype());
        assertEquals((float) 3.1415926, l.floatValue());
        valueUri = vf.createURI(prefix + "doubleValue");
        l = (Literal) toSet(sc.getStatements(valueUri, hasValueUri, null, false)).iterator().next().getObject();
        assertNotNull(l);
        assertNull(l.getLanguage());
        assertEquals("3.1415926", l.getLabel());
        assertEquals(XMLSchema.DOUBLE, l.getDatatype());
        assertEquals(3.1415926, l.doubleValue());
        valueUri = vf.createURI(prefix + "dateTimeValue");
        calendar = XMLDatatypeUtil.parseCalendar("2002-10-10T12:00:00-05:00");
        l = (Literal) toSet(sc.getStatements(valueUri, hasValueUri, null, false)).iterator().next().getObject();
        assertNotNull(l);
        assertNull(l.getLanguage());
        assertEquals("2002-10-10T12:00:00-05:00", l.getLabel());
        assertEquals(XMLSchema.DATETIME, l.getDatatype());
        assertEquals(calendar, l.calendarValue());
        sc.rollback();
        sc.close();
    }

    // blank nodes /////////////////////////////////////////////////////////////

    @Test
    public void testBlankNodes() throws Throwable {
        URI uriA = sail.getValueFactory().createURI("http://example.org/test/S_POG#a");
        URI uriB = sail.getValueFactory().createURI("http://example.org/test/S_POG#b");
        SailConnection sc = sail.getConnection();
        try {
            sc.begin();
            ValueFactory factory = sail.getValueFactory();
            BNode bNode = factory.createBNode();
            try {
                sc.addStatement(uriA, uriA, bNode);
            } catch (SailException se) {
                // FIXME: not supporting blank nodes ATM
                assertTrue(se.getCause() instanceof UnsupportedOperationException);
            }
            sc.commit();
        } finally {
            sc.rollback();
            sc.close();
        }
    }

    // tuple queries ///////////////////////////////////////////////////////////

    @Test
    public void testEvaluate() throws Exception {
        Set<String> languages;
        String prefix = "urn:com.tinkerpop.blueprints.pgm.oupls.sail.test/";
        URI thorUri = sail.getValueFactory().createURI(prefix + "thor");

        SailConnection sc = sail.getConnection();
        try {
            sc.begin();
            URI uriA = sail.getValueFactory().createURI("http://example.org/uriA");
            URI uriB = sail.getValueFactory().createURI("http://example.org/uriB");
            URI uriC = sail.getValueFactory().createURI("http://example.org/uriC");
            sc.addStatement(uriA, uriB, uriC);
            sc.commit();
            sc.begin();

            SPARQLParser parser = new SPARQLParser();
            BindingSet bindings = new EmptyBindingSet();
            String baseURI = "http://example.org/bogus/";
            String queryStr;
            ParsedQuery query;
            CloseableIteration<? extends BindingSet, QueryEvaluationException> results;
            int count;
            // s ?p ?o SELECT
            queryStr = "SELECT ?y ?z WHERE { <http://example.org/uriA> ?y ?z }";
            query = parser.parseQuery(queryStr, baseURI);
            results = sc.evaluate(query.getTupleExpr(), query.getDataset(), bindings, false);
            count = 0;
            while (results.hasNext()) {
                count++;
                BindingSet set = results.next();
                URI y = (URI) set.getValue("y");
                Value z = (Value) set.getValue("z");
                assertNotNull(y);
                assertNotNull(z);
                // System.out.println("y = " + y + ", z = " + z);
            }
            results.close();
            assertTrue(count > 0);

            // s p ?o SELECT using a namespace prefix
            queryStr = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + "SELECT ?z WHERE { <" + prefix + "thor> foaf:name ?z }";
            query = parser.parseQuery(queryStr, baseURI);
            results = sc.evaluate(query.getTupleExpr(), query.getDataset(), bindings, false);
            count = 0;
            languages = new HashSet<String>();
            while (results.hasNext()) {
                count++;
                BindingSet set = results.next();
                Literal z = (Literal) set.getValue("z");
                assertNotNull(z);
                languages.add(z.getLanguage());
            }
            results.close();
            assertTrue(count > 0);
            assertEquals(2, languages.size());
            assertTrue(languages.contains("en"));
            assertTrue(languages.contains("is"));

            // ?s p o SELECT using a plain literal value with no language tag
            queryStr = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + "SELECT ?s WHERE { ?s rdfs:comment \"he really knows where his towel is\" }";
            URI fordUri = sail.getValueFactory().createURI(prefix + "ford");
            query = parser.parseQuery(queryStr, baseURI);
            results = sc.evaluate(query.getTupleExpr(), query.getDataset(), bindings, false);
            count = 0;
            while (results.hasNext()) {
                count++;
                BindingSet set = results.next();
                URI s = (URI) set.getValue("s");
                assertNotNull(s);
                assertEquals(s, fordUri);
            }
            results.close();
            assertTrue(count > 0);

            // ?s p o SELECT using a language-specific literal value
            queryStr = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + "SELECT ?s WHERE { ?s foaf:name \"Thor\"@en }";
            query = parser.parseQuery(queryStr, baseURI);
            results = sc.evaluate(query.getTupleExpr(), query.getDataset(), bindings, false);
            count = 0;
            while (results.hasNext()) {
                count++;
                BindingSet set = results.next();
                URI s = (URI) set.getValue("s");
                assertNotNull(s);
                assertEquals(s, thorUri);
            }
            results.close();
            assertTrue(count > 0);

            // The language tag is necessary
            queryStr = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + "SELECT ?s WHERE { ?s foaf:name \"Thor\" }";
            query = parser.parseQuery(queryStr, baseURI);
            results = sc.evaluate(query.getTupleExpr(), query.getDataset(), bindings, false);
            count = 0;
            while (results.hasNext()) {
                count++;
                results.next();
            }
            results.close();
            assertEquals(0, count);

            // ?s p o SELECT using a typed literal value
            queryStr = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" + "SELECT ?s WHERE { ?s foaf:msnChatID \"Thorster123\"^^xsd:string }";
            query = parser.parseQuery(queryStr, baseURI);
            results = sc.evaluate(query.getTupleExpr(), query.getDataset(), bindings, false);
            count = 0;
            while (results.hasNext()) {
                count++;
                BindingSet set = results.next();
                URI s = (URI) set.getValue("s");
                assertNotNull(s);
                assertEquals(s, thorUri);
            }
            results.close();
            assertTrue(count > 0);

            // The data type is necessary
            queryStr = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" + "SELECT ?s WHERE { ?s foaf:msnChatID \"Thorster123\" }";
            query = parser.parseQuery(queryStr, baseURI);
            results = sc.evaluate(query.getTupleExpr(), query.getDataset(), bindings, false);
            count = 0;
            while (results.hasNext()) {
                count++;
                results.next();
            }
            results.close();
            assertEquals(0, count);

            // s ?p o SELECT
            // TODO: commented out languages for now
            queryStr = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" + "SELECT ?p WHERE { <" + prefix + "thor> ?p \"Thor\"@en }";
            query = parser.parseQuery(queryStr, baseURI);
            URI foafNameUri = sail.getValueFactory().createURI("http://xmlns.com/foaf/0.1/name");
            results = sc.evaluate(query.getTupleExpr(), query.getDataset(), bindings, false);
            count = 0;
            while (results.hasNext()) {
                count++;
                BindingSet set = results.next();
                URI p = (URI) set.getValue("p");
                assertNotNull(p);
                assertEquals(p, foafNameUri);
            }
            results.close();
            assertTrue(count > 0);

            // context-specific SELECT
            queryStr = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + "SELECT ?z\n" + "FROM <" + prefix + "ctx1>\n" + "WHERE { <" + prefix + "thor> foaf:name ?z }";
            query = parser.parseQuery(queryStr, baseURI);
            results = sc.evaluate(query.getTupleExpr(), query.getDataset(), bindings, false);
            count = 0;
            languages = new HashSet<String>();
            while (results.hasNext()) {
                count++;
                BindingSet set = results.next();
                Literal z = (Literal) set.getValue("z");
                assertNotNull(z);
                languages.add(z.getLanguage());
            }
            results.close();
            assertTrue(count > 0);
            assertEquals(2, languages.size());
            assertTrue(languages.contains("en"));
            assertTrue(languages.contains("is"));
            queryStr = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + "SELECT ?z\n" + "FROM <http://example.org/emptycontext>\n" + "WHERE { <" + prefix + "thor> foaf:name ?z }";
            query = parser.parseQuery(queryStr, baseURI);
            results = sc.evaluate(query.getTupleExpr(), query.getDataset(), bindings, false);
            count = 0;
            while (results.hasNext()) {
                count++;
                results.next();
            }
            results.close();
            assertEquals(0, count);

            // s p o? select without and with inferencing
            // TODO commented out waiting for inferencing
            // queryStr =
            // "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
            // + "SELECT ?o\n"
            // + "WHERE { <" + prefix + "instance1> rdf:type ?o }";
            // query = parser.parseQuery(queryStr, baseURI);
            // results = sc.evaluate(query.getTupleExpr(), query.getDataset(),
            // bindings, false);
            // count = 0;
            // while (results.hasNext()) {
            // count++;
            // BindingSet set = results.next();
            // URI o = (URI) set.getValue("o");
            // assertEquals(prefix + "classB", o.toString());
            // }
            // results.close();
            // assertEquals(1, count);
            // results = sc.evaluate(query.getTupleExpr(), query.getDataset(),
            // bindings, true);
            // count = 0;
            // boolean foundA = false, foundB = false;
            // while (results.hasNext()) {
            // count++;
            // BindingSet set = results.next();
            // URI o = (URI) set.getValue("o");
            // String s = o.toString();
            // if (s.equals(prefix + "classA")) {
            // foundA = true;
            // } else if (s.equals(prefix + "classB")) {
            // foundB = true;
            // }
            // }
            // results.close();
            // assertEquals(2, count);
            // assertTrue(foundA);
            // assertTrue(foundB);

        } finally {
            sc.rollback();
            sc.close();
        }
    }

    @Test
    public void testJoins() throws Exception {
        SPARQLParser parser = new SPARQLParser();
        BindingSet bindings = new EmptyBindingSet();
        String baseURI = "http://example.org/bogus/";

        SailConnection sc = sail.getConnection();
        try {
            CloseableIteration<? extends BindingSet, QueryEvaluationException> results;
            int count;
            String queryStr = "PREFIX : <urn:com.tinkerpop.blueprints.pgm.oupls.sail.test/>\n" +
                    "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                    "SELECT ?foaf WHERE {\n" +
                    "    :ford foaf:knows ?friend .\n" +
                    "    ?friend foaf:knows ?foaf .\n" +
                    "}";
            ParsedQuery query = parser.parseQuery(queryStr, baseURI);
            results = sc.evaluate(query.getTupleExpr(), query.getDataset(), bindings, false);
            count = 0;
            while (results.hasNext()) {
                count++;
                BindingSet set = results.next();
                URI foaf = (URI) set.getValue("foaf");
                assertTrue(foaf.stringValue().startsWith("urn:com.tinkerpop.blueprints.pgm.oupls.sail.test/"));
            }
            results.close();
            assertEquals(4, count);
        } finally {
            sc.rollback();
            sc.close();
        }
    }

    // listeners ///////////////////////////////////////////////////////////////
    // (disabled for Sails which do not implement NotifyingSail)

    @Test
    public void testSailConnectionListeners() throws Exception {
        if (sail instanceof NotifyingSail) {
            URI uriA = sail.getValueFactory().createURI("http://example.org/uriA");
            URI uriB = sail.getValueFactory().createURI("http://example.org/uriB");
            URI uriC = sail.getValueFactory().createURI("http://example.org/uriC");

            TestListener listener1 = new TestListener(), listener2 = new TestListener();
            NotifyingSailConnection sc = ((NotifyingSail) sail).getConnection();
            try {
                sc.begin();
                sc.clear();
                sc.commit();
                sc.begin();

                // Add a listener and add statements
                sc.addConnectionListener(listener1);
                sc.addStatement(uriA, uriB, uriC, uriA);
                sc.addStatement(uriB, uriC, uriA, uriA);
                sc.commit();
                sc.begin();

                // Add another listener and remove a statement
                sc.addConnectionListener(listener2);
                sc.removeStatements(uriA, null, null);
                sc.commit();
                sc.begin();

                assertEquals(2, listener1.getAdded());
                assertEquals(0, listener2.getAdded());
                assertEquals(1, listener1.getRemoved());
                assertEquals(1, listener2.getRemoved());

                // Remove a listener and clear
                sc.removeConnectionListener(listener1);
                sc.clear();
                sc.commit();
                sc.begin();

                assertEquals(1, listener1.getRemoved());
                assertEquals(2, listener2.getRemoved());
            } finally {
                sc.rollback();
                sc.close();
            }
        }
    }

    @Test
    public void testSailChangedListeners() throws Exception {
        if (sail instanceof NotifyingSail) {
            final Collection<SailChangedEvent> events = new LinkedList<SailChangedEvent>();
            SailChangedListener listener = new SailChangedListener() {
                public void sailChanged(final SailChangedEvent event) {
                    events.add(event);
                }
            };
            ((NotifyingSail) sail).addSailChangedListener(listener);
            URI uriA = sail.getValueFactory().createURI("http://example.org/uriA");
            URI uriB = sail.getValueFactory().createURI("http://example.org/uriB");
            URI uriC = sail.getValueFactory().createURI("http://example.org/uriC");
            SailConnection sc = sail.getConnection();
            try {
                sc.begin();
                sc.clear();
                sc.commit();
                sc.begin();
                events.clear();
                assertEquals(0, events.size());
                sc.addStatement(uriA, uriB, uriC, uriA);
                sc.addStatement(uriB, uriC, uriA, uriA);
                // Events are buffered until the commit
                assertEquals(0, events.size());
                sc.commit();
                sc.begin();
                // Only one SailChangedEvent per commit
                assertEquals(1, events.size());
                SailChangedEvent event = events.iterator().next();
                assertTrue(event.statementsAdded());
                assertFalse(event.statementsRemoved());
                events.clear();
                assertEquals(0, events.size());
                sc.removeStatements(uriA, uriB, uriC, uriA);
                sc.commit();
                sc.begin();
                assertEquals(1, events.size());
                event = events.iterator().next();
                assertFalse(event.statementsAdded());
                assertTrue(event.statementsRemoved());
                events.clear();
                assertEquals(0, events.size());
                sc.clear();
                sc.commit();
                sc.begin();
                assertEquals(1, events.size());
                event = events.iterator().next();
                assertFalse(event.statementsAdded());
                assertTrue(event.statementsRemoved());
            } finally {
                sc.rollback();
                sc.close();
            }
        }
    }

    // namespaces //////////////////////////////////////////////////////////////

    @Test
    public void testClearNamespaces() throws Exception {
        SailConnection sc = sail.getConnection();
        try {
            CloseableIteration<? extends Namespace, SailException> namespaces;
            int count;
            count = 0;
            namespaces = sc.getNamespaces();
            while (namespaces.hasNext()) {
                namespaces.next();
                count++;
            }
            namespaces.close();
            assertTrue(count > 0);
            // TODO: actually clear namespaces (but this wipes them out for
            // subsequent tests)
        } finally {
            sc.rollback();
            sc.close();
        }
    }

    @Test
    public void testGetNamespace() throws Exception {
        SailConnection sc = sail.getConnection();
        try {
            // FIXME: temporary
            //sc.setNamespace("foo", "http://example.org/foo/");
            //showNamespaces(sc);

            String name;
            name = sc.getNamespace("bogus");
            assertNull(name);
            // assertEquals(name, "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
            name = sc.getNamespace("rdfs");
            //sc.commit();
            assertEquals(name, "http://www.w3.org/2000/01/rdf-schema#");
        } finally {
            sc.rollback();
            sc.close();
        }
    }

    private void showNamespaces(final SailConnection c) throws SailException {
        System.out.println("namespaces:");
        CloseableIteration<? extends Namespace, SailException> iter = c.getNamespaces();
        try {
            while (iter.hasNext()) {
                Namespace n = iter.next();
                System.out.println("\t" + n.getPrefix() + ":\t" + n.getName());
            }
        } finally {
            iter.close();
        }
    }

    @Test
    public void testGetNamespaces() throws Exception {
        SailConnection sc = sail.getConnection();
        try {
            sc.begin();
            CloseableIteration<? extends Namespace, SailException> namespaces;
            int before = 0, during = 0, after = 0;
            // just iterate through all namespaces
            namespaces = sc.getNamespaces();
            while (namespaces.hasNext()) {
                Namespace ns = namespaces.next();
                before++;
                // System.out.println("namespace: " + ns);
            }
            namespaces.close();
            // Note: assumes that these namespace prefixes are unused.
            int nTests = 10;
            String prefixPrefix = "testns";
            String namePrefix = "http://example.org/test";
            for (int i = 0; i < nTests; i++) {
                sc.setNamespace(prefixPrefix + i, namePrefix + i);
            }
            sc.commit();
            sc.begin();
            namespaces = sc.getNamespaces();
            while (namespaces.hasNext()) {
                Namespace ns = namespaces.next();
                during++;
                String prefix = ns.getPrefix();
                String name = ns.getName();
                if (prefix.startsWith(prefixPrefix)) {
                    assertEquals(name, namePrefix + prefix.substring(prefixPrefix.length()));
                }
            }
            namespaces.close();
            for (int i = 0; i < nTests; i++) {
                sc.removeNamespace(prefixPrefix + i);
            }
            sc.commit();
            sc.begin();
            namespaces = sc.getNamespaces();
            while (namespaces.hasNext()) {
                namespaces.next();
                after++;
            }
            namespaces.close();
            assertEquals(during, before + nTests);
            assertEquals(after, before);
        } finally {
            sc.rollback();
            sc.close();
        }
    }

    @Test
    public void testSetNamespace() throws Exception {
        SailConnection sc = sail.getConnection();
        try {
            sc.begin();
            String prefix = "foo";
            String emptyPrefix = "";
            String name = "http://example.org/foo";
            String otherName = "http://example.org/bar";

            sc.removeNamespace(prefix);
            sc.removeNamespace(emptyPrefix);
            sc.commit();
            sc.begin();

            // Namespace initially absent?
            assertNull(sc.getNamespace(prefix));
            assertNull(sc.getNamespace(emptyPrefix));

            // Can we set the namespace?
            sc.setNamespace(prefix, name);
            sc.commit();
            sc.begin();
            assertEquals(sc.getNamespace(prefix), name);

            // Can we reset the namespace?
            sc.setNamespace(prefix, otherName);
            sc.commit();
            sc.begin();
            assertEquals(sc.getNamespace(prefix), otherName);

            // Can we use an empty namespace prefix?
            sc.setNamespace(emptyPrefix, name);
            sc.commit();
            sc.begin();
            assertEquals(sc.getNamespace(emptyPrefix), name);
        } finally {
            sc.rollback();
            sc.close();
        }
    }

    @Test
    public void testRemoveNamespace() throws Exception {
        SailConnection sc = sail.getConnection();
        try {
            sc.begin();
            String prefix = "foo";
            String emptyPrefix = "";
            String name = "http://example.org/foo";

            // Set namespace initially.
            sc.setNamespace(prefix, name);
            sc.commit();
            sc.begin();
            assertEquals(sc.getNamespace(prefix), name);

            // Remove the namespace and make sure it's gone.
            sc.removeNamespace(prefix);
            sc.commit();
            sc.begin();
            assertNull(sc.getNamespace(prefix));

            // Same thing for the default namespace.
            sc.setNamespace(emptyPrefix, name);
            sc.commit();
            sc.begin();
            assertEquals(sc.getNamespace(emptyPrefix), name);
            sc.removeNamespace(emptyPrefix);
            sc.commit();
            sc.begin();
            assertNull(sc.getNamespace(emptyPrefix));
        } finally {
            sc.rollback();
            sc.close();
        }
    }

    // connections and transactions ////////////////////////////////////////////

    @Test
    public void testPersistentCommits() throws Exception {
        SailConnection sc;
        int count;
        URI uriA = sail.getValueFactory().createURI("http://example.org/test/persistentCommits#a");
        URI uriB = sail.getValueFactory().createURI("http://example.org/test/persistentCommits#b");
        URI uriC = sail.getValueFactory().createURI("http://example.org/test/persistentCommits#c");
        sc = sail.getConnection();
        try {
            sc.begin();
            count = countStatements(sc, uriA, null, null, false);
            assertEquals(0, count);
            sc.rollback();
            sc.close();

            sc = sail.getConnection();
            sc.begin();
            sc.addStatement(uriA, uriB, uriC);
            count = countStatements(sc, uriA, null, null, false);
            assertEquals(1, count);
            sc.commit();
            sc.close();

            sc = sail.getConnection();
            sc.begin();
            count = countStatements(sc, uriA, null, null, false);
            assertEquals(1, count);
            sc.rollback();
            sc.close();

            sc = sail.getConnection();
            sc.begin();
            sc.removeStatements(uriA, null, null);
            count = countStatements(sc, uriA, null, null, false);
            assertEquals(0, count);
            sc.commit();
            sc.close();

            sc = sail.getConnection();
            sc.begin();
            count = countStatements(sc, uriA, null, null, false);
            assertEquals(0, count);
        } finally {
            sc.rollback();
            sc.close();
        }
    }

    /*
    // TODO: restore me... but only for ACID-compliant stores
    @Ignore
    @Test
    public void testVisibilityOfChanges() throws Exception {
        SailConnection sc1, sc2;
        int count;
        URI uriA = sail.getValueFactory().createURI(
                "http://example.org/test/visibilityOfChanges#a");
        sc1 = sail.getConnection();
        sc2 = sail.getConnection();
        try {
            sc1.clear();
            sc2.clear();

            // Statement doesn't exist for either connection.
            count = countStatements(sc1, uriA, null, null);
            assertEquals(0, count);
            count = countStatements(sc2, uriA, null, null);
            assertEquals(0, count);
            // First connection adds a statement. It is visible to the first
            // connection, but not to the second.
            sc1.addStatement(uriA, uriA, uriA);
            count = countStatements(sc1, null, null, null);
            assertEquals(1, count);
            count = countStatements(sc2, uriA, null, null);
            assertEquals(0, count);
            // ...
        }
        finally {
            sc2.close();
            sc1.close();
        }
    }
    */

    @Test
    public void testNullContext() throws Exception {
        URI uriA = sail.getValueFactory().createURI("http://example.org/test/nullContext#a");
        URI uriB = sail.getValueFactory().createURI("http://example.org/test/nullContext#b");
        URI uriC = sail.getValueFactory().createURI("http://example.org/test/nullContext#c");
        int count = 0;

        SailConnection sc = sail.getConnection();
        try {
            sc.begin();
            count = countStatements(sc, uriA, null, null, false);
            assertEquals(0, count);
            sc.addStatement(uriA, uriB, uriC);
            Statement statement = sc.getStatements(uriA, uriB, uriC, false, new Resource[]{null}).next();
            Resource context = statement.getContext();
            assertNull(context);
            sc.removeStatements(uriA, null, null);
            sc.commit();
        } finally {
            sc.rollback();
            sc.close();
        }
    }

    // inference ////////////////////////////////////////////////////////////////

    public void testInference() throws Exception {
        if (null != inferencer) {
            URI uriA = sail.getValueFactory().createURI("http://example.org/uriA");
            URI uriB = sail.getValueFactory().createURI("http://example.org/uriB");
            URI classX = sail.getValueFactory().createURI("http://example.org/classX");
            URI classY = sail.getValueFactory().createURI("http://example.org/classY");

            SailConnection sc = inferencer.getConnection();
            try {
                sc.clear();

                sc.addStatement(classX, RDFS.SUBCLASSOF, classY);
                sc.addStatement(uriA, RDF.TYPE, classX);
                sc.addStatement(uriB, RDF.TYPE, classY);
                sc.commit();

                //showStatements(sc, uriA, RDF.TYPE, null);

                assertEquals(3, countStatements(sc, uriA, RDF.TYPE, null, true));
                assertEquals(1, countStatements(sc, uriA, RDF.TYPE, null, false));
                assertEquals(2, countStatements(sc, uriB, RDF.TYPE, null, true));
                assertEquals(1, countStatements(sc, uriB, RDF.TYPE, null, false));

                if (uniqueStatements) {
                    sc.addStatement(uriA, RDF.TYPE, classY);
                    sc.commit();

                    //showStatements(sc, uriA, RDF.TYPE, null);
                    assertEquals(3, countStatements(sc, uriA, RDF.TYPE, null, true));
                    assertEquals(2, countStatements(sc, uriA, RDF.TYPE, null, false));

                    sc.removeStatements(uriA, RDF.TYPE, classY);
                    sc.commit();

                    assertEquals(3, countStatements(sc, uriA, RDF.TYPE, null, true));
                    assertEquals(2, countStatements(sc, uriA, RDF.TYPE, null, false));

                    //sc.removeStatements(uriA, RDF.TYPE, classX);
                    //sc.commit();
                    //assertEquals(1, countStatements(sc, uriA, RDF.TYPE, null));
                }
            } finally {
                sc.rollback();
                sc.close();
            }
        }
    }

// TODO: concurrency testing ///////////////////////////////////////////////
// //////////////////////////////////////////////////////////////////////////

    private class TestListener implements SailConnectionListener {
        private int added = 0, removed = 0;

        public void statementAdded(final Statement statement) {
            added++;
        }

        public void statementRemoved(final Statement statement) {
            removed++;
        }

        public int getAdded() {
            return added;
        }

        public int getRemoved() {
            return removed;
        }
    }

    protected void showStatements(final SailConnection sc,
                                  final Resource subject,
                                  final URI predicate,
                                  final Value object,
                                  final Resource... contexts) throws SailException {
        CloseableIteration<?, SailException> statements = sc.getStatements(subject, predicate, object, true, contexts);
        int count = 0;
        try {
            while (statements.hasNext()) {
                System.out.println("" + count + ") " + statements.next());
                count++;
            }
        } finally {
            statements.close();
        }
    }

    protected int countStatements(final SailConnection sc,
                                  final Resource subject,
                                  final URI predicate,
                                  final Value object,
                                  final boolean includeInferred,
                                  final Resource... contexts) throws SailException {
        CloseableIteration<?, SailException> statements = sc.getStatements(subject, predicate, object, includeInferred, contexts);
        int count = 0;
        try {
            while (statements.hasNext()) {
                statements.next();
                count++;
            }
        } finally {
            statements.close();
        }
        return count;
    }

    private Set<Statement> toSet(final CloseableIteration<? extends Statement, SailException> i) throws SailException {
        try {
            Set<Statement> set = new HashSet<Statement>();
            while (i.hasNext()) {
                set.add(i.next());
            }
            return set;
        } finally {
            i.close();
        }
    }

    protected void clear() throws Exception {
        SailConnection sc = sail.getConnection();
        try {
            sc.begin();
            sc.clear();
            sc.commit();
        } finally {
            sc.rollback();
            sc.close();
        }
    }

    protected void addFile(final InputStream in,
                           final RDFFormat format) throws Exception {
        try {
            SailConnection sc = sail.getConnection();
            sc.begin();
            try {
                RDFHandler h = new SailAdder(sc);
                RDFParser p = Rio.createParser(format);
                p.setRDFHandler(h);
                p.parse(in, "http://example.org/bogusBaseURI/");
                sc.commit();
            } finally {
                sc.rollback();
                sc.close();
            }
        } finally {
            in.close();
        }
    }

    protected class SailAdder implements RDFHandler {
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
            //System.out.println("adding statement: " + s);
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
