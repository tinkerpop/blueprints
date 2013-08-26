package com.tinkerpop.blueprints.impls.sail.impls;

import com.tinkerpop.blueprints.impls.sail.SailGraph;
import net.fortytwo.sesametools.reposail.RepositorySail;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParserFactory;
import org.openrdf.rio.RDFParserRegistry;
import org.openrdf.sail.Sail;

import java.util.Collection;
import java.util.LinkedList;

/**
 * SparqlRepositorySailGraph turns any SPARQL endpoint into a Blueprints SailGraph.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class SparqlRepositorySailGraph extends SailGraph {
    static {
        // TODO: this should eventually be moved to a more central "Sesame config" location
        try {
            ignoreDatatypesInAllParsers();
        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

    public SparqlRepositorySailGraph(final String queryEndpointUrl) {
        super(createSail(queryEndpointUrl, null));
    }

    public SparqlRepositorySailGraph(final String queryEndpointUrl,
                                     final String updateEndpointUrl) {
        super(createSail(queryEndpointUrl, updateEndpointUrl));
    }

    private static Sail createSail(final String queryEndpointUrl,
                                   final String updateEndpointUrl) {
        if (null == queryEndpointUrl) {
            throw new IllegalArgumentException("Query endpoint URL may not be null");
        }

        SPARQLRepository r = null == updateEndpointUrl
                ? new SPARQLRepository(queryEndpointUrl)
                : new SPARQLRepository(queryEndpointUrl, updateEndpointUrl);

        return new RepositorySail(r);
    }

    // wrap RDF parser factories such that they ignore invalid values in data-typed literals
    // (e.g. a value of "fish" for an xsd:integer literal,
    // or a value of 1995-01-01T00:00:00+02:00 for an xsd:gYear literal).
    // The default behavior is to throw an exception when bad literals are encountered,
    // resulting in failure.
    private static void ignoreDatatypesInAllParsers() {
        RDFParserRegistry r = RDFParserRegistry.getInstance();
        Collection<RDFParserFactory> oldFactories = new LinkedList<RDFParserFactory>();
        Collection<RDFParserFactory> newFactories = new LinkedList<RDFParserFactory>();

        for (final RDFFormat f : r.getKeys()) {
            final RDFParserFactory pf = r.get(f);
            pf.getParser().setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);

            RDFParserFactory pfn = new RDFParserFactory() {
                public RDFFormat getRDFFormat() {
                    return f;
                }

                public RDFParser getParser() {
                    RDFParser p = pf.getParser();
                    p.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);
                    return p;
                }
            };

            oldFactories.add(pf);
            newFactories.add(pfn);
        }

        for (RDFParserFactory pf : oldFactories) {
            r.remove(pf);
        }

        for (RDFParserFactory pfn : newFactories) {
            r.add(pfn);
        }
    }

    /*public static void main(final String[] args) throws Exception {
        Repository r = new SPARQLRepository("http://dbpedia.org/sparql");
        Sail s = new RepositorySail(r);
        s.initialize();
        try {
            SailConnection sc = s.getConnection();
            try {
                CloseableIteration<? extends Statement, SailException> iter
                        = sc.getStatements(new URIImpl("http://dbpedia.org/resource/Beijing"), null, null, false);
                try {
                    while (iter.hasNext()) {
                        System.out.println("statement: " + iter.next());
                    }
                } finally {
                    iter.close();
                }
            } finally {
                sc.close();
            }
        } finally {
            s.shutDown();
        }
    }*/
}
