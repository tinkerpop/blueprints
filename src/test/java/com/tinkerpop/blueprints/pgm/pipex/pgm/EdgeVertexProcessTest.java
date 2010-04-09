package com.tinkerpop.blueprints.pgm.pipex.pgm;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraphFactory;
import com.tinkerpop.blueprints.pgm.pipex.BlockingChannel;
import com.tinkerpop.blueprints.pgm.pipex.Channel;
import junit.framework.TestCase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class EdgeVertexProcessTest extends TestCase {

    public void testInCommingVertex() {
        Graph graph = TinkerGraphFactory.createTinkerGraph();
        Vertex marko = graph.getVertex("1");
        Channel<Edge> in = new BlockingChannel<Edge>(50);
        Channel<Vertex> out = new BlockingChannel<Vertex>(50);
        EdgeVertexProcess evp = new EdgeVertexProcess(EdgeVertexProcess.Step.IN_VERTEX);
        evp.setInputChannel(in);
        evp.setOutputChannel(out);
        for (Edge edge : marko.getOutEdges()) {
            in.write(edge);
        }
        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.execute(evp);
        in.close();

        int counter = 0;
        while (!out.isComplete()) {
            Vertex v = out.read();
            if (null != v) {
                assertTrue(v.getId().equals("2") || v.getId().equals("3") || v.getId().equals("4"));
                counter++;
            }
        }
        assertEquals(counter, 3);
        executor.shutdown();

        Vertex josh = graph.getVertex("4");
        in = new BlockingChannel<Edge>(50);
        out = new BlockingChannel<Vertex>(50);
        evp = new EdgeVertexProcess(EdgeVertexProcess.Step.IN_VERTEX);
        evp.setInputChannel(in);
        evp.setOutputChannel(out);
        for (Edge edge : josh.getOutEdges()) {
            in.write(edge);
        }
        executor = Executors.newFixedThreadPool(1);
        executor.execute(evp);
        in.close();
        counter = 0;
        while (!out.isComplete()) {
            Vertex v = out.read();
            if (null != v) {
                assertTrue(v.getId().equals("5") || v.getId().equals("3"));
                counter++;
            }
        }
        assertEquals(counter, 2);
        executor.shutdown();

    }
}