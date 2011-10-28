package com.tinkerpop.blueprints.pgm.impls.event.tx;

import com.tinkerpop.blueprints.pgm.*;
import com.tinkerpop.blueprints.pgm.impls.event.EventEdge;
import com.tinkerpop.blueprints.pgm.impls.event.EventGraph;
import com.tinkerpop.blueprints.pgm.impls.event.EventVertex;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toby.orourke
 * Date: 28/10/2011
 * Time: 10:27
 * To change this template use File | Settings | File Templates.
 */
public class EventTransactionalGraph extends EventGraph implements TransactionalGraph{

    ThreadLocal<List<Event>> eventBuffer = new ThreadLocal<List<Event>>();


    public EventTransactionalGraph(final TransactionalGraph graph) {
        super(graph);
        graph.registerAutoStopCallback(new TransactionStatusCallback() {

            @Override
            public void success() {
                for (Event event : getEventBuffer()) {
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
                if (getEventBuffer() == null) {
                    eventBuffer.set(new LinkedList<Event>());
                }
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
        getEventBuffer().add(new VertexAddedEvent(vertex));
    }

    @Override
    protected void onEdgeAdded(Edge edge) {
        getEventBuffer().add(new EdgeAddedEvent(edge));
    }

    private List<Event> getEventBuffer() {
        if (eventBuffer.get() == null) {
            eventBuffer.set(new LinkedList<Event>());
        }
        return eventBuffer.get();
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

