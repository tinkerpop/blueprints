package com.tinkerpop.blueprints.util.wrappers.batch.loader;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.WrapperGraph;
import com.tinkerpop.blueprints.util.wrappers.batch.VertexIDType;
import com.tinkerpop.blueprints.util.wrappers.batch.cache.VertexCache;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public class ParallelGraphLoader<T extends Graph> extends Debugger implements WrapperGraph<T> {

    private final T graph;
    private final VertexIDType idType;
    
    private int numThreads;
    private int transactionSize;
    
    private String externalVertexIdKey = null;
    private String externalEdgeIdKey = null;

    public ParallelGraphLoader(final T graph) {
        this(graph,VertexIDType.OBJECT);
    }

    public ParallelGraphLoader(final T graph, final VertexIDType idType) {
        if (graph==null) throw new IllegalArgumentException("Need to provide graph");
        if (idType==null) throw new IllegalArgumentException("Need to provide vertex id type");
        this.graph=graph;
        this.idType=idType;

        this.numThreads = Math.max(1,Runtime.getRuntime().availableProcessors()/2);
        this.transactionSize = 1000;
    }
    
    public void configure(final int numThreads, final int transactionSize) {
        this.numThreads=numThreads;
        this.transactionSize=transactionSize;
    }
    
    public void setVertexIdKey(String key) {
        if (key==null) throw new IllegalArgumentException("Key cannot be null");
        this.externalVertexIdKey=key;
    }
    
    public String getVertexIdKey() {
        return externalVertexIdKey;
    }

    public void setEdgeIdKey(String key) {
        if (key==null) throw new IllegalArgumentException("Key cannot be null");
        this.externalEdgeIdKey=key;
    }

    public String getEdgeIdKey() {
        return externalEdgeIdKey;
    }

    public void load(Iterable<Triple> triples) {
        final VertexCache cache = idType.getVertexCache();

        List<Object> vertexIds = new ArrayList<Object>(transactionSize+2);
        Set<Object> currentVertexIds = new HashSet<Object>();

        Thread[] threads = new Thread[numThreads];
        int nextThread = 0;

        Iterator<Triple> iter = triples.iterator();
        while (iter.hasNext()) {
            Object[] vertices = null;
            Triple triple = iter.next();
            if (triple.isProperty()) vertices = new Object[]{triple.getOutVertexId()};
            else vertices = new Object[]{triple.getOutVertexId(),((EdgeTriple)triple).getInVertexId()};
            for (Object vertex : vertices) {
                if (!currentVertexIds.contains(vertex) && !cache.contains(vertex)) {
                    currentVertexIds.add(vertex);
                    vertexIds.add(vertex);
                }
            }
            if (vertexIds.size()>=transactionSize || !iter.hasNext()) {
                debug("Filling next thread with: ",vertexIds.size());
                threads[nextThread]=new Thread(new AddVertices(cache,vertexIds));
                vertexIds = new ArrayList<Object>(transactionSize+2);
                nextThread++;
                if (nextThread==threads.length || !iter.hasNext()) {
                    //Start all threads and wait for them
                    currentVertexIds.clear();
                    debug("Kicking off threads #=",nextThread);
                    try {
                        for (int i=0;i<nextThread;i++) {
                            threads[i].start();
                            Thread.sleep(10);
                        }
                        for (int i=0;i<nextThread;i++)
                            threads[i].join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException("Got interrupted while starting vertex threads",e);
                    }
                    nextThread=0;
                    debug("Threads have completed");
                }
            }
        }

        vertexIds = null;
        currentVertexIds = null;

        debug("Loading vertices completed");

        threads = new Thread[numThreads];
        int waitTime = Math.max(10,transactionSize/100);
        iter = triples.iterator();
        List<Triple> nextTriples = new ArrayList<Triple>(transactionSize);
        while (iter.hasNext()) {
            nextTriples.add(iter.next());
            if (nextTriples.size()>=transactionSize || !iter.hasNext()) {
                //Kick off another thread
                debug("Kicking off another thread: " + nextTriples.size());
                boolean started = false;
                while (!started) {
                    for (int i=0;i<threads.length;i++) {
                        if (threads[i]==null || !threads[i].isAlive()) {
                            threads[i]=new Thread(new AddTriples(cache,nextTriples));
                            threads[i].start();
                            debug("New thread started ",i);
                            nextTriples = new ArrayList<Triple>(transactionSize);
                            started=true;
                            break;
                        }
                    }
                    //Wait a little for some thread to finish
                    if (!started) {
                        try {
                            debug("Waiting for thread to become available (ms):",waitTime);
                            Thread.sleep(waitTime);
                        } catch (InterruptedException e) {
                            throw new RuntimeException("Got interrupted while waiting for edge threads",e);
                        }
                    }
                }
            }
        }
        nextTriples=null;
        debug("Joining all threads at end");
        try {
            for (int i=0;i<threads.length;i++)
                if (threads[i]!=null && threads[i].isAlive()) threads[i].join();
        } catch (InterruptedException e) {
            throw new RuntimeException("Got interrupted while waiting for edge threads",e);
        }
    }
    
    private class AddVertices implements Runnable {

        private final VertexCache cache;
        private final List<Object> vertexIds;

        AddVertices(final VertexCache cache, final List<Object> vertexIds) {
            this.cache=cache;
            this.vertexIds=vertexIds;
        }
        
        @Override
        public void run() {
            List<Vertex> vertices = new ArrayList<Vertex>();
            for (Object vid : vertexIds) {
                Vertex v = null;
                if (!graph.getFeatures().ignoresSuppliedIds) {
                    v = graph.addVertex(vid);
                } else {
                    v = graph.addVertex(null);
                    if (externalVertexIdKey!=null) {
                        v.setProperty(externalVertexIdKey,vid);
                    }
                }
                vertices.add(v);
            }
            assert vertices.size()==vertexIds.size();
            if (graph instanceof TransactionalGraph) {
                ((TransactionalGraph)graph).stopTransaction(TransactionalGraph.Conclusion.SUCCESS);
            }
            synchronized (cache) {
                for (int i=0;i<vertexIds.size();i++) {
                    cache.setId(vertices.get(i).getId(),vertexIds.get(i));
                }
            }
        }
    }

    private class AddTriples implements Runnable {

        private final VertexCache cache;
        private final List<Triple> triples;

        AddTriples(final VertexCache cache, final List<Triple> triples) {
            this.cache=cache;
            this.triples=triples;
        }

        @Override
        public void run() {
            for (Triple triple : triples) {                
                Vertex out = getVertex(triple.getOutVertexId());
                if (out==null) {
                    System.out.println("Skipping triple due to: " + triple.getOutVertexId());
                    continue;
                }
                String type = triple.getType();
                
                if (triple.isEdge()) {
                    Vertex in = getVertex(((EdgeTriple)triple).getInVertexId());
                    if (in==null) {
                        System.out.println("Skipping triple due to: " + ((EdgeTriple)triple).getInVertexId());
                        continue;
                    }
                    Object edgeid = null;
                    Map<String,Object> properties = triple.getProperties();
                    if (!graph.getFeatures().ignoresSuppliedIds && properties.containsKey("id"))
                        edgeid = properties.get("id");
                    Edge edge = graph.addEdge(edgeid,out,in,type);
                    for (Map.Entry<String,Object> entry : properties.entrySet()) {
                        if (entry.getKey().equals("id")) {
                            if (graph.getFeatures().ignoresSuppliedIds && externalEdgeIdKey!=null) {
                                edge.setProperty(externalEdgeIdKey,entry.getValue());
                            }
                        } else edge.setProperty(entry.getKey(),entry.getValue());
                    }
                } else {
                    if (!triple.getProperties().isEmpty()) 
                        throw new UnsupportedOperationException("Properties on property are not supported.");
                    out.setProperty(type,((PropertyTriple)triple).getProperty());
                }
            }
            if (graph instanceof TransactionalGraph) {
                ((TransactionalGraph)graph).stopTransaction(TransactionalGraph.Conclusion.SUCCESS);
            }
            
        }
        

        
        private final Vertex getVertex(Object externalId) {
            Object internalId = cache.getEntry(externalId);
            if (internalId==null) return null;
            return graph.getVertex(internalId);
        }
    }

    @Override
    public T getBaseGraph() {
        return graph;
    }
}
