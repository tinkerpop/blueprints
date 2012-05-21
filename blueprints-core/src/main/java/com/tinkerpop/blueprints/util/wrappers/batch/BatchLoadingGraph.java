package com.tinkerpop.blueprints.util.wrappers.batch;

import com.tinkerpop.blueprints.*;
import com.tinkerpop.blueprints.util.wrappers.WrapperGraph;
import com.tinkerpop.blueprints.util.wrappers.batch.vertexcache.*;

import java.util.Set;

/**
 * BachLoadingGraph is a wrapper that enables batch loading of a large number of edges and vertices.
 * 
 * BatchLoadingGraph is ONLY meant for loading data and does not support any retrieval or removal operations.
 * That is, BatchLoadingGraph only supports the following methods:
 * - {@link #addVertex(Object)} for adding vertices
 * - {@link #addEdge(Object, com.tinkerpop.blueprints.Vertex, com.tinkerpop.blueprints.Vertex, String)} for adding edges
 * - {@link #getVertex(Object)} to be used when adding edges
 * 
 * BatchLoadingGraph tries to determine the optimal transaction length based on the available heap memory.
 * 
 * 
 * @author Matthias Broecheler (http://www.matthiasb.com)
 */

public class BatchLoadingGraph<T extends TransactionalGraph> implements TransactionalGraph, WrapperGraph<T> {

    public static final double MAX_PERCENTAGE_MEMORY = 0.5;
    public static final int DEFAULT_BYTES_PER_ELEMENT = 1000;

    private static final long MIN_BUFFERSIZE = 1000;
    private static final long MAX_BUFFERSIZE = 10000000;
    private static final double CONSERVATIVE_BPE_FACTOR = 1.3;
    private static final int GC_RUN_SLEEPTIME = 100;
    

    private final T graph;
    private final boolean ignoreSuppliedIDs;
    private final String vertexIDKey;
    private final String edgeIDKey;
    
    private final VertexCache cache;
    
    private double maxPercentageMemory = MAX_PERCENTAGE_MEMORY;
    private boolean stopTransactionFlag = false;

    private int bytesPerElement = DEFAULT_BYTES_PER_ELEMENT;
    private long remainingBufferSize;
    private long lastAvailableMemory = -1;
    private long lastBufferSize = -1;
    
    private BatchLoadingEdge currentEdge =null;
    private Edge currentEdgeCached =null;
    
    public BatchLoadingGraph(T graph, IDType type, String vertexIDKey, String edgeIDKey) {
        if (graph==null) throw new IllegalArgumentException("Graph may not be null");
        if (type==null) throw new IllegalArgumentException("Type may not be null");
        this.graph = graph;
        this.ignoreSuppliedIDs = graph.getFeatures().ignoresSuppliedIds;
        if (!ignoreSuppliedIDs && (vertexIDKey!=null || edgeIDKey!=null)) {
            throw new IllegalArgumentException("Wrapped graph supports supplied IDs");
        }
        this.vertexIDKey=vertexIDKey;
        this.edgeIDKey=edgeIDKey;
        
        cache = type.getVertexCache(this.graph);

        graph.startTransaction();
        remainingBufferSize = calculateRemainingBufferSize();
    }

    public BatchLoadingGraph(T graph, IDType type) {
        this(graph,type,null,null);
    }
    
    public BatchLoadingGraph(T graph) {
        this(graph,IDType.OBJECT);
    }

    public void setBytesPerElement(int bytesPerElement) {
        if (bytesPerElement<=0) throw new IllegalArgumentException("Expected positive number.");
        this.bytesPerElement=bytesPerElement;
    }

    public void setMaxPercentageMemory(double percentage) {
        if (percentage<=0.0 || percentage>1.0) throw new IllegalArgumentException("Expected positive percentage in (0,1]");
        this.maxPercentageMemory=percentage;
    }

    private long calculateRemainingBufferSize() {
        runGC();
        Runtime runtime = Runtime.getRuntime();
        long availableMemory = runtime.maxMemory()-runtime.totalMemory()+runtime.freeMemory();
        //Update
        if (lastAvailableMemory>0) {
            bytesPerElement = (int)Math.ceil((lastAvailableMemory - availableMemory) /
                    lastBufferSize * CONSERVATIVE_BPE_FACTOR);
            assert bytesPerElement>0;
        }
        long bufferSize = (long)(availableMemory-(1.0-maxPercentageMemory)*runtime.maxMemory())/bytesPerElement;
        if (bufferSize<MIN_BUFFERSIZE) bufferSize=0;
        if (bufferSize>MAX_BUFFERSIZE) bufferSize=MAX_BUFFERSIZE;
        lastAvailableMemory = availableMemory;
        lastBufferSize = bufferSize;

        return bufferSize;
    }

    private static void runGC() {
        for (int i=0;i<2;i++) {
            try {
                System.gc();
                Thread.sleep(GC_RUN_SLEEPTIME);
                System.runFinalization();
                Thread.sleep(GC_RUN_SLEEPTIME);
            }
            catch (InterruptedException ex){
                ex.printStackTrace();
            }
        }
    }

    private void nextElement() {
        currentEdge =null;
        currentEdgeCached =null;
        if (remainingBufferSize<=0) remainingBufferSize = calculateRemainingBufferSize();
        if (remainingBufferSize<=0 || stopTransactionFlag) {
            graph.stopTransaction(Conclusion.SUCCESS);
            cache.newTransaction();
            lastAvailableMemory=-1;
            lastBufferSize=-1;
            graph.startTransaction();
            remainingBufferSize = calculateRemainingBufferSize();
        }
        if (remainingBufferSize<=0) {
            throw new OutOfMemoryError("Could not allocate non-empty write buffer since max memory " +
                    "fraction has been reached ["+maxPercentageMemory+"]. Try increasing heap space (using -Xmx) " +
                    "or allow a larger fraction of memory to be allocated.");
        }
        remainingBufferSize--;
    }

    @Override
    public void startTransaction() throws IllegalStateException {
        //Do nothing, transaction is already started
    }

    @Override
    public void stopTransaction(Conclusion conclusion) {
        if (conclusion!=Conclusion.SUCCESS) throw new IllegalArgumentException("Cannot aboart batch loading.");
        stopTransactionFlag=true;
    }

    @Override
    public void shutdown() {
        graph.stopTransaction(Conclusion.SUCCESS);
        graph.shutdown();
        currentEdge = null;
        currentEdgeCached = null;
    }

    @Override
    public T getBaseGraph() {
        return graph;
    }
    
    @Override
    public Features getFeatures() {
        Features features = graph.getFeatures().copyFeatures();
        features.ignoresSuppliedIds=false;
        features.isWrapper=true;
        features.supportsEdgeIteration=false;
        features.supportsThreadedTransactions=false;
        features.supportsVertexIteration=false;
        return features;
    }
    
    private Vertex getCachedVertex(Object externalID) {
        Vertex v = cache.getVertex(externalID);
        if (v==null) throw new IllegalArgumentException("Vertex for given ID cannot be found: " + externalID);
        return v;
    }

    @Override
    public Vertex getVertex(Object id) {
        return getCachedVertex(id);
    }

    @Override
    public Vertex addVertex(Object id) {
        if (id==null) throw new IllegalArgumentException("A vertex must be assigned a unique id.");
        nextElement();
        
        Vertex v = null;
        if (ignoreSuppliedIDs) {
            v = graph.addVertex(null);
            if (vertexIDKey!=null) {
                v.setProperty(vertexIDKey,id);
            }
        } else {
            v = graph.addVertex(id);
        }
        cache.add(v,id);
        return new BatchLoadingVertex(id);
    }

    @Override
    public Edge addEdge(Object id, Vertex outVertex, Vertex inVertex, String label) {
        if (outVertex.getClass() != BatchLoadingVertex.class || inVertex.getClass() != BatchLoadingVertex.class)
            throw new IllegalArgumentException("Unexpected vertex type.");
        nextElement();
        Vertex ov = getCachedVertex(outVertex.getId());
        Vertex iv = getCachedVertex(inVertex.getId());
        if (ignoreSuppliedIDs) {
            currentEdgeCached = graph.addEdge(null,ov,iv,label);
            if (edgeIDKey!=null && id!=null) {
                currentEdgeCached.setProperty(edgeIDKey,id);
            }
        } else {
            currentEdgeCached = graph.addEdge(id,ov,iv,label);
        }
        currentEdge = new BatchLoadingEdge();
        return currentEdge;
    }

    // ################### Unsupported Graph Methods ####################

    @Override
    public Edge getEdge(Object id) {
        throw retrievalNotSupported();
    }

    @Override
    public void removeVertex(Vertex vertex) {
        throw removalNotSupported();
    }

    @Override
    public Iterable<Vertex> getVertices() {
        throw retrievalNotSupported();
    }

    @Override
    public Iterable<Vertex> getVertices(String key, Object value) {
        throw retrievalNotSupported();
    }
    
    @Override
    public void removeEdge(Edge edge) {
        throw removalNotSupported();
    }

    @Override
    public Iterable<Edge> getEdges() {
        throw retrievalNotSupported();
    }

    @Override
    public Iterable<Edge> getEdges(String key, Object value) {
        throw retrievalNotSupported();
    }
    
    private class BatchLoadingVertex implements Vertex {

        private final Object externalID;

        BatchLoadingVertex(Object id) {
            if (id==null) throw new IllegalArgumentException("External id may not be null");
            externalID = id;
        }
        
        @Override
        public Iterable<Edge> getEdges(Direction direction, String... labels) {
            throw retrievalNotSupported();
        }

        @Override
        public Iterable<Vertex> getVertices(Direction direction, String... labels) {
            throw retrievalNotSupported();
        }

        @Override
        public Query query() {
            throw retrievalNotSupported();
        }

        @Override
        public void setProperty(String key, Object value) {
            getCachedVertex(externalID).setProperty(key,value);
        }

        @Override
        public Object getId() {
            return externalID;
        }

        @Override
        public Object getProperty(String key) {
            return getCachedVertex(externalID).getProperty(key);
        }

        @Override
        public Set<String> getPropertyKeys() {
            return getCachedVertex(externalID).getPropertyKeys();
        }

        @Override
        public Object removeProperty(String key) {
            return getCachedVertex(externalID).removeProperty(key);
        }
    }
    
    private class BatchLoadingEdge implements Edge {

        @Override
        public Vertex getVertex(Direction direction) throws IllegalArgumentException {
            return getWrappedEdge().getVertex(direction);
        }

        @Override
        public String getLabel() {
            return getWrappedEdge().getLabel();
        }

        @Override
        public void setProperty(String key, Object value) {
            getWrappedEdge().setProperty(key,value);
        }

        @Override
        public Object getId() {
            return getWrappedEdge().getId();
        }

        @Override
        public Object getProperty(String key) {
            return getWrappedEdge().getProperty(key);
        }

        @Override
        public Set<String> getPropertyKeys() {
            return getWrappedEdge().getPropertyKeys();
        }

        @Override
        public Object removeProperty(String key) {
            return getWrappedEdge().removeProperty(key);
        }

        private Edge getWrappedEdge() {
            if (this!=currentEdge) {
                throw new UnsupportedOperationException("This edge is no longer in scope.");
            }
            return currentEdgeCached;
        }
    }
    
    
    private static final UnsupportedOperationException retrievalNotSupported() {
        return new UnsupportedOperationException("Retrieval operations are not supported during batch loading.");
    }

    private static final UnsupportedOperationException removalNotSupported() {
        return new UnsupportedOperationException("Removal operations are not supported during batch loading.");
    }

    public static enum IDType {

        OBJECT, NUMBER, STRING, URL;

        private VertexCache getVertexCache(Graph g) {
            switch(this) {
                case OBJECT: return new ObjectIDVertexCache(g);
                case NUMBER: return new LongIDVertexCache(g);
                case STRING: return new StringIDVertexCache(g);
                case URL: return new StringIDVertexCache(g,new URLCompression());
                default: throw new IllegalArgumentException("Unrecognized ID type: " + this);
            }
        }

    }
}
