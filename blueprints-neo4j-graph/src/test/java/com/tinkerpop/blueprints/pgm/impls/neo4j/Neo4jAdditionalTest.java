package com.tinkerpop.blueprints.pgm.impls.neo4j;

import com.tinkerpop.blueprints.pgm.Vertex;
import junit.framework.TestCase;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.Config;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * @author peterneubauer
 * @since 1/28/12
 */
public class Neo4jAdditionalTest extends TestCase {

    public void testShouldNotDeleteAutomaticNeo4jIndexes() {
        Map<String, String> config;
        config = new HashMap<String, String>();
        config.put(Config.NODE_KEYS_INDEXABLE, "name");
        config.put(Config.RELATIONSHIP_KEYS_INDEXABLE, "rel1");
        config.put(Config.NODE_AUTO_INDEXING, "true");
        config.put(Config.RELATIONSHIP_AUTO_INDEXING, "true");
        EmbeddedGraphDatabase graphDb;
        graphDb = new EmbeddedGraphDatabase(
                "target/db1", config);

        Transaction tx = graphDb.beginTx();
        try {
            Node n1 = graphDb.createNode();
            n1.setProperty("name", "foo");
            tx.success();
        } finally {
            tx.finish();
        }
        Neo4jGraph g = new Neo4jGraph(graphDb, false);
        g.clear();
        assertTrue(null != graphDb.index().getNodeAutoIndexer().getAutoIndex());

    }
    public void testShouldNotDeleteAutomaticBlueprintsIndexes() {
        EmbeddedGraphDatabase graphDb;
        graphDb = new EmbeddedGraphDatabase(
                "target/db2");

        Transaction tx = graphDb.beginTx();
        try {
            Node n1 = graphDb.createNode();
            n1.setProperty("name", "foo");
            tx.success();
        } finally {
            tx.finish();
        }
        Neo4jGraph g = new Neo4jGraph(graphDb, false);
        g.clear();
        Vertex v1 = g.addVertex(null);
        v1.setProperty("name","foo");
        assertTrue(null != graphDb.index().getNodeAutoIndexer().getAutoIndex());

    }
}
