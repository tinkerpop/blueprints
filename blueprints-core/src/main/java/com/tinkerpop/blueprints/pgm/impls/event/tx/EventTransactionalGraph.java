package com.tinkerpop.blueprints.pgm.impls.event.tx;

import com.tinkerpop.blueprints.pgm.*;
import com.tinkerpop.blueprints.pgm.impls.event.EventEdge;
import com.tinkerpop.blueprints.pgm.impls.event.EventGraph;
import com.tinkerpop.blueprints.pgm.impls.event.EventVertex;

import java.util.LinkedList;
import java.util.List;

public class EventTransactionalGraph extends EventGraph implements TransactionalGraph{

    ThreadLocal<List<Event>> eventBuffer = new ThreadLocal<List<Event>>(){
        protected List<Event> initialValue() {
            return new LinkedList<Event>();
        }
    };


    public EventTransactionalGraph(final TransactionalGraph graph) {
        super(graph);
        graph.registerAutoStopCallback(new TransactionStatusCallback() {

            @Override
            public void success() {
                for (Event event : eventBuffer.get()) {
                    event.fireEvent(getListenerIterator());
                }
                eventBuffer.set(new LinkedList<Event>());
            }

            @Override
            public void failure() {
                eventBuffer.set(new LinkedList<Event>());
            }

            @Override
            public void start() {
                eventBuffer.get();
            }
        });
    }

    @Override
    public Vertex addVertex(Object id) {
        return new EventTransactionalVertex((EventVertex) super.addVertex(id), graphChangedListeners, eventBuffer);
    }

    @Override
    public Edge addEdge(Object id, Vertex outVertex, Vertex inVertex, String label) {
        return new EventTransactionalEdge((EventEdge) super.addEdge(id, outVertex, inVertex, label), graphChangedListeners, eventBuffer);
    }

    @Override
    protected void onVertexAdded(Vertex vertex) {
        eventBuffer.get().add(new VertexAddedEvent(vertex));
    }

    @Override
    protected void onEdgeAdded(Edge edge) {
        eventBuffer.get().add(new EdgeAddedEvent(edge));
    }


    @Override
    public void setMaxBufferSize(int bufferSize) {
        ((TransactionalGraph) super.getRawGraph()).setMaxBufferSize(bufferSize);
    }

    @Override
    public int getMaxBufferSize() {
        return ((TransactionalGraph) super.getRawGraph()).getMaxBufferSize();
    }

    @Override
    public int getCurrentBufferSize() {
        return ((TransactionalGraph) super.getRawGraph()).getCurrentBufferSize();
    }

    @Override
    public void startTransaction() {
        ((TransactionalGraph) super.getRawGraph()).startTransaction();
    }

    @Override
    public void stopTransaction(Conclusion conclusion) {
       ((TransactionalGraph) super.getRawGraph()).stopTransaction(conclusion);
    }

    @Override
    public void registerAutoStopCallback(TransactionStatusCallback callback) {
        ((TransactionalGraph) super.getRawGraph()).registerAutoStopCallback(callback);
    }
}

