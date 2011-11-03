package com.tinkerpop.blueprints.pgm.impls.sail.impls;

import com.tinkerpop.blueprints.pgm.impls.sail.SailGraph;
import net.fortytwo.sesametools.reposail.RepositorySail;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.openrdf.sail.Sail;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class SparqlRepositorySailGraph extends SailGraph {

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
            throw new IllegalArgumentException("query endpoint URL may not be null");
        }

        Repository r = null == updateEndpointUrl
                ? new SPARQLRepository(queryEndpointUrl)
                : new SPARQLRepository(queryEndpointUrl, updateEndpointUrl);

        return new RepositorySail(r);
    }
}
