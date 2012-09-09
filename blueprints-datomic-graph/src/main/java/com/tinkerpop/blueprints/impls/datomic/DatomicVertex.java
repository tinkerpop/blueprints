package com.tinkerpop.blueprints.impls.datomic;

import com.tinkerpop.blueprints.*;
import com.tinkerpop.blueprints.util.DefaultQuery;
import com.tinkerpop.blueprints.util.MultiIterable;
import com.tinkerpop.blueprints.util.StringFactory;
import datomic.*;
import java.util.*;

/**
 * @author Davy Suvee (http://datablend.be)
 */
public class DatomicVertex extends DatomicElement implements TimeAwareVertex {

    protected DatomicVertex(final DatomicGraph datomicGraph, final Database database) {
        super(datomicGraph, database);
        datomicGraph.addToTransaction(Util.map(":db/id", id,
                                              ":graph.element/type", ":graph.element.type/vertex",
                                              ":db/ident", uuid));
    }

    public DatomicVertex(final DatomicGraph datomicGraph, final Database database, final Object id) {
        super(datomicGraph, database);
        this.id = id;
    }

    @Override
    public TimeAwareVertex getPreviousVersion() {
        // Retrieve the previous version time id
        Object previousTimeId = DatomicUtil.getPreviousTransaction(datomicGraph, this);
        if (previousTimeId != null) {
            // Create a new version of the vertex timescoped to the previous time id
            return new DatomicVertex(datomicGraph, datomicGraph.getRawGraph(previousTimeId), id);
        }
        return null;
    }

    @Override
    public TimeAwareVertex getNextVersion() {
        // Retrieve the next version time id
        Object nextTimeId = DatomicUtil.getNextTransactionId(datomicGraph, this);
        if (nextTimeId != null) {
            DatomicVertex nextVertexVersion = new DatomicVertex(datomicGraph, datomicGraph.getRawGraph(nextTimeId), id);
            // If no next version exists, the version of the edge is the current version (timescope with a null database)
            if (DatomicUtil.getNextTransactionId(datomicGraph, nextVertexVersion) == null) {
                return new DatomicVertex(datomicGraph, null, id);
            }
            else {
                return nextVertexVersion;
            }
        }
        return null;
    }

    @Override
    public Iterable<TimeAwareVertex> getNextVersions() {
        return new DatomicTimeIterable(this, true);
    }

    @Override
    public Iterable<TimeAwareVertex> getPreviousVersions() {
        return new DatomicTimeIterable(this, false);
    }

    @Override
    public Iterable<TimeAwareVertex> getPreviousVersions(TimeAwareFilter timeAwareFilter) {
        return new DatomicTimeIterable(this, false, timeAwareFilter);
    }

    @Override
    public Iterable<TimeAwareVertex> getNextVersions(TimeAwareFilter timeAwareFilter) {
        return new DatomicTimeIterable(this, true, timeAwareFilter);
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
            return new DatomicIterable(vertices, datomicGraph, database, Vertex.class);
        } else if (direction.equals(Direction.IN)) {
            Iterator<Edge> edgesit = this.getInEdges(labels).iterator();
            List<Object> vertices = new ArrayList<Object>();
            while (edgesit.hasNext()) {
                vertices.add(edgesit.next().getVertex(Direction.OUT).getId());
            }
            return new DatomicIterable(vertices, datomicGraph, database, Vertex.class);
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
            return new MultiIterable<Vertex>(Arrays.<Iterable<Vertex>>asList(new DatomicIterable(outvertices, datomicGraph, database, Vertex.class), new DatomicIterable(invertices, datomicGraph, database, Vertex.class)));
        }
    }

    @Override
    public String toString() {
        return StringFactory.vertexString(this);
    }

    @Override
    public Query query() {
        return new DefaultQuery(this);
    }

    private Iterable<Edge> getInEdges(final String... labels) {
        if (labels.length == 0) {
            return getInEdges();
        }
        Collection<List<Object>> inEdges = Peer.q("[:find ?edge " +
                                                   ":in $ ?vertex [?label ...] " +
                                                   ":where [?edge :graph.edge/inVertex ?vertex] " +
                                                          "[?edge :graph.edge/label ?label ] ]", getDatabase(), id, labels);
        return new DatomicIterable(inEdges, datomicGraph, database, Edge.class);
    }

    private Iterable<Edge> getInEdges() {
        Iterable<Datom> inEdges = getDatabase().datoms(Database.AVET, datomicGraph.GRAPH_EDGE_IN_VERTEX, getId());
        return new DatomicIterable(inEdges, datomicGraph, database, Edge.class);
    }

    private Iterable<Edge> getOutEdges(final String... labels) {
        if (labels.length == 0) {
            return getOutEdges();
        }
        Collection<List<Object>> outEdges = Peer.q("[:find ?edge " +
                                                    ":in $ ?vertex [?label ...] " +
                                                    ":where [?edge :graph.edge/outVertex ?vertex] " +
                                                           "[?edge :graph.edge/label ?label ] ]", getDatabase(), id, labels);
        return new DatomicIterable(outEdges, datomicGraph, database, Edge.class);
    }

    private Iterable<Edge> getOutEdges() {
        Iterable<Datom> outEdges = getDatabase().datoms(Database.AVET, datomicGraph.GRAPH_EDGE_OUT_VERTEX, getId());
        return new DatomicIterable(outEdges, datomicGraph, database, Edge.class);
    }

}
