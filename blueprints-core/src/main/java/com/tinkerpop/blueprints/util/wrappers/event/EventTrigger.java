package com.tinkerpop.blueprints.util.wrappers.event;

import com.tinkerpop.blueprints.util.wrappers.event.listener.Event;

import java.util.ArrayDeque;
import java.util.Deque;

public class EventTrigger {

    /**
     * A queue of events that are triggered by change to the graph. The queue builds up until the EventTrigger fires them in the
     * order they were received.
     */
    private final ThreadLocal<Deque<Event>> eventQueue = new ThreadLocal<Deque<Event>>() {
        protected Deque<Event> initialValue() {
            return new ArrayDeque<Event>();
        }
    };

    /**
     * When set to true, events in the event queue will only be fired when a transaction is committed.
     */
    private final boolean enqueEvents;

    private final EventGraph graph;

    public EventTrigger(final EventGraph graph, final boolean enqueEvents) {
        this.enqueEvents = enqueEvents;
        this.graph = graph;
    }

    /**
     * Add an event to the event queue.
     *
     * If the enqueEvents is false, then the queue fires and resets after each event is added.
     */
    public void addEvent(Event evt) {
        this.eventQueue.get().add(evt);

        if (!this.enqueEvents) {
            this.fireEventQueue();
            this.resetEventQueue();
        }
    }

    public void resetEventQueue() {
        eventQueue.set(new ArrayDeque<Event>());
    }

    public void fireEventQueue() {
        Deque<Event> deque = eventQueue.get();

        // This array
        for (Event event = deque.pollFirst(); event != null; event = deque.pollFirst()) {
            event.fireEvent(this.graph.getListenerIterator());
        }
    }
}
