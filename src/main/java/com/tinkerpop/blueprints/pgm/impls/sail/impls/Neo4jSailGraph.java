package com.tinkerpop.blueprints.pgm.impls.sail.impls;

import com.tinkerpop.blueprints.pgm.impls.sail.SailGraph;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.index.IndexService;
import org.neo4j.index.lucene.LuceneIndexService;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.rdf.sail.GraphDatabaseSail;
import org.neo4j.rdf.store.RdfStore;
import org.neo4j.rdf.store.VerboseQuadStore;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jSailGraph extends SailGraph {

    public Neo4jSailGraph(String directory) {
        GraphDatabaseService graphDb = new EmbeddedGraphDatabase(directory);
        IndexService indexService = new LuceneIndexService(graphDb);
        RdfStore rdfStore = new VerboseQuadStore(graphDb, indexService);
        this.startSail(new GraphDatabaseSail(graphDb, rdfStore, true));
    }
}
