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
import com.tinkerpop.blueprints.util.ExceptionFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * @author Dan McClary
 */
public class KVEdge extends KVElement implements Edge {
    private final KVStore store;
    private final String idString;

    public KVEdge(final KVGraph graph) {
        super(graph);
        this.store = graph.getRawGraph();
        this.idString = UUID.randomUUID().toString();
        this.id = graph.getGraphKey() + "/Edge/"+this.idString;
    }

    public KVEdge(final KVGraph graph, final Object id) {
        super(graph);
        this.store = graph.getRawGraph();
        this.idString = id.toString();
        this.id = graph.getGraphKey() + "/Edge/"+this.idString;
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
    
    @Override
    public Vertex getVertex(Direction direction) throws IllegalArgumentException {
        Vertex result = null;
        if (direction.equals(Direction.OUT))
        {
            String vertexId = (String)this.getProperty("OUT");
            result = this.graph.getVertex(vertexId);
        }
        else if (direction.equals(Direction.IN))
        {
            String vertexId = (String)this.getProperty("IN");
            result = this.graph.getVertex(vertexId);
        }
        else
        {
            throw ExceptionFactory.bothIsNotSupported();
        }
          
        return result;
    }

    @Override
    public String getLabel() {
        return (String)getValue(this.store, keyFromString(this.id+"/LABEL"));
    }

    public String toString() {
      return StringFactory.edgeString(this);
    }

    public boolean equals(final Object object) {
      return object instanceof KVEdge && ((KVEdge)object).getId().equals(this.getId());
    }
}