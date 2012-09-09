package com.tinkerpop.blueprints.impls.datomic;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.TimeAwareEdge;
import com.tinkerpop.blueprints.TimeAwareFilter;
import com.tinkerpop.blueprints.TimeAwareVertex;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import com.tinkerpop.blueprints.util.StringFactory;
import datomic.Database;
import datomic.Util;

/**
 * @author Davy Suvee (http://datablend.be)
 */
public class DatomicEdge extends DatomicElement implements TimeAwareEdge {

    public DatomicEdge(final DatomicGraph datomicGraph, final Database database) {
        super(datomicGraph, database);
        datomicGraph.addToTransaction(Util.map(":db/id", id,
                                               ":graph.element/type", ":graph.element.type/edge",
                                               ":db/ident", uuid));
    }

    public DatomicEdge(final DatomicGraph datomicGraph, final Database database, final Object id) {
        super(datomicGraph, database);
        this.id = id;
    }

    @Override
    public TimeAwareEdge getPreviousVersion() {
        // Retrieve the previous version time id
        Object previousTimeId = DatomicUtil.getPreviousTransaction(datomicGraph, this);
        if (previousTimeId != null) {
            // Create a new version of the edge timescoped to the previous time id
            return new DatomicEdge(datomicGraph, datomicGraph.getRawGraph(previousTimeId), id);
        }
        return null;
    }

    @Override
    public TimeAwareEdge getNextVersion() {
        // Retrieve the next version time id
        Object nextTimeId = DatomicUtil.getNextTransactionId(datomicGraph, this);
        if (nextTimeId != null) {
            // Create a new version of the edge timescoped to the next time id
            DatomicEdge nextVertexVersion = new DatomicEdge(datomicGraph, datomicGraph.getRawGraph(nextTimeId), id);
            // If no next version exists, the version of the edge is the current version (timescope with a null database)
            if (DatomicUtil.getNextTransactionId(datomicGraph, nextVertexVersion) == null) {
                return new DatomicEdge(datomicGraph, null, id);
            }
            else {
                return nextVertexVersion;
            }
        }
        return null;
    }

    @Override
    public Iterable<TimeAwareEdge> getNextVersions() {
        return new DatomicTimeIterable(this, true);
    }

    @Override
    public Iterable<TimeAwareEdge> getPreviousVersions() {
        return new DatomicTimeIterable(this, false);
    }

    @Override
    public Iterable<TimeAwareEdge> getPreviousVersions(TimeAwareFilter timeAwareFilter) {
        return new DatomicTimeIterable(this, false, timeAwareFilter);
    }

    @Override
    public Iterable<TimeAwareEdge> getNextVersions(TimeAwareFilter timeAwareFilter) {
        return new DatomicTimeIterable(this, true, timeAwareFilter);
    }

    @Override
    public TimeAwareVertex getVertex(Direction direction) throws IllegalArgumentException {
        if (direction.equals(Direction.OUT))
            return new DatomicVertex(datomicGraph, database, getDatabase().datoms(Database.EAVT, getId(), datomicGraph.GRAPH_EDGE_OUT_VERTEX).iterator().next().v());
        else if (direction.equals(Direction.IN))
            return new DatomicVertex(datomicGraph, database, getDatabase().datoms(Database.EAVT, getId(), datomicGraph.GRAPH_EDGE_IN_VERTEX).iterator().next().v());
        else
            throw ExceptionFactory.bothIsNotSupported();
    }

    @Override
    public String getLabel() {
        return (String)getDatabase().datoms(Database.EAVT, getId(), datomicGraph.GRAPH_EDGE_LABEL).iterator().next().v();
    }

    @Override
    public String toString() {
        return StringFactory.edgeString(this);
    }

}
