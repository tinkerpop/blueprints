package com.tinkerpop.blueprints.pgm.impls.git;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;
import junit.framework.TestCase;

import java.io.File;

/**
 * User: josh
 * Date: 4/13/11
 * Time: 11:47 PM
 */
public class GitGraphTest extends TestCase {
    public void testAll() throws Exception {
        Graph g;
        Vertex v1, v2, v3, v4, v5, v6;
        Edge e1, e2, e3, e4;

        File baseDir = System.getProperty("os.name").toUpperCase().contains("WINDOWS")
                ? new File("C:/temp/gitgraph-test")
                : new File("/tmp/gitgraph-test");
        if (baseDir.exists()) {
            GitGraphHelper.deleteDirectory(baseDir);
        }

        // Create child graph #1
        g = new GitGraph(new File(baseDir, "test1"));
        v1 = g.addVertex("melaney");
        v1.setProperty("name", "Melaney");
        v1.setProperty("age", 0.6);
        v2 = g.addVertex("us");
        v2.setProperty("name", "United States");
        e1 = g.addEdge("A", v1, v2, "lives in");
        e1.setProperty("as of", "2011-04-13");
        g.shutdown();

        // Validate persistence of graph #1 alone
        g = new GitGraph(new File(baseDir, "test1"));
        assertEquals(2, count(g.getVertices()));
        assertEquals(1, count(g.getEdges()));
        v1 = g.getVertex("melaney");
        assertEquals(0.6, v1.getProperty("age"));
        assertEquals(1, count(v1.getOutEdges()));
        assertEquals(0, count(v1.getInEdges()));
        g.shutdown();

        // Create child graph #2
        g = new GitGraph(new File(baseDir, "test2"));
        v4 = g.addVertex("leon");
        v4.setProperty("name", "Leon");
        v4.setProperty("age", 0.5);
        v5 = g.addVertex("china");
        v5.setProperty("name", "China");
        e2 = g.addEdge("B", v4, v5, "lives in");
        e2.setProperty("as of", "2011-04-13");
        v6 = g.addVertex("\n weird vertex");
        e3 = g.addEdge("weird  \tedge", v6, v5, "weird\tlabel");
        e3.setProperty("  a\t\nweird property", 12345l);
        g.shutdown();

        // Validate persistence of graph #2 alone.
        g = new GitGraph(new File(baseDir, "test2"));
        assertEquals(3, count(g.getVertices()));
        assertEquals(2, count(g.getEdges()));
        v4 = g.getVertex("leon");
        assertEquals(0.5, v4.getProperty("age"));
        v1 = v4.getOutEdges("lives in").iterator().next().getInVertex();
        assertEquals("china", v1.getId());
        assertEquals("China", v1.getProperty("name"));
        e1 = v1.getInEdges().iterator().next();
        assertEquals("weird  \tedge", e1.getId());
        assertEquals("weird\tlabel", e1.getLabel());
        v2 = e1.getOutVertex();
        assertEquals("\n weird vertex", v2.getId());
        assertEquals(12345l, e1.getProperty("  a\t\nweird property"));
        g.shutdown();

        // Combine the two graphs as a super-graph.
        // Add some edges which span the child graphs.
        g = new GitGraph(baseDir);
        v1 = g.getVertex("test1/melaney");
        assertEquals(0.6, v1.getProperty("age"));
        v4 = g.getVertex("test2/leon");
        assertEquals(0.5, v4.getProperty("age"));
        e4 = g.addEdge("C", v4, v1, "knows");
        e4.setProperty("comment", "via Skype");
        g.shutdown();

        // Validate the combined graph.
        g = new GitGraph(baseDir);
        v4 = g.getVertex("test2/leon");
        assertEquals(2, count(v4.getOutEdges()));
        assertEquals("test1/melaney", v4.getOutEdges("knows").iterator().next().getInVertex().getId());
        g.shutdown();

        // Add a grandchild graph.
        g = new GitGraph(new File(new File(baseDir, "test2"), "test3"));
        assertEquals(0, count(g.getEdges()));
        assertEquals(0, count(g.getVertices()));
        v1 = g.addVertex("yellow");
        v1.setProperty("name", "yellow");
        v2 = g.addVertex("color");
        e1 = g.addEdge("D", v1, v2, "is-a");
        g.shutdown();

        // Validate grandchild graph at its level.
        g = new GitGraph(new File(new File(baseDir, "test2"), "test3"));
        assertEquals(1, count(g.getEdges()));
        assertEquals(2, count(g.getVertices()));
        v2 = g.getVertex("color");
        assertEquals("yellow", v2.getInEdges().iterator().next().getOutVertex().getId());
        g.shutdown();

        // Add a new edge into the grandchild from the level of its parent.
        // Also add a property to a grandchild vertex from this level.
        g = new GitGraph(new File(baseDir, "test2"));
        v4 = g.getVertex("leon");
        assertEquals(0.5, v4.getProperty("age"));
        v1 = g.getVertex("test3/yellow");
        assertEquals("yellow", v1.getProperty("name"));
        v1.setProperty("comment", "comes after orange");
        e1 = g.addEdge("E", v4, v1, "likes");
        assertEquals(1, count(v4.getOutEdges("likes")));
        e1.setProperty("comment", "just a hunch");
        g.shutdown();

        // Validate grandchild and child from the level of the top-level graph.
        g = new GitGraph(baseDir);
        v1 = g.getVertex("test2/leon");
        assertEquals(0.5, v1.getProperty("age"));
        assertEquals(1, count(v1.getOutEdges("likes")));
        v2 = v1.getOutEdges("likes").iterator().next().getInVertex();
        assertEquals("test2/test3/yellow", v2.getId());
        assertEquals("comes after orange", v2.getProperty("comment"));
        g.shutdown();
    }

    private int count(final Iterable i) {
        int c = 0;
        for (Object o : i) {
            c++;
        }
        return c;
    }
}
