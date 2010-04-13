package com.tinkerpop.blueprints.pgm.pipex.pgm;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraphFactory;
import com.tinkerpop.blueprints.pgm.pipex.BlockingChannel;
import junit.framework.TestCase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class EdgeVertexProcessTest extends TestCase {

    public void testInCommingVertex() throws Exception {
        Graph graph = TinkerGraphFactory.createTinkerGraph();
        Vertex marko = graph.getVertex("1");
        EdgeVertexProcess evp = new EdgeVertexProcess(EdgeVertexProcess.Step.IN_VERTEX, new BlockingChannel<Edge>(1), new BlockingChannel<Vertex>(1));
        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.execute(evp);
        for (Edge edge : marko.getOutEdges()) {
            evp.getInChannel().write(edge);
        }
        evp.getInChannel().close();
        int counter = 0;
        Vertex v = evp.getOutChannel().read();
        while (null != v) {
            System.out.println(v);
            assertTrue(v.getId().equals("2") || v.getId().equals("3") || v.getId().equals("4"));
            counter++;
            v = evp.getOutChannel().read();
        }
        evp.getOutChannel().close();

        assertEquals(counter, 3);
        executor.shutdown();
        System.out.println("---");
        Vertex josh = graph.getVertex("4");
        evp = new EdgeVertexProcess(EdgeVertexProcess.Step.IN_VERTEX, new BlockingChannel<Edge>(50), new BlockingChannel<Vertex>(50));
        executor = Executors.newFixedThreadPool(2);
        executor.execute(evp);

        for (Edge edge : josh.getOutEdges()) {
            evp.getInChannel().write(edge);
        }
        evp.getInChannel().close();

        counter = 0;
        v = evp.getOutChannel().read();
        while (null != v) {
            System.out.println(v);
            assertTrue(v.getId().equals("5") || v.getId().equals("3"));
            counter++;
            v = evp.getOutChannel().read();
        }
        evp.getOutChannel().close();

        assertEquals(counter, 2);
        executor.shutdown();

    }
}