package com.tinkerpop.blueprints.impls.orient;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.TestSuite;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.GraphTest;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReader;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class OrientBenchmarkTestSuite extends TestSuite {

  private static final int TOTAL_RUNS = 3;
  private final boolean    print      = false;
  private final boolean    export     = true;

  public OrientBenchmarkTestSuite() {
  }

  public OrientBenchmarkTestSuite(final GraphTest graphTest) {
    super(graphTest);
  }

  //
  // public void testOrientRaw() throws Exception {
  // double totalTime = 0.0d;
  // OrientGraph graph = (OrientGraph) graphTest.generateGraph();
  // GraphMLReader.inputGraph(graph, GraphMLReader.class.getResourceAsStream("graph-example-2.xml"));
  // graph.shutdown();
  //
  // for (int i = 0; i < TOTAL_RUNS; i++) {
  // graph = (OrientGraph) graphTest.generateGraph();
  // OGraphDatabase db = new OGraphDatabase( (ODatabaseRecordTx) graph.getRawGraph().getUnderlying() );
  // this.stopWatch();
  // int counter = 0;
  // for (final ODocument vertex : db.browseClass(OGraphDatabase.VERTEX_CLASS_NAME)) {
  // counter++;
  // for (final Object edge : db.getOutEdges(vertex)) {
  // counter++;
  // final ODocument vertex2 = db.getInVertex((ODocument) edge);
  // counter++;
  // for (final Object edge2 : db.getOutEdges(vertex2)) {
  // counter++;
  // final ODocument vertex3 = db.getInVertex((ODocument) edge2);
  // counter++;
  // for (final Object edge3 : db.getOutEdges(vertex3)) {
  // counter++;
  // db.getOutVertex((ODocument) edge3);
  // counter++;
  // }
  // }
  // }
  // }
  // double currentTime = this.stopWatch();
  // totalTime = totalTime + currentTime;
  // BaseTest.printPerformance(db.toString(), counter, "Orient raw elements touched", currentTime);
  // graph.shutdown();
  // }
  // BaseTest.printPerformance("OrientRaw", 1, "OrientDB Raw experiment average", totalTime / (double) TOTAL_RUNS);
  // }

  public void testOrientGraph() throws Exception {
    double totalTime = 0.0d;
    Graph graph = graphTest.generateGraph();
    // graph = new OrientBatchGraph((ODatabaseDocumentTx) ((OrientGraph) graph).getRawGraph());
    GraphMLReader.inputGraph(graph, GraphMLReader.class.getResourceAsStream("graph-example-2.xml"));
    System.out.println("V: " + ((OrientBaseGraph) graph).getRawGraph().countClass("V") + " E: "
        + ((OrientBaseGraph) graph).getRawGraph().countClass("E"));
    graph.shutdown();

    for (int i = 0; i < TOTAL_RUNS; i++) {
      graph = graphTest.generateGraph();
      this.stopWatch();
      int counter = 0;
      for (final Vertex vertex : graph.getVertices()) {
        if (print)
          System.out.println("V1 " + vertex + " out: " + count(vertex.getEdges(Direction.OUT)) + " in: "
              + count(vertex.getEdges(Direction.IN)) + " -- " + counter);
        counter++;
        for (final Edge edge : vertex.getEdges(Direction.OUT)) {
          if (print)
            System.out.println("E1.out " + edge + " -- " + counter);
          counter++;
          final Vertex vertex2 = edge.getVertex(Direction.IN);
          if (print)
            System.out.println("V2 " + vertex2 + " out: " + count(vertex2.getEdges(Direction.OUT)) + " in: "
                + count(vertex2.getEdges(Direction.IN)) + " -- " + counter);
          counter++;
          for (final Edge edge2 : vertex2.getEdges(Direction.OUT)) {
            if (print)
              System.out.println("E2.out " + edge2 + " -- " + counter);
            counter++;
            final Vertex vertex3 = edge2.getVertex(Direction.IN);
            if (print)
              System.out.println("V3 " + vertex3 + " out: " + count(vertex3.getEdges(Direction.OUT)) + " in: "
                  + count(vertex3.getEdges(Direction.IN)) + " -- " + counter);
            counter++;
            for (final Edge edge3 : vertex3.getEdges(Direction.OUT)) {
              if (print)
                System.out.println("E3.out " + edge3 + " -- " + counter);
              counter++;
              final Vertex vertex4 = edge3.getVertex(Direction.OUT);
              if (print)
                System.out.println("V4 " + vertex4 + " out: " + count(vertex4.getEdges(Direction.OUT)) + " in: "
                    + count(vertex4.getEdges(Direction.IN)) + " -- " + counter);
              counter++;
            }
          }
        }
      }
      double currentTime = this.stopWatch();
      totalTime = totalTime + currentTime;
      BaseTest.printPerformance(graph.toString(), counter, "OrientGraph elements touched", currentTime);
      if (export)
        GraphMLWriter.outputGraph(graph, "/temp/graph-example-2-testOrientGraph.xml");
      graph.shutdown();
    }
    BaseTest.printPerformance("OrientGraph", 1, "OrientGraph experiment average", totalTime / (double) TOTAL_RUNS);
  }

  public void testOrientGraphNoDynaLinks() throws Exception {
    double totalTime = 0.0d;
    Graph graph = graphTest.generateGraph();
    // graph = new OrientBatchGraph((ODatabaseDocumentTx) ((OrientGraph) graph).getRawGraph());
    ((OrientBaseGraph) graph).setSaveOriginalIds(true);
    ((OrientBaseGraph) graph).setUseDynamicEdges(false);
    GraphMLReader.inputGraph(graph, GraphMLReader.class.getResourceAsStream("graph-example-2.xml"), 10, null, null, null);
    System.out.println("V: " + ((OrientBaseGraph) graph).getRawGraph().countClass("V") + " E: "
        + ((OrientBaseGraph) graph).getRawGraph().countClass("E"));
    graph.shutdown();

    for (int i = 0; i < TOTAL_RUNS; i++) {
      graph = graphTest.generateGraph();
      this.stopWatch();
      int counter = 0;
      for (final Vertex vertex : graph.getVertices()) {
        if (print)
          System.out.println("V1 " + vertex + " out: " + count(vertex.getEdges(Direction.OUT)) + " in: "
              + count(vertex.getEdges(Direction.IN)) + " -- " + counter);
        counter++;
        for (final Edge edge : vertex.getEdges(Direction.OUT)) {
          if (print)
            System.out.println("E1.out " + edge + " -- " + counter);
          counter++;
          final Vertex vertex2 = edge.getVertex(Direction.IN);
          if (print)
            System.out.println("V2 " + vertex2 + " out: " + count(vertex2.getEdges(Direction.OUT)) + " in: "
                + count(vertex2.getEdges(Direction.IN)) + " -- " + counter);
          counter++;
          for (final Edge edge2 : vertex2.getEdges(Direction.OUT)) {
            if (print)
              System.out.println("E2.out " + edge2 + " -- " + counter);
            counter++;
            final Vertex vertex3 = edge2.getVertex(Direction.IN);
            if (print)
              System.out.println("V3 " + vertex3 + " out: " + count(vertex3.getEdges(Direction.OUT)) + " in: "
                  + count(vertex3.getEdges(Direction.IN)) + " -- " + counter);
            counter++;
            for (final Edge edge3 : vertex3.getEdges(Direction.OUT)) {
              if (print)
                System.out.println("E3.out " + edge3 + " -- " + counter);
              counter++;
              final Vertex vertex4 = edge3.getVertex(Direction.OUT);
              if (print)
                System.out.println("V4 " + vertex4 + " out: " + count(vertex4.getEdges(Direction.OUT)) + " in: "
                    + count(vertex4.getEdges(Direction.IN)) + " -- " + counter);
              counter++;
            }
          }
        }
      }
      double currentTime = this.stopWatch();
      totalTime = totalTime + currentTime;
      BaseTest.printPerformance(graph.toString(), counter, "OrientGraph elements touched", currentTime);
      if (export)
        GraphMLWriter.outputGraph(graph, "/temp/graph-example-2-testOrientGraphNoDynaLinks.xml");
      graph.shutdown();
    }
    BaseTest.printPerformance("OrientGraph", 1, "OrientGraph experiment average", totalTime / (double) TOTAL_RUNS);
  }

}
