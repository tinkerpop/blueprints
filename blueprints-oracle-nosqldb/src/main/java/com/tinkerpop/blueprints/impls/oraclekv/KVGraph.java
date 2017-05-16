package com.tinkerpop.blueprints.impls.oraclekv;

import oracle.kv.Depth;
import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.Key;
import oracle.kv.Value;
import oracle.kv.KeyRange;
import oracle.kv.KeyValueVersion;
import oracle.kv.Operation;
import oracle.kv.OperationFactory;
import oracle.kv.ValueVersion;
import org.apache.commons.lang.StringUtils;
import com.tinkerpop.blueprints.impls.oraclekv.util.KVUtil;
import static com.tinkerpop.blueprints.impls.oraclekv.util.KVUtil.*;
import com.tinkerpop.blueprints.*;
// import com.tinkerpop.blueprints.impls.oraclenosqldb.util.KVUtil;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import com.tinkerpop.blueprints.util.StringFactory;

// import static com.tinkerpop.blueprints.impls.oraclenosqldb.util.KVUtil.*;

import java.net.UnknownHostException;
import java.util.*;

/**
 * A Blueprints implementation for the Oracle NoSQL Database 
 *
 * @author Dan McClary
 */
public class KVGraph implements MetaGraph<KVStore> {
    private final KVStore store;
    protected final String storestring;
    protected final String hostname;
    private final String graphKeyString;
    public static final String NOSQLDB_ERROR_EXCEPTION_MESSAGE = "An error occured within the Oracle NoSQL DB datastore";

    private static final Features FEATURES = new Features();

    /**
    * Construct a graph on top of Oracle NoSQL DB
    */
    public KVGraph(final String graphName, final String storeName, final String hostName, final int hostPort) {
       graphKeyString = graphName;
       storestring = storeName;
       hostname = hostName;
       try {
           store = KVStoreFactory.getStore
               (new KVStoreConfig(storestring, hostName + ":" + hostPort));
               
       } catch (Exception e) {
               throw new RuntimeException(KVGraph.NOSQLDB_ERROR_EXCEPTION_MESSAGE);
       }
    }
    
    static {
        FEATURES.supportsDuplicateEdges = true;
        FEATURES.supportsSelfLoops = true;
        FEATURES.isPersistent = true;
        FEATURES.isRDFModel = false;
        FEATURES.supportsVertexIteration = true;
        FEATURES.supportsEdgeIteration = true;
        FEATURES.supportsVertexIndex = false;
        FEATURES.supportsEdgeIndex = false;
        FEATURES.ignoresSuppliedIds = false;
        FEATURES.supportsEdgeRetrieval = true;
        FEATURES.supportsVertexProperties = true;
        FEATURES.supportsEdgeProperties = true;
        FEATURES.supportsTransactions = false;
        FEATURES.supportsIndices = false;

        FEATURES.supportsSerializableObjectProperty = true;
        FEATURES.supportsBooleanProperty = true;
        FEATURES.supportsDoubleProperty = true;
        FEATURES.supportsFloatProperty = true;
        FEATURES.supportsIntegerProperty = true;
        FEATURES.supportsPrimitiveArrayProperty = true;
        FEATURES.supportsUniformListProperty = true;
        FEATURES.supportsMixedListProperty = true;
        FEATURES.supportsLongProperty = true;
        FEATURES.supportsMapProperty = true;
        FEATURES.supportsStringProperty = true;

        FEATURES.isWrapper = true;
        FEATURES.supportsKeyIndices = false;
        FEATURES.supportsVertexKeyIndex = false;
        FEATURES.supportsEdgeKeyIndex = false;
        FEATURES.supportsThreadedTransactions = false;
    }

    public void shutdown() {
        store.close();
    }
    
    @Override
    public String toString()
    {
        return this.getClass().getSimpleName().toLowerCase();
    }

    @Override
    public Features getFeatures() {
        return FEATURES;
    }
    
    public String getGraphKey()
    {
        return this.graphKeyString;
    }
    
    @Override
    public KVStore getRawGraph()
    {
        return store;
    }
    

    /**
     * Create a new vertex, add it to the graph, and return the newly created vertex.
     * The provided object identifier is a recommendation for the identifier to use.
     * It is not required that the implementation use this identifier.
     *
     * @param id the recommended object identifier
     * @return the newly created vertex
     */
     
    public Vertex addVertex(Object id)
    {
        /* get a new vertex and its uuid*/
        KVVertex vertex;
        if (id != null) 
        {
            vertex = new KVVertex(this, id);
        }
        else
        {
            vertex = new KVVertex(this);
        }
        putValue(this.store, keyFromString(this.getGraphKey()+"/VertexIndex/"+vertex.getId()), "");
        vertex.setProperty("ID", vertex.getId().toString());
        
        return vertex;
    }
        
    public Vertex getVertex(final Object id) {
        Vertex vert = null;
    	if (null == id)
            throw ExceptionFactory.edgeIdCanNotBeNull();
        
        try {
            final String theId = id.toString();
            KVVertex v = new KVVertex(this, theId);
            if (v.exists())
            	vert = (Vertex)v;
        } 
        
        catch (Exception e) {
                return null;
        }
        return vert;
    }
    
    
    public void removeVertex(Vertex v) {
        KVVertex vertexToRemove = new KVVertex(this, v.getId());
        Iterable<Edge> edgesToRemove = vertexToRemove.getEdges(Direction.BOTH);
        if (edgesToRemove != null)
        {
            for (Edge edge : edgesToRemove)
            {
            	removeEdge(edge);
            }
        }
        /* use multiDelete to wipe out all properties */
        Key vertexKey = majorKeyFromString(this.getGraphKey()+"/Vertex/"+vertexToRemove.getId());
        this.store.multiDelete(vertexKey, null, Depth.DESCENDANTS_ONLY);
        this.store.delete(keyFromString(this.getGraphKey()+"/VertexIndex/"+vertexToRemove.getId()));
    }
    /**
     * Return the edge referenced by the provided object identifier.
     * If no edge is referenced by that identifier, then return null.
     *
     * @param id the identifier of the edge to retrieved from the graph
     * @return the edge referenced by the provided identifier or null when no such edge exists
     */
    public Edge getEdge(Object id)
    {
    	Edge edge = null;
     	if (null == id)
             throw ExceptionFactory.edgeIdCanNotBeNull();
         
         try {
             final String theId = id.toString();
             KVEdge e = new KVEdge(this, theId);
             if (e.exists())
             	edge = (Edge)e;
         } 
         
         catch (Exception e) {
                 return null;
         }
         return edge;
    }
    
    /**
     * Remove the provided edge from the graph.
     *
     * @param edge the edge to remove from the graph
     */
    public void removeEdge(Edge edge)
    {
    	 KVEdge edgeToRemove = new KVEdge(this, edge.getId());
         
         
    	 /* for the IN Vertex, remove the edge ID */
    	 String inVString = (String)edgeToRemove.getProperty("IN");
         Set<String> edgelist;
    	 if (inVString != null)
    	 {
        	 KVVertex inV = new KVVertex(this, inVString);
    	 
             edgelist= (Set<String>)inV.getProperty("IN");
        	 if (edgelist != null)
        	 {
            	 edgelist.remove(edgeToRemove.getId());
            	 inV.setProperty("IN", edgelist);
        	 }
    	 }
    	 
    	 
    	 /* for the OUT Vertex, remove the edge ID */
    	 String outVString = (String)edgeToRemove.getProperty("OUT");
    	 if (outVString != null)
    	 {
        	 KVVertex outV = new KVVertex(this, outVString);
        	 edgelist = (Set<String>)outV.getProperty("OUT");
        	 if (edgelist != null)
        	 {
        	     edgelist.remove(edgeToRemove.getId());
        	     outV.setProperty("OUT",edgelist);
    	     }
	     }
    	 
    	 
         /* use multiDelete to wipe out all properties */
         Key edgeKey = majorKeyFromString(this.getGraphKey()+"/Edge/"+edgeToRemove.getId());
         this.store.multiDelete(edgeKey, null, Depth.DESCENDANTS_ONLY);
         this.store.delete(keyFromString(this.getGraphKey()+"/EdgeIndex/"+edgeToRemove.getId()));
    }
    
    /**
     * Add an edge to the graph. The added edges requires a recommended identifier, a tail vertex, an head vertex, and a label.
     * Like adding a vertex, the provided object identifier may be ignored by the implementation.
     *
     * @param id        the recommended object identifier
     * @param outVertex the vertex on the tail of the edge
     * @param inVertex  the vertex on the head of the edge
     * @param label     the label associated with the edge
     * @return the newly created edge
     */
    public Edge addEdge(Object id, Vertex outVertex, Vertex inVertex, String label)
    {
        /* get a new vertex and its uuid*/
        KVEdge edge;
        if (id != null)
        {
             edge = new KVEdge(this, id);
        }
        else
            edge = new KVEdge(this);
            
        edge.setProperty("ID", edge.getId());
        edge.setProperty("IN", inVertex.getId());
        edge.setProperty("OUT", outVertex.getId());
        edge.setProperty("LABEL", label);
        
        // System.out.println("Adding edge:"+inVertex.getId()+"<-"+label+"-"+edge.getId()+"<-"+outVertex.getId());
        /*for the In Vertex, add this edge ID to the set of IN Edges*/
        Set<String> inEdges = (Set<String>) inVertex.getProperty("IN");
        if (inEdges == null)
        	inEdges = new HashSet<String>();
        
        inEdges.add(edge.getId().toString());

        inVertex.setProperty("IN", inEdges);
        /* for the Out Vertex, add this edge ID to the set of OUT edges*/
        
        Set<String> outEdges = (Set<String>) outVertex.getProperty("OUT");
        if (outEdges == null)
        	outEdges = new HashSet<String>();
        outEdges.add(edge.getId().toString());
        outVertex.setProperty("OUT", outEdges);
        putValue(this.store, keyFromString(this.getGraphKey()+"/EdgeIndex/"+edge.getId()), "");
        return edge;
        
    } 
    /**
     * Return an iterable to all the vertices in the graph.
     * If this is not possible for the implementation, then an UnsupportedOperationException can be thrown.
     *
     * @return an iterable reference to all vertices in the graph
     */
    public Iterable<Vertex> getVertices()
    {
    	ArrayList<Vertex> vertices = new ArrayList<Vertex>();
    	/* get all immediate children of the graph with Vertex in the major key */
        Iterator <KeyValueVersion> vertexKeys = this.store.multiGetIterator(oracle.kv.Direction.FORWARD, 0,majorKeyFromString(this.getGraphKey()+"/VertexIndex"), null,null);
        // Iterator<Key> vertexKeys = this.store.storeKeysIterator(oracle.kv.Direction.UNORDERED, 1,majorKeyFromString(this.getGraphKey()+"/Vertex"), null, Depth.CHILDREN_ONLY);

    	int vcount = 0;
    	
    	while (vertexKeys.hasNext())
    	{
            vcount++;
            Key vk = vertexKeys.next().getKey();
            
    		List<String> vertexAddress = vk.getFullPath();
    		String vId = vertexAddress.get(vertexAddress.size()-1);
    		
    		Vertex vertex = this.getVertex(vId);
    		if (vertex != null)
    		{
    			vertices.add(vertex);
    			
    		}
    	}
        //System.out.println("There are "+vcount + " keys");
        //System.out.println("There are "+vertices.size() + " vertices");
    	
    	return vertices;
    }
    
    /**
     * Return an iterable to all the vertices in the graph that have a particular key/value property.
     * If this is not possible for the implementation, then an UnsupportedOperationException can be thrown.
     * The graph implementation should use indexing structures to make this efficient else a full vertex-filter scan is required.
     *
     * @param key   the key of vertex
     * @param value the value of the vertex
     * @return an iterable of vertices with provided key and value
     */
    public Iterable<Vertex> getVertices(String key, Object value)
    {
    	ArrayList<Vertex> vertices = new ArrayList<Vertex>();
    	/* get all immediate children of the graph with Vertex in the major key */
        // Iterator <KeyValueVersion> vertexKeys = this.store.multiGetIterator(oracle.kv.Direction.FORWARD, 0,majorKeyFromString(this.getGraphKey()+"/Vertex"), null,null);
        Iterator <KeyValueVersion> vertexKeys = this.store.multiGetIterator(oracle.kv.Direction.FORWARD, 0,majorKeyFromString(this.getGraphKey()+"/VertexIndex"), null,null);    	
        // Iterator<Key> vertexKeys = this.store.storeKeysIterator(oracle.kv.Direction.UNORDERED, 0,majorKeyFromString(this.getGraphKey()+"/Vertex"), null, Depth.CHILDREN_ONLY);
    	
    	while (vertexKeys.hasNext())
    	{
    		Key vk = vertexKeys.next().getKey();
    		List<String> vertexAddress = vk.getFullPath();
    		String vId = vertexAddress.get(vertexAddress.size()-1);
    		KVVertex vertex = new KVVertex(this, vId);
    		if (vertex.exists())
    		{
    			if (vertex.getPropertyKeys().contains(key))
    			{
    				if (vertex.getProperty(key).equals(value))
    					vertices.add(vertex);
    			}
    		}
    	}
    	
    	return vertices;
    }

    /**
     * Return an iterable to all the edges in the graph.
     * If this is not possible for the implementation, then an UnsupportedOperationException can be thrown.
     *
     * @return an iterable reference to all edges in the graph
     */
    public Iterable<Edge> getEdges()
    {
    	ArrayList<Edge> edges = new ArrayList<Edge>();
    	/* get all immediate children of the graph with Edge in the major key */
    	Iterator <KeyValueVersion> edgeKeys = this.store.multiGetIterator(oracle.kv.Direction.FORWARD, 0,majorKeyFromString(this.getGraphKey()+"/EdgeIndex"), null,null);
    	
        // Iterator<Key> edgeKeys = this.store.storeKeysIterator(oracle.kv.Direction.UNORDERED, 0,majorKeyFromString(this.getGraphKey()+"/Edge"), new KeyRange(null, true, null, true), Depth.CHILDREN_ONLY);
        int ecount = 0;
    	while (edgeKeys.hasNext())
    	{
    	    ecount++;
    		Key ek = edgeKeys.next().getKey();
    		List<String> edgeAddress = ek.getFullPath();
    		String eId = edgeAddress.get(edgeAddress.size()-1);
    		Edge edge = this.getEdge(eId);
    		if (edge != null)
    			edges.add(edge);
    	}
    	// System.out.println("ecount:"+ecount);
    	// System.out.println("edges:"+edges.size());
    	return edges;
    }
    
    /**
     * Return an iterable to all the edges in the graph that have a particular key/value property.
     * If this is not possible for the implementation, then an UnsupportedOperationException can be thrown.
     * The graph implementation should use indexing structures to make this efficient else a full edge-filter scan is required.
     *
     * @param key   the key of the edge
     * @param value the value of the edge
     * @return an iterable of edges with provided key and value
     */
    public Iterable<Edge> getEdges(String key, Object value)
    {
    	ArrayList<Edge> edges = new ArrayList<Edge>();
    	/* get all immediate children of the graph with Edge in the major key */
        Iterator <KeyValueVersion> edgeKeys = this.store.multiGetIterator(oracle.kv.Direction.FORWARD, 0,majorKeyFromString(this.getGraphKey()+"/EdgeIndex"), null,null);

        // Iterator<Key> edgeKeys = this.store.storeKeysIterator(oracle.kv.Direction.UNORDERED, 0,majorKeyFromString(this.getGraphKey()+"/Edge"), new KeyRange(null, true, null, true), Depth.CHILDREN_ONLY);
    	while (edgeKeys.hasNext())
    	{
    		Key ek = edgeKeys.next().getKey();
    		List<String> edgeAddress = ek.getFullPath();
    		String eId = edgeAddress.get(edgeAddress.size()-1);
    		KVEdge edge = new KVEdge(this, eId);
    		if (edge.exists())
    		{

    			if (edge.getPropertyKeys().contains(key))
    			{

    				if (edge.getProperty(key).equals(value))
    					edges.add(edge);
    			}
    		}
    	}
    	
    	return edges;
    }
    

}