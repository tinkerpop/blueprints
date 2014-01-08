package com.tinkerpop.blueprints.impls.neo4j;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.kernel.impl.util.FileUtils;

import java.io.File;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * @author mh
 * @since 08.01.14
 */
public class Neo4jVertexTest {

    private Neo4jGraph graph;

    @Before
    public void setUp() throws Exception {
        FileUtils.deleteRecursively(new File("target/test.db"));
        graph = new Neo4jGraph("target/test.db");
    }

    @Test
    public void testVertexLabels() throws Exception {
        Neo4jVertex vertex = graph.addVertex(null);
        vertex.addLabel("Label");
        assertEquals(Arrays.asList("Label"), vertex.getLabels());
        vertex.addLabel("Label2");
        assertEquals(Arrays.asList("Label","Label2"), vertex.getLabels());
        vertex.removeLabel("Label2");
        assertEquals(Arrays.asList("Label"), vertex.getLabels());
        vertex.removeLabel("Label");
        assertEquals(Arrays.<String>asList(), vertex.getLabels());
    }

    @After
    public void tearDown() throws Exception {
        graph.shutdown();
    }
}
