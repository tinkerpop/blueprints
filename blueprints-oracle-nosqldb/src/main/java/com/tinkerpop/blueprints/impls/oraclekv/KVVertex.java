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
import com.tinkerpop.blueprints.util.VerticesFromEdgesIterable;

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
    private final String idString;
    
    public KVVertex(final KVGraph graph) {
        super(graph);
        this.store = graph.getRawGraph();
        this.idString = UUID.randomUUID().toString();
        this.id = graph.getGraphKey() + "/Vertex/"+this.idString;
    }

    public KVVertex(final KVGraph graph, final Object id) {
        super(graph);
        this.store = graph.getRawGraph();
        this.idString = id.toString();
        this.id = graph.getGraphKey() + "/Vertex/"+this.idString;
    }
    
    public Object getId()
    {
        return this.idString;
    }
    
    public boolean exists()
    {
    	if (graph.getRawGraph().get(keyFromString(this.id.toString()+"/ID")) != null)
    			return true;
    	else
    		return false;
    }

    /**
     * Return the edges incident to the vertex according to the provided direction and edge labels.
     *
     * @param direction the direction of the edges to retrieve
     * @param labels    the labels of the edges to retrieve
     * @return an iterable of incident edges
     */
    public Iterable<Edge> getEdges(Direction direction, String... labels)
    {
    	Iterable<Edge> edges = null;
    	if (direction == Direction.OUT)
    	{
    		edges = this.getOutEdges(labels);
    	}
    	
    	else if (direction == Direction.IN)
    	{
    		edges = this.getInEdges(labels);
    	}
    	
    	else if (direction == Direction.BOTH)
    	{
    		edges = this.getOutEdges(labels);
    		if (edges != null)
    		{
    		    Iterable<Edge> inEdges = this.getInEdges(labels);
    		    if (inEdges != null)
    		        ((ArrayList<Edge>)edges).addAll((ArrayList<Edge>)inEdges);
		    }
    	}
    	
    	return edges;
    }

    /**
     * Return the vertices adjacent to the vertex according to the provided direction and edge labels.
     *
     * @param direction the direction of the edges of the adjacent vertices
     * @param labels    the labels of the edges of the adjacent vertices
     * @return an iterable of adjacent vertices
     */
   
    
    public Iterable<Vertex> getVertices(Direction direction, String... labels) {
        return this.verticesFromEdgesIterable(this, direction, labels);
    }
    
    public Iterable<Vertex> verticesFromEdgesIterable(KVVertex v, Direction direction, String... labels)
    {
    	ArrayList<Vertex> vertices = new ArrayList<Vertex>();
    	if (direction == Direction.OUT)
    	{
    		/* get the outbound edges */
    		ArrayList<Edge> edgeList = this.getOutEdges(labels);
    		/* for each edge, get the OUT node */
    		for (Edge e : edgeList)
    		{
    			vertices.add(e.getVertex(Direction.IN));
    		}
    		
    	}
    	
    	else if (direction == Direction.IN)
    	{
    		/* get the inbound edges */
    		ArrayList<Edge> edgeList = this.getInEdges(labels);
    		/* for each edge, get the IN node */
    		for (Edge e : edgeList)
    		{
    			vertices.add(e.getVertex(Direction.OUT));
    		}
    		
    	}
    	
    	else if (direction == Direction.BOTH)
    	{
    		/* get the outbound edges */
        	ArrayList<Edge> edgeList = this.getOutEdges(labels);
        	/* for each edge, get the OUT node */
        	for (Edge e : edgeList)
        	{
    			vertices.add(e.getVertex(Direction.IN));
        	}
        	
        	edgeList = this.getInEdges(labels);
        	/* for each edge, get the IN node */
        	for (Edge e : edgeList)
        	{
        		vertices.add(e.getVertex(Direction.OUT));
        	}
    	}
    	
    	return vertices;
    }
    
    private ArrayList<Edge> getOutEdges(final String... labels) {
        /*get the set of Edge IDs that are outbound */
        Set<String> outEdgeIds = (Set<String>)this.getProperty("OUT");
        if (outEdgeIds == null)
            outEdgeIds = new HashSet<String>();

        ArrayList<Edge> outEdges = new ArrayList<Edge>();
        if (!outEdgeIds.isEmpty())
        {
            for(String edgeId : outEdgeIds)
            {
                /*Get the edges for the collection*/
                KVEdge e = new KVEdge(this.graph, edgeId);

                if (labels.length == 0)
                	outEdges.add((Edge)e);
                else if (labels.length == 1)
                {
                	if (e.getLabel().equals(labels[0]))
                		outEdges.add((Edge)e);
            	}
            	
                else
                {

                	if (Arrays.asList(labels).contains(e.getLabel()))
                	{
                		outEdges.add((Edge)e);
            		}
            	}
                
            }
        }
        return outEdges;
    }
    
    private ArrayList<Edge> getInEdges(final String... labels) {
    	/*get the set of Edge IDs that are inbound */
        Set<String> inEdgeIds = (Set<String>)this.getProperty("IN");
        if (inEdgeIds == null)
            inEdgeIds = new HashSet<String>();
            
        ArrayList<Edge> inEdges = new ArrayList<Edge>();
        if (!inEdgeIds.isEmpty())
        {
            for(String edgeId : inEdgeIds)
            {
                /*Get the edges for the collection*/

                KVEdge e = new KVEdge(this.graph, edgeId);

                if (labels.length == 0)
                	inEdges.add((Edge)e);
                else if (labels.length == 1)
                {
                	if (e.getLabel().equals(labels[0]))
                		inEdges.add((Edge)e);
            	}
            	
                else
                {

                	if (Arrays.asList(labels).contains(e.getLabel()))
                	{
                		inEdges.add((Edge)e);
            		}
            	}
            }
        }
        return inEdges;
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