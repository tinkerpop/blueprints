package com.tinkerpop.blueprints.util.wrappers.event;

import com.tinkerpop.blueprints.util.wrappers.event.listener.Event;

import java.util.ArrayList;
import java.util.List;

class EventTrigger {

    /**
     * A queue of events that are triggered by change to the graph.  The queue builds
     * up until the EventTrigger fires them in the order they were received.
     */
    private final ThreadLocal<List<Event>> eventQueue = new ThreadLocal<List<Event>>() {
        protected List<Event> initialValue() {
            return new ArrayList<Event>();
        }
    };

    /**
     * When set to true, events in the event queue will only be fired when a transaction
     * is committed.
     */
    private final boolean enqueEvents;

    private final EventGraph graph;

    public EventTrigger(final EventGraph graph, final boolean enqueEvents) {
        this.enqueEvents = enqueEvents;
        this.graph = graph;
    }

    /**
     * Add an event to the event queue.
     * <p/>
     * If the enqueEvents is false, then the queue fires and resets after each event
     * is added.
     */
    public void addEvent(Event evt) {
        this.eventQueue.get().add(evt);

        if (!this.enqueEvents) {
            this.fireEventQueue();
            this.resetEventQueue();
        }
    }

    public void resetEventQueue() {
        eventQueue.set(new ArrayList<Event>());
    }

    public void fireEventQueue() {
        for (Event event : eventQueue.get()) {
            event.fireEvent(this.graph.getListenerIterator());
        }
    }
}
