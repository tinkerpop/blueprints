package com.tinkerpop.blueprints.impls.oraclekv;

import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.Key;
import oracle.kv.Value;
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
public abstract class KVGraph implements MetaGraph<KVStore> {
    private final KVStore store;
    private final String graphKeyString;
    public static final String NOSQLDB_ERROR_EXCEPTION_MESSAGE = "An error occured within the Oracle NoSQL DB datastore";

    private static final Features FEATURES = new Features();

    /**
    * Construct a graph on top of Oracle NoSQL DB
    */
    public KVGraph(final String graphName, final String storeName, final String hostName, final int hostPort) {
       graphKeyString = graphName;
       try {
           store = KVStoreFactory.getStore
               (new KVStoreConfig(storeName, hostName + ":" + hostPort));
               
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
        FEATURES.ignoresSuppliedIds = true;
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
        KVVertex vertex = new KVVertex(this);
        String vertexId = vertex.getId().toString()+"/ID";
        
        /* build the KVstore keys */
        Key vertexKey = keyFromString(vertexId);
                
        /* set the key and value */
        Value vertexValue = Value.createValue(toByteArray(vertex.getId()));
        
        /* write */
        try {
            store.put(vertexKey, vertexValue);
        }
        catch (Exception e)
        {
            vertex = null;
        }
        return vertex;
    }
    
    public Vertex getVertex(final Object id) {
        if (null == id)
            throw ExceptionFactory.edgeIdCanNotBeNull();
        
        try {
            final String theId = id.toString();
            return new KVVertex(this, theId);
        } 
        
        catch (Exception e) {
                return null;
        }
    }
    
    
    public Vertex removeVertex(final Object id) {
        KVVertex vertexToRemove = new KVVertex(this, id);
        /* TODO: Remove all connected edges */
        return vertexToRemove;
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
        KVEdge edge = new KVEdge(this);
        String edgeId = edge.getId().toString()+"/ID";
        String edgeInV = edge.getId().toString()+"/IN";
        String edgeOutV = edge.getId().toString()+"/OUT";
        String edgeLabel = edge.getId().toString()+"/LABEL";
        /* build the KVstore keys */
        Key edgeKey = keyFromString(edgeId);
        Key edgeInKey = keyFromString(edgeInV);
        Key edgeOutKey = keyFromString(edgeOutV);
        Key edgeLabelKey = keyFromString(edgeLabel);
        

        
        /* set the key and value */
        Value edgeValue = Value.createValue(toByteArray(edge.getId()));
        Value edgeInVal = Value.createValue(toByteArray(inVertex.getId()));
        Value edgeOutVal = Value.createValue(toByteArray(outVertex.getId()));
        Value edgeLabelVal = Value.createValue(toByteArray(label));
        
        /*for the In Vertex, add this edge ID to the set of IN Edges*/
        Key inVinEdgeKey = keyFromString(inVertex.getId()+"/IN");
        Set<String> inEdges = (Set<String>) getValue(this.getRawGraph(), inVinEdgeKey);
        inEdges.add(edge.getId().toString());
        putValue(this.getRawGraph(), inVinEdgeKey, inEdges);
        
        /* for the Out Vertex, add this edge ID to the set of OUT edges*/
        Key outVoutEdgeKey = keyFromString(outVertex.getId()+"/OUT");
        Set<String> outEdges = (Set<String>) getValue(this.getRawGraph(), outVoutEdgeKey);
        outEdges.add(edge.getId().toString());
        putValue(this.getRawGraph(), outVoutEdgeKey, outEdges);
        
        /* for both verteces, add the other to the set of ADJ verteces */
        Key inVK = keyFromString(inVertex.getId()+"/ADJ");
        Key outVK = keyFromString(outVertex.getId()+"/ADJ");
        Set<String> outAdj = (Set<String>) getValue(this.getRawGraph(), outVK);        
        Set<String> inAdj = (Set<String>) getValue(this.getRawGraph(), inVK);
        outAdj.add(inVertex.getId().toString());
        inAdj.add(outVertex.getId().toString());
        putValue(this.getRawGraph(), inVK, inAdj);
        putValue(this.getRawGraph(), outVK, outAdj);
        
        
        /* write */
        try {
            
            // Get the operation factory. Note that we do not show the 
            // creation of the kvstore handle here.

            OperationFactory of = store.getOperationFactory();

            // We need a List to hold the operations in the
            // sequence.
            List<Operation> opList = new ArrayList<Operation>();
            opList.add(of.createPut(edgeKey, edgeValue));
            opList.add(of.createPut(edgeInKey, edgeInVal));
            opList.add(of.createPut(edgeOutKey, edgeOutVal));
            opList.add(of.createPut(edgeLabelKey, edgeLabelVal));
            store.execute(opList);

        }
        catch (Exception e)
        {
            edge = null;
        }
        return edge;
        
    } 
}