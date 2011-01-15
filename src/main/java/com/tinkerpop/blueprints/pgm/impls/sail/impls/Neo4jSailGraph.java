package com.tinkerpop.blueprints.pgm.impls.sail.impls;

import com.tinkerpop.blueprints.pgm.impls.sail.SailGraph;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
/*public class Neo4jSailGraph extends SailGraph {

    public Neo4jSailGraph(String directory) {
        GraphDatabaseService graphDb = new EmbeddedGraphDatabase(directory);
        IndexService indexService = new LuceneIndexService(graphDb);
        RdfStore rdfStore = new VerboseQuadStore(graphDb, indexService);
        this.startSail(new GraphDatabaseSail(graphDb, rdfStore, true));
    }
}*/

public class Neo4jSailGraph extends SailGraph {
    public Neo4jSailGraph() {
        throw new RuntimeException("Neo4jSail is no longer supported");
    }
}