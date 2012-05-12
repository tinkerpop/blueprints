package com.tinkerpop.blueprints.pgm.util.wrappers.eventtransactional;

import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraphFactory;

/**
 * Created by IntelliJ IDEA.
 * User: stephen m
 * Date: 5/12/12
 * Time: 11:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class TinkerTransactionalGraph extends TinkerGraph implements TransactionalGraph {
    public void startTransaction() {
    }

    public void stopTransaction(Conclusion conclusion) {
    }

    public static TinkerTransactionalGraph createTinkerGraph() {

        TinkerTransactionalGraph graph = new TinkerTransactionalGraph();

        Vertex marko = graph.addVertex("1");
        marko.setProperty("name", "marko");
        marko.setProperty("age", 29);

        Vertex vadas = graph.addVertex("2");
        vadas.setProperty("name", "vadas");
        vadas.setProperty("age", 27);

        Vertex lop = graph.addVertex("3");
        lop.setProperty("name", "lop");
        lop.setProperty("lang", "java");

        Vertex josh = graph.addVertex("4");
        josh.setProperty("name", "josh");
        josh.setProperty("age", 32);

        Vertex ripple = graph.addVertex("5");
        ripple.setProperty("name", "ripple");
        ripple.setProperty("lang", "java");

        Vertex peter = graph.addVertex("6");
        peter.setProperty("name", "peter");
        peter.setProperty("age", 35);

        graph.addEdge("7", marko, vadas, "knows").setProperty("weight", 0.5f);
        graph.addEdge("8", marko, josh, "knows").setProperty("weight", 1.0f);
        graph.addEdge("9", marko, lop, "created").setProperty("weight", 0.4f);

        graph.addEdge("10", josh, ripple, "created").setProperty("weight", 1.0f);
        graph.addEdge("11", josh, lop, "created").setProperty("weight", 0.4f);

        graph.addEdge("12", peter, lop, "created").setProperty("weight", 0.2f);

        return graph;

    }
}
