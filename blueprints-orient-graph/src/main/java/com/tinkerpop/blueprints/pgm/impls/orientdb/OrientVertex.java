package com.tinkerpop.blueprints.pgm.impls.orientdb;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Filter;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.FilteredEdgeIterable;
import com.tinkerpop.blueprints.pgm.impls.MultiIterable;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.impls.orientdb.util.OrientElementSequence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientVertex extends OrientElement implements Vertex {

    public OrientVertex() {
        super(null, new ODocument());
    }

    public OrientVertex(final OrientGraph rawGraph, final ODocument rawVertex) {
        super(rawGraph, rawVertex);
    }

    public Iterable<Edge> getOutEdges(final Object... filters) {
        if (this.rawElement == null)
            return Collections.emptyList();

        if (filters.length == 0)
            return new OrientElementSequence<Edge>(graph, graph.getRawGraph().getOutEdges(this.rawElement, null).iterator());
        else if (filters.length == 1) {
            if (filters[0] instanceof String)
                return new OrientElementSequence<Edge>(graph, graph.getRawGraph().getOutEdges(this.rawElement, (String) filters[0]).iterator());
            else if (filters[0] instanceof Filter)
                return new FilteredEdgeIterable(new OrientElementSequence<Edge>(graph, graph.getRawGraph().getOutEdges(this.rawElement, null).iterator()), FilteredEdgeIterable.getFilter(filters));
            else
                throw new IllegalArgumentException(Vertex.TYPE_ERROR_MESSAGE);
        } else {
            final List<Iterable<Edge>> edges = new ArrayList<Iterable<Edge>>();
            int counter = 0;
            for (final Object filter : filters) {
                if (filter instanceof String) {
                    edges.add(new OrientElementSequence<Edge>(graph, graph.getRawGraph().getOutEdges(this.rawElement, (String) filter).iterator()));
                    counter++;
                }
            }
            if (edges.size() == filters.length)
                return new MultiIterable<Edge>(edges);
            else if (counter == 0)
                return new FilteredEdgeIterable(new OrientElementSequence<Edge>(graph, graph.getRawGraph().getOutEdges(this.rawElement, null).iterator()), FilteredEdgeIterable.getFilter(filters));
            else
                return new FilteredEdgeIterable(new MultiIterable<Edge>(edges), FilteredEdgeIterable.getFilter(filters));
        }
    }

    public Iterable<Edge> getInEdges(final Object... filters) {
        if (this.rawElement == null)
            return Collections.emptyList();

        if (filters.length == 0)
            return new OrientElementSequence<Edge>(graph, graph.getRawGraph().getInEdges(this.rawElement, null).iterator());
        else if (filters.length == 1) {
            if (filters[0] instanceof String)
                return new OrientElementSequence<Edge>(graph, graph.getRawGraph().getInEdges(this.rawElement, (String) filters[0]).iterator());
            else if (filters[0] instanceof Filter)
                return new FilteredEdgeIterable(new OrientElementSequence<Edge>(graph, graph.getRawGraph().getInEdges(this.rawElement, null).iterator()), FilteredEdgeIterable.getFilter(filters));
            else
                throw new IllegalArgumentException(Vertex.TYPE_ERROR_MESSAGE);
        } else {
            final List<Iterable<Edge>> edges = new ArrayList<Iterable<Edge>>();
            int counter = 0;
            for (final Object filter : filters) {
                if (filter instanceof String) {
                    edges.add(new OrientElementSequence<Edge>(graph, graph.getRawGraph().getInEdges(this.rawElement, (String) filter).iterator()));
                    counter++;
                }
            }
            if (edges.size() == filters.length)
                return new MultiIterable<Edge>(edges);
            else if (counter == 0)
                return new FilteredEdgeIterable(new OrientElementSequence<Edge>(graph, graph.getRawGraph().getInEdges(this.rawElement, null).iterator()), FilteredEdgeIterable.getFilter(filters));
            else
                return new FilteredEdgeIterable(new MultiIterable<Edge>(edges), FilteredEdgeIterable.getFilter(filters));
        }
    }

    public Set<String> getPropertyKeys() {
        final Set<String> set = super.getPropertyKeys();
        if (set.size() > 0) {
            set.remove(OGraphDatabase.VERTEX_FIELD_IN);
            set.remove(OGraphDatabase.VERTEX_FIELD_OUT);
        }
        return set;
    }

    public String toString() {
        return StringFactory.vertexString(this);
    }
}
