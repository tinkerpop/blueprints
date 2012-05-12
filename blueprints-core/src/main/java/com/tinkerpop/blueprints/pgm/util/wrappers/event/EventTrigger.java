package com.tinkerpop.blueprints.pgm.util.wrappers.event;

import com.tinkerpop.blueprints.pgm.util.wrappers.event.listener.Event;

import java.util.ArrayList;
import java.util.List;

class EventTrigger {
    private final ThreadLocal<List<Event>> eventBuffer = new ThreadLocal<List<Event>>(){
        protected List<Event> initialValue() {
            return new ArrayList<Event>();
        }
    };
    
    private final boolean triggerFromTransaction;
    
    private final EventGraph graph;
    
    public EventTrigger(final EventGraph graph, final boolean triggerFromTransaction) {
        this.triggerFromTransaction = triggerFromTransaction;
        this.graph = graph;
    }
    
    public void addEvent(Event evt) {
        this.eventBuffer.get().add(evt);

        if (!this.triggerFromTransaction) {
            this.fireEventBuffer();
            this.resetEventBuffer();
        }
    }
    
    public ThreadLocal<List<Event>> getEventBuffer() {
        return this.eventBuffer;
    }

    public void resetEventBuffer() {
        eventBuffer.set(new ArrayList<Event>());
    }

    public void fireEventBuffer() {
        for (Event event : eventBuffer.get()) {
            event.fireEvent(this.graph.getListenerIterator());
        }
    }
}
