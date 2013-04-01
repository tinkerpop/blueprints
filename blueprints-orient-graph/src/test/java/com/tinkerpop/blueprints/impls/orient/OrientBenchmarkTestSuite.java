package com.tinkerpop.blueprints.impls.orient;

import java.io.FileInputStream;

import com.orientechnologies.orient.core.config.OGlobalConfiguration;
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

  private static final int TOTAL_RUNS = 1;
  private final boolean    print      = false;
  private final boolean    export     = false;

  public OrientBenchmarkTestSuite() {
  }

  public OrientBenchmarkTestSuite(final GraphTest graphTest) {
    super(graphTest);
    OGlobalConfiguration.STORAGE_KEEP_OPEN.setValue(true);
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
    //GraphMLReader.inputGraph(graph, GraphMLReader.class.getResourceAsStream("graph-example-2.xml"));
    GraphMLReader.inputGraph(graph, new FileInputStream("/Users/luca/Downloads/graph-example-2.xml"));
    System.out.println("V: " + ((OrientBaseGraph) graph).getRawGraph().countClass("V") + " E: "
        + ((OrientBaseGraph) graph).getRawGraph().countClass("E"));
    graph.shutdown();

    for (int i = 0; i < TOTAL_RUNS; i++) {
      graph = graphTest.generateGraph();
      int counter = execute(graph);
      double currentTime = this.stopWatch();
      totalTime = totalTime + currentTime;
      BaseTest.printPerformance(graph.toString(), counter, "OrientGraph elements touched", currentTime);
      if (export)
        GraphMLWriter.outputGraph(graph, "/temp/graph-example-2-testOrientGraph.xml");
      graph.shutdown();
    }
    BaseTest.printPerformance("OrientGraph", 1, "OrientGraph experiment average", totalTime / (double) TOTAL_RUNS);
  }
//
//  public void testOrientGraphNoDynaLinks() throws Exception {
//    double totalTime = 0.0d;
//    Graph graph = graphTest.generateGraph();
//    // graph = new OrientBatchGraph((ODatabaseDocumentTx) ((OrientGraph) graph).getRawGraph());
//    ((OrientBaseGraph) graph).setUseDynamicEdges(false);
//    //GraphMLReader.inputGraph(graph, GraphMLReader.class.getResourceAsStream("graph-example-2.xml"));
//    GraphMLReader.inputGraph(graph, new FileInputStream("/Users/luca/Downloads/graph-example-2.xml"));
//    System.out.println("V: " + ((OrientBaseGraph) graph).getRawGraph().countClass("V") + " E: "
//        + ((OrientBaseGraph) graph).getRawGraph().countClass("E"));
//    graph.shutdown();
//
//    for (int i = 0; i < TOTAL_RUNS; i++) {
//      graph = graphTest.generateGraph();
//      int counter = execute(graph);
//      double currentTime = this.stopWatch();
//      totalTime = totalTime + currentTime;
//      BaseTest.printPerformance(graph.toString(), counter, "OrientGraph elements touched", currentTime);
//      if (export)
//        GraphMLWriter.outputGraph(graph, "/temp/graph-example-2-testOrientGraphNoDynaLinks.xml");
//      graph.shutdown();
//    }
//    BaseTest.printPerformance("OrientGraph", 1, "OrientGraph experiment average", totalTime / (double) TOTAL_RUNS);
//  }

  private int execute(Graph graph) {
    this.stopWatch();
    int counter = 0;

    int counter0 = 0;
    int counter1 = 0;
    int counter2 = 0;
    int counter3 = 0;
    int counter4 = 0;
    int counter5 = 0;
    int counter6 = 0;

    for (final Vertex vertex : graph.getVertices()) {
      if (print)
        System.out.println("V1 " + vertex + " out: " + count(vertex.getEdges(Direction.OUT)) + " in: "
            + count(vertex.getEdges(Direction.IN)) + " -- " + counter);
      counter++;
      counter0++;
      for (final Edge edge : vertex.getEdges(Direction.OUT)) {
        if (print)
          System.out.println("E1.out " + edge + " -- " + counter);
        counter++;
        counter1++;
        final Vertex vertex2 = edge.getVertex(Direction.IN);
        counter++;
        counter2++;
        for (final Edge edge2 : vertex2.getEdges(Direction.OUT)) {
          counter++;
          counter3++;
          final Vertex vertex3 = edge2.getVertex(Direction.IN);
          counter++;
          counter4++;
          for (final Edge edge3 : vertex3.getEdges(Direction.OUT)) {
            counter++;
            counter5++;
            final Vertex vertex4 = edge3.getVertex(Direction.OUT);
            counter++;
            counter6++;
          }
        }
      }
    }
    System.out.println("counter0 = " + counter0);
    System.out.println("counter1 = " + counter1);
    System.out.println("counter2 = " + counter2);
    System.out.println("counter3 = " + counter3);
    System.out.println("counter4 = " + counter4);
    System.out.println("counter5 = " + counter5);
    System.out.println("counter6 = " + counter6);
    return counter;
  }

}
