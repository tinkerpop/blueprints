package com.tinkerpop.blueprints.pgm.impls.event.listener;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;

public interface GraphChangedListener {
    public void vertexAdded(final Vertex vertex);
    public void vertexPropertyChanged(final Vertex vertex, final String key, final Object setValue);
    public void vertexPropertyRemoved(final Vertex vertex, final String key, final Object removedValue);
    public void vertexRemoved(final Vertex vertex);

    public void edgeAdded(final Edge edge);
    public void edgePropertyChanged(final Edge edge, final String key, final Object setValue);
    public void edgePropertyRemoved(final Edge edge, final String key, final Object removedValue);
    public void edgeRemoved(final Edge edge);
}
