package com.tinkerpop.blueprints.pgm.impls.event.tx;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.TransactionStatusCallback;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.event.StubGraphChangedListener;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraphFactory;

public class EventedTransactionalGraphTest extends BaseTest {

    private StubGraphChangedListener graphChangedListener;
    private EventTransactionalGraph txGraph;

    public void setUp() throws Exception {
        super.setUp();
        graphChangedListener = new StubGraphChangedListener();
        txGraph = new EventTransactionalGraph(createStubTransactionalGraph());
        txGraph.addListener(graphChangedListener);
        txGraph.setMaxBufferSize(0);
    }

    private TransactionalGraph createStubTransactionalGraph() {

        return new TinkerTransactionalGraph(TinkerGraphFactory.createTinkerGraph());

    }

    @Override
    public void tearDown() throws Exception {
        graphChangedListener.reset();
        super.tearDown();
        txGraph.shutdown();
    }

    public void testAddVertex() {
        txGraph.startTransaction();

        txGraph.addVertex(null);

        txGraph.stopTransaction(TransactionalGraph.Conclusion.FAILURE);

        assertFalse("Event should not be triggered", graphChangedListener.addVertexEventRecorded());

        txGraph.startTransaction();

        txGraph.addVertex(null);

        txGraph.stopTransaction(TransactionalGraph.Conclusion.SUCCESS);

        assertTrue("Event should be triggered", graphChangedListener.addVertexEventRecorded());
    }

    public void testAddEdge() {
        txGraph.startTransaction();

        txGraph.addEdge(null, txGraph.addVertex(null), txGraph.addVertex(null), "knows");

        txGraph.stopTransaction(TransactionalGraph.Conclusion.FAILURE);

        assertFalse("Event should not be triggered", graphChangedListener.addEdgeEventRecorded());

        txGraph.startTransaction();

        txGraph.addEdge(null, txGraph.addVertex(null), txGraph.addVertex(null), "knows");

        txGraph.stopTransaction(TransactionalGraph.Conclusion.SUCCESS);

        assertTrue("Event should be triggered", graphChangedListener.addEdgeEventRecorded());
    }

    public void testAddVertexProperty() {
        txGraph.startTransaction();

        txGraph.addVertex(null).setProperty("name", "marko");

        txGraph.stopTransaction(TransactionalGraph.Conclusion.FAILURE);

        assertFalse("Event should not be triggered", graphChangedListener.vertexPropertyChangedEventRecorded());

        txGraph.startTransaction();

        txGraph.addVertex(null).setProperty("name", "marko");

        txGraph.stopTransaction(TransactionalGraph.Conclusion.SUCCESS);

        assertTrue("Event should be triggered", graphChangedListener.vertexPropertyChangedEventRecorded());
    }

    public void testAddEdgeProperty() {
        txGraph.startTransaction();

        txGraph.addEdge(null, txGraph.addVertex(null), txGraph.addVertex(null), "knows").setProperty("weight", 50);

        txGraph.stopTransaction(TransactionalGraph.Conclusion.FAILURE);

        assertFalse("Event should not be triggered", graphChangedListener.edgePropertyChangedEventRecorded());

        txGraph.startTransaction();

        txGraph.addEdge(null, txGraph.addVertex(null), txGraph.addVertex(null), "knows").setProperty("weight", 50);

        txGraph.stopTransaction(TransactionalGraph.Conclusion.SUCCESS);

        assertTrue("Event should be triggered", graphChangedListener.edgePropertyChangedEventRecorded());

    }

    public void testRemoveEdgeProperty() {
        txGraph.startTransaction();

        Edge edge = txGraph.addEdge(null, txGraph.addVertex(null), txGraph.addVertex(null), "knows");
        edge.setProperty("weight", 50);
        edge.removeProperty("weight");

        txGraph.stopTransaction(TransactionalGraph.Conclusion.FAILURE);

        assertFalse("Event should not be triggered", graphChangedListener.edgePropertyRemovedEventRecorded());

        txGraph.startTransaction();

        edge = txGraph.addEdge(null, txGraph.addVertex(null), txGraph.addVertex(null), "knows");
        edge.setProperty("weight", 50);
        edge.removeProperty("weight");

        txGraph.stopTransaction(TransactionalGraph.Conclusion.SUCCESS);

        assertTrue("Event should be triggered", graphChangedListener.edgePropertyRemovedEventRecorded());

    }

    public void testRemoveVertexProperty() {
        txGraph.startTransaction();

        Vertex vertex = txGraph.addVertex(null);
        vertex.setProperty("name", "marko");
        vertex.removeProperty("name");

        txGraph.stopTransaction(TransactionalGraph.Conclusion.FAILURE);

        assertFalse("Event should not be triggered", graphChangedListener.vertexPropertyRemovedEventRecorded());

        txGraph.startTransaction();

        vertex = txGraph.addVertex(null);
        vertex.setProperty("name", "marko");
        vertex.removeProperty("name");

        txGraph.stopTransaction(TransactionalGraph.Conclusion.SUCCESS);

        assertTrue("Event should be triggered", graphChangedListener.vertexPropertyRemovedEventRecorded());
    }

    public void testAutoCommitTx(){
        txGraph.setMaxBufferSize(2);

        txGraph.addVertex(null);

        assertFalse("Event should not be triggered", graphChangedListener.addVertexEventRecorded());

        txGraph.addVertex(null);

        assertTrue("Event should be triggered", graphChangedListener.addVertexEventRecorded());
    }

}


/**
 * Noddy class that increments the tx buffer when we want it to.
 */
class TinkerTransactionalGraph extends TinkerGraph implements TransactionalGraph {

    private TinkerGraph graph;
    private TransactionStatusCallback transactionStatusCallback;
    private int bufferSize;
    private int bufferCount = 0;

    public TinkerTransactionalGraph(TinkerGraph tinkerGraph) {
        this.graph = tinkerGraph;
    }

    @Override
    public void setMaxBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    @Override
    public int getMaxBufferSize() {
       return bufferSize;
    }

    @Override
    public int getCurrentBufferSize() {
        return bufferCount;
    }

    @Override
    public void startTransaction() {
        if (transactionStatusCallback != null)  transactionStatusCallback.start();
    }

    @Override
    public void stopTransaction(Conclusion conclusion) {
        if(Conclusion.SUCCESS.equals(conclusion)) {
            if (transactionStatusCallback != null)  transactionStatusCallback.success();
        } else {
            if (transactionStatusCallback != null)  transactionStatusCallback.failure();
        }
    }

    @Override
    public Vertex addVertex(Object id) {
        bufferCount++;
        Vertex vertex = super.addVertex(id);
        if(bufferCount == bufferSize){
            stopTransaction(Conclusion.SUCCESS);
        }
        return vertex;
    }

    @Override
    public void registerAutoStopCallback(TransactionStatusCallback callback) {
        this.transactionStatusCallback = callback;
    }
}

