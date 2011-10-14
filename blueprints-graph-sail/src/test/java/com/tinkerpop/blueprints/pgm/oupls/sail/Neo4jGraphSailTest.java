package com.tinkerpop.blueprints.pgm.oupls.sail;

import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;
import org.junit.Test;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.Sail;

import java.io.File;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class Neo4jGraphSailTest extends GraphSailTest {

    protected IndexableGraph createGraph() {
        String directory = this.getWorkingDirectory();

        Neo4jGraph g = new Neo4jGraph(directory);
        //g.clear();
        g.setMaxBufferSize(0);
        return g;
    }

    private String getWorkingDirectory() {
        String d = System.getProperty("neo4jGraphDirectory");
        if (null == d) {
            d = System.getProperty("os.name").toUpperCase().contains("WINDOWS")
                    ? "C:/temp/blueprints_test"
                    : "/tmp/blueprints_test";
        }

        deleteDirectory(new File(d));

        return d;
    }

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