package com.tinkerpop.blueprints.impls.neo4j2;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.kernel.impl.util.FileUtils;

import java.io.File;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author mh
 * @since 08.01.14
 */
public class Neo4j2GraphCypherTest {
    private Neo4j2Graph graph;

    @Before
    public void setUp() throws Exception {
        FileUtils.deleteRecursively(new File("target/test.db"));
        graph = new Neo4j2Graph("target/test.db");
    }

    @Test
    public void testVertexLabels() throws Exception {
        Neo4j2Vertex vertex = graph.addVertex(null);
        vertex.addLabel("Label");
        vertex.setProperty("key","value");
        queryAndAssert(vertex);

        graph.commit();

        graph.query("create index on :Label(key)",null);

        queryAndAssert(vertex);
    }

    private void queryAndAssert(Neo4j2Vertex vertex) {
        Map<String, Object> params = MapUtil.map("prop", "value");
        Map<String,Object> row = IteratorUtil.single(graph.query("MATCH (n:Label {key:{prop}}) RETURN n", params));
        assertEquals(vertex.getRawVertex(), row.get("n"));
    }

    @After
    public void tearDown() throws Exception {
        graph.shutdown();
    }
}
