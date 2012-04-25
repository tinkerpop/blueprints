package com.tinkerpop.blueprints.pgm.impls.neo4j;


import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Filter;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.FilteredEdgeIterable;
import com.tinkerpop.blueprints.pgm.impls.MultiIterable;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.impls.neo4j.util.Neo4jEdgeSequence;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jVertex extends Neo4jElement implements Vertex {

    public Neo4jVertex(final Node node, final Neo4jGraph graph) {
        super(graph);
        this.rawElement = node;

    }

    public Iterable<Edge> getInEdges(final Object... filters) {
        if (filters.length == 0)
            return new Neo4jEdgeSequence(((Node) this.rawElement).getRelationships(Direction.INCOMING), this.graph);
        else if (filters.length == 1) {
            if (filters[0] instanceof String)
                return new Neo4jEdgeSequence(((Node) this.rawElement).getRelationships(DynamicRelationshipType.withName((String) filters[0]), Direction.INCOMING), this.graph);
            else if (filters[0] instanceof Filter)
                return new FilteredEdgeIterable(new Neo4jEdgeSequence(((Node) this.rawElement).getRelationships(Direction.INCOMING), this.graph), FilteredEdgeIterable.getFilter(filters));
            else
                throw new IllegalArgumentException(Vertex.TYPE_ERROR_MESSAGE);
        } else {
            final List<Iterable<Edge>> edges = new ArrayList<Iterable<Edge>>();
            int counter = 0;
            for (final Object filter : filters) {
                if (filter instanceof String) {
                    edges.add(new Neo4jEdgeSequence(((Node) this.rawElement).getRelationships(DynamicRelationshipType.withName((String) filter), Direction.INCOMING), this.graph));
                    counter++;
                }
            }
            if (edges.size() == filters.length)
                return new MultiIterable<Edge>(edges);
            else if (counter == 0)
                return new FilteredEdgeIterable(new Neo4jEdgeSequence(((Node) this.rawElement).getRelationships(Direction.INCOMING), this.graph), FilteredEdgeIterable.getFilter(filters));
            else
                return new FilteredEdgeIterable(new MultiIterable<Edge>(edges), FilteredEdgeIterable.getFilter(filters));
        }
    }

    public Iterable<Edge> getOutEdges(final Object... filters) {
        if (filters.length == 0)
            return new Neo4jEdgeSequence(((Node) this.rawElement).getRelationships(Direction.OUTGOING), this.graph);
        else if (filters.length == 1) {
            if (filters[0] instanceof String)
                return new Neo4jEdgeSequence(((Node) this.rawElement).getRelationships(DynamicRelationshipType.withName((String) filters[0]), Direction.OUTGOING), this.graph);
            else if (filters[0] instanceof Filter)
                return new FilteredEdgeIterable(new Neo4jEdgeSequence(((Node) this.rawElement).getRelationships(Direction.OUTGOING), this.graph), FilteredEdgeIterable.getFilter(filters));
            else
                throw new IllegalArgumentException(Vertex.TYPE_ERROR_MESSAGE);
        } else {
            final List<Iterable<Edge>> edges = new ArrayList<Iterable<Edge>>();
            int counter = 0;
            for (final Object filter : filters) {
                if (filter instanceof String) {
                    edges.add(new Neo4jEdgeSequence(((Node) this.rawElement).getRelationships(DynamicRelationshipType.withName((String) filter), Direction.OUTGOING), this.graph));
                    counter++;
                }
            }
            if (edges.size() == filters.length)
                return new MultiIterable<Edge>(edges);
            else if (counter == 0)
                return new FilteredEdgeIterable(new Neo4jEdgeSequence(((Node) this.rawElement).getRelationships(Direction.OUTGOING), this.graph), FilteredEdgeIterable.getFilter(filters));
            else
                return new FilteredEdgeIterable(new MultiIterable<Edge>(edges), FilteredEdgeIterable.getFilter(filters));
        }
    }


    public boolean equals(final Object object) {
        return object instanceof Neo4jVertex && ((Neo4jVertex) object).getId().equals(this.getId());
    }

    public String toString() {
        return StringFactory.vertexString(this);
    }

    public Node getRawVertex() {
        return (Node) this.rawElement;
    }

}
