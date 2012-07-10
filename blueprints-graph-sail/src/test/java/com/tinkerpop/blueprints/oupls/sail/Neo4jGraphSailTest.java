package com.tinkerpop.blueprints.oupls.sail;


import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.impls.neo4j.Neo4jGraph;
import org.junit.Test;

import java.io.File;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class Neo4jGraphSailTest extends GraphSailTest {

    private String getWorkingDirectory() throws Exception {
        File dir = File.createTempFile("blueprints", "-neo4j-test");
        String path = dir.getPath();
        dir.delete();
        dir.mkdir();

        return path;
    }

    /*
    @Test
    public void testEvalNew() throws Exception {
        Neo4jGraph graph;
        Sail sail = null;
        deleteDirectory(new File(getWorkingDirectory()));
        graph = new Neo4jGraph(getWorkingDirectory());
        sail = new GraphSail(graph);
        sail.initialize();
        RepositoryConnection rc = new
                SailRepository(sail).getConnection();
        try {
            rc.add(SailTest.class.getResourceAsStream("graph-example-sparql.ttl"),
                    "http://example.org/baseURI/",
                    RDFFormat.TURTLE);
            rc.commit();
            System.out.println("Execute SPARQL query");
            TupleQuery query = rc.prepareTupleQuery(QueryLanguage.SPARQL,
                    "PREFIX ctag: <http://commontag.org/ns#> " +
                            "SELECT ?tag ?label " +
                            "WHERE { " +
                            "?tag ctag:label ?label . " +
                            "}");
            System.out.println("TupleQuery");
            TupleQueryResult result = query.evaluate();
            System.out.println("TupleQueryResults:");
            while (result.hasNext()) {
                System.out.println(result.next());
            }
        } finally {
            rc.close();
            sail.shutDown();
        }
    }
    */

    @Test
    public void testSelfEdgesNotSupported() throws Exception {

    }

    protected KeyIndexableGraph createGraph() throws Exception {
        String directory = System.getProperty("neo4jGraphDirectory");
        if (directory == null) {
            directory = this.getWorkingDirectory();
        }

        Neo4jGraph g = new Neo4jGraph(directory);
        g.setCheckElementsInTransaction(true);

        return g;
    }
}