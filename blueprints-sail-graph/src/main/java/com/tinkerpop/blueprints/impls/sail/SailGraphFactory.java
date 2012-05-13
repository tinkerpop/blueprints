package com.tinkerpop.blueprints.impls.sail;

import com.tinkerpop.blueprints.Vertex;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class SailGraphFactory {

    public static void createTinkerGraph(SailGraph graph) {
        graph.addNamespace("tg", "http://tinkerpop.com#");
        Vertex marko = graph.addVertex(graph.expandPrefix("tg:1"));
        Vertex vadas = graph.addVertex(graph.expandPrefix("tg:2"));
        Vertex lop = graph.addVertex(graph.expandPrefix("tg:3"));
        Vertex josh = graph.addVertex(graph.expandPrefix("tg:4"));
        Vertex ripple = graph.addVertex(graph.expandPrefix("tg:5"));
        Vertex peter = graph.addVertex(graph.expandPrefix("tg:6"));
        graph.addEdge(null, marko, vadas, graph.expandPrefix("tg:knows"));
        graph.addEdge(null, marko, lop, graph.expandPrefix("tg:created"));
        graph.addEdge(null, marko, josh, graph.expandPrefix("tg:knows"));
        graph.addEdge(null, josh, lop, graph.expandPrefix("tg:created"));
        graph.addEdge(null, josh, ripple, graph.expandPrefix("tg:created"));
        graph.addEdge(null, peter, lop, graph.expandPrefix("tg:created"));
    }

}
