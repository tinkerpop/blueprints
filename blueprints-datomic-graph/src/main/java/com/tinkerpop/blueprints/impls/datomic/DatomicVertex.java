package com.tinkerpop.blueprints.impls.datomic;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Query;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.DefaultQuery;
import com.tinkerpop.blueprints.util.MultiIterable;
import com.tinkerpop.blueprints.util.StringFactory;
import datomic.Database;
import datomic.Datom;
import datomic.Peer;
import datomic.Util;

import java.util.*;

/**
 * @author Davy Suvee (http://datablend.be)
 */
public class DatomicVertex extends DatomicElement implements Vertex {

    public DatomicVertex(final DatomicGraph graph) {
        super(graph);
        graph.addToTransaction(Util.map(":db/id", id,
                                        ":graph.element/type", ":graph.element.type/vertex",
                                        ":db/ident", uuid));
    }

    public DatomicVertex(final DatomicGraph graph, final Object id) {
        super(graph);
        this.id = id;
    }

    public Iterable<Edge> getInEdges(final String... labels) {
        if (labels.length == 0) {
            return getInEdges();
        }
        Collection<List<Object>> inEdges = Peer.q("[:find ?edge " +
                                                  ":in $ ?vertex [?label ...] " +
                                                  ":where [?edge :graph.edge/inVertex ?vertex] " +
                                                         "[?edge :graph.edge/label ?label ] ]", graph.getRawGraph(), id, labels);
        return new DatomicIterable(inEdges, graph, Edge.class);
    }

    public Iterable<Edge> getInEdges() {
        Iterable<Datom> inEdges = graph.getRawGraph().datoms(Database.AVET, graph.GRAPH_EDGE_IN_VERTEX, getId());
        return new DatomicIterable(inEdges, this.graph, Edge.class);
    }

    public Iterable<Edge> getOutEdges(final String... labels) {
        if (labels.length == 0) {
            return getOutEdges();
        }
        Collection<List<Object>> outEdges = Peer.q("[:find ?edge " +
                                                  ":in $ ?vertex [?label ...] " +
                                                  ":where [?edge :graph.edge/outVertex ?vertex] " +
                                                         "[?edge :graph.edge/label ?label ] ]", graph.getRawGraph(), id, labels);
        return new DatomicIterable(outEdges, graph, Edge.class);
    }

    public Iterable<Edge> getOutEdges() {
        Iterable<Datom> outEdges = graph.getRawGraph().datoms(Database.AVET, graph.GRAPH_EDGE_OUT_VERTEX, getId());
        return new DatomicIterable(outEdges, this.graph, Edge.class);
    }

    public boolean equals(final Object object) {
        return object instanceof DatomicVertex && ((DatomicVertex)object).getId().equals(this.getId());
    }

    public String toString() {
        return StringFactory.vertexString(this);
    }

    @Override
    public Iterable<Edge> getEdges(Direction direction, String... labels) {
        if (direction.equals(Direction.OUT)) {
            return this.getOutEdges(labels);
        } else if (direction.equals(Direction.IN))
            return this.getInEdges(labels);
        else {
            return new MultiIterable<Edge>(Arrays.asList(this.getInEdges(labels), this.getOutEdges(labels)));
        }
    }

    @Override
    public Iterable<Vertex> getVertices(Direction direction, String... labels) {
        if (direction.equals(Direction.OUT)) {
            Iterator<Edge> edgesit = this.getOutEdges(labels).iterator();
            List<Object> vertices = new ArrayList<Object>();
            while (edgesit.hasNext()) {
                vertices.add(edgesit.next().getVertex(Direction.IN).getId());
            }
            return new DatomicIterable(vertices, this.graph, Vertex.class);
        } else if (direction.equals(Direction.IN)) {
            Iterator<Edge> edgesit = this.getInEdges(labels).iterator();
            List<Object> vertices = new ArrayList<Object>();
            while (edgesit.hasNext()) {
                vertices.add(edgesit.next().getVertex(Direction.OUT).getId());
            }
            return new DatomicIterable(vertices, this.graph, Vertex.class);
        }
        else {
            Iterator<Edge> outEdgesIt = this.getOutEdges(labels).iterator();
            List<Object> outvertices = new ArrayList<Object>();
            while (outEdgesIt.hasNext()) {
                outvertices.add(outEdgesIt.next().getVertex(Direction.IN).getId());
            }
            Iterator<Edge> inEdgesIt = this.getInEdges(labels).iterator();
            List<Object> invertices = new ArrayList<Object>();
            while (inEdgesIt.hasNext()) {
                invertices.add(inEdgesIt.next().getVertex(Direction.OUT).getId());
            }
            return new MultiIterable<Vertex>(Arrays.<Iterable<Vertex>>asList(new DatomicIterable(outvertices, this.graph, Vertex.class), new DatomicIterable(invertices, this.graph, Vertex.class)));
        }
    }

    @Override
    public Query query() {
        return new DefaultQuery(this);
    }
}
