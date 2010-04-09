package com.tinkerpop.blueprints.pgm.pipex;

import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraphFactory;
import com.tinkerpop.blueprints.pgm.pipex.pgm.EdgeVertexProcess;
import com.tinkerpop.blueprints.pgm.pipex.pgm.LabelFilterProcess;
import com.tinkerpop.blueprints.pgm.pipex.pgm.VertexEdgeProcess;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class PlayTest extends TestCase {

    public void testInOutVertex() {
        SerialProcess proc1 = new VertexEdgeProcess(VertexEdgeProcess.Step.OUT_EDGES);
        SerialProcess proc2 = new LabelFilterProcess(Arrays.asList("knows"), false);
        SerialProcess proc3 = new EdgeVertexProcess(EdgeVertexProcess.Step.IN_VERTEX);

        Channel<Vertex> startChannel = new BlockingChannel<Vertex>(10);
        Channel<Vertex> endChannel = new BlockingChannel<Vertex>(10);
        /*Channel<Edge> middleChannel = new BlockingChannel<Edge>(10);
        proc1.setChannel(SerialProcess.INPUT, startChannel);
        proc1.setChannel(SerialProcess.OUTPUT, middleChannel);
        proc2.setChannel(SerialProcess.INPUT, middleChannel);
        proc2.setChannel(SerialProcess.OUTPUT, endChannel);*/


        ExecutorService executor = Executors.newFixedThreadPool(2);
        SerialComposition comp = new SerialComposition(executor, 10, startChannel, endChannel, proc1, proc2, proc3);
        executor.execute(comp);

        Graph graph = TinkerGraphFactory.createTinkerGraph();
        startChannel.write(graph.getVertex("1"));
        startChannel.close();

        while (!endChannel.isComplete()) {
            System.out.println(endChannel.read());
        }

    }
}
