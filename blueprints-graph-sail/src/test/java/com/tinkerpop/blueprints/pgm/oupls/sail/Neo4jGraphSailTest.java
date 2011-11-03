package com.tinkerpop.blueprints.pgm.oupls.sail;

import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;
import junit.framework.TestCase;
import org.junit.Test;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.Sail;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class Neo4jGraphSailTest extends TestCase { //GraphSailTest {

    protected IndexableGraph createGraph() {
        String directory = System.getProperty("neo4jGraphDirectory");
        if (directory == null)
            directory = this.getWorkingDirectory();
        //g.clear();
        //g.setMaxBufferSize(0);
        Neo4jGraph g = new Neo4jGraph(directory);

        return g;
    }

    private String getWorkingDirectory() {
        String directory = System.getProperty("neo4jGraphDirectory");
        if (directory == null) {
            if (System.getProperty("os.name").toUpperCase().contains("WINDOWS"))
                directory = "C:/temp/blueprints_test";
            else
                directory = "/tmp/blueprints_test";
        }
        return directory;
    }

    /*
    public void testTemp() throws Exception {
        IndexableGraph graph;
        Sail sail;

        graph = new Neo4jGraph("/tmp/neodebug");
        sail = new GraphSail(graph);
        sail.initialize();
        sail.shutDown();

        graph = new Neo4jGraph("/tmp/neodebug");
        sail = new GraphSail(graph);
        sail.initialize();
        sail.shutDown();
    }
    */

    @Test
    public void testEvalNew() throws Exception {
        Neo4jGraph graph;
        Sail sail = null;
        graph = new Neo4jGraph("/tmp/neo4j-data");
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
}