package com.tinkerpop.blueprints.impls.oraclekv;

import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.Key;
import oracle.kv.Value;
import oracle.kv.ValueVersion;
import com.tinkerpop.blueprints.impls.oraclekv.*;
import static com.tinkerpop.blueprints.impls.oraclekv.util.KVUtil.*;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Query;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.DefaultQuery;
import com.tinkerpop.blueprints.util.MultiIterable;
import com.tinkerpop.blueprints.util.StringFactory;
import java.util.*;
// import java.util.Arrays;
// import java.util.Iterator;
// import java.util.List;
// import java.util.UUID;
// import java.util.Set;

/**
 * @author Dan McClary
 */
public class KVVertex extends KVElement implements Vertex {
    private final KVStore store;
    
    public KVVertex(final KVGraph graph) {
        super(graph);
        this.store = graph.getRawGraph();
        this.id = graph.getGraphKey() + "/Vertex/"+UUID.randomUUID().toString();
    }

    public KVVertex(final KVGraph graph, final Object id) {
        super(graph);
        this.store = graph.getRawGraph();
        this.id = graph.getGraphKey() + "/Vertex/"+id.toString();
    }

    /**
     * Return the edges incident to the vertex according to the provided direction and edge labels.
     *
     * @param direction the direction of the edges to retrieve
     * @param labels    the labels of the edges to retrieve
     * @return an iterable of incident edges
     */
    // public Iterable<Edge> getEdges(Direction direction, String... labels);

    /**
     * Return the vertices adjacent to the vertex according to the provided direction and edge labels.
     *
     * @param direction the direction of the edges of the adjacent vertices
     * @param labels    the labels of the edges of the adjacent vertices
     * @return an iterable of adjacent vertices
     */
    // public Iterable<Vertex> getVertices(Direction direction, String... labels);    

    private Iterable<Edge> getOutEdges(final String... labels) {
        /*get the set of Edge IDs that are outbound */
        Set<String> outEdgeIds = (Set<String>)getValue(this.store, keyFromString(this.id+"/OUT"));
        ArrayList<Edge> outEdges = new ArrayList<Edge>();
        for(String edgeId : outEdgeIds)
        {
            /*Get the edges for the collection*/
            KVEdge e = new KVEdge(this.graph, edgeId);
            if (Arrays.asList(labels).contains(e.getLabel()))
                outEdges.add((Edge)e);
        }
        return outEdges;
    }
    
    private Iterable<Edge> getInEdges(final String... labels) {
    }
    public String toString()
    {
        return this.id.toString();
    }
    
    @Override
    public Query query() {
        return new DefaultQuery(this);
    }


}