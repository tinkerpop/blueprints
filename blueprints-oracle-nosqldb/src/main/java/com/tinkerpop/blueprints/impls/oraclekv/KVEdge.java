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
    
    public KVEdge(final KVGraph graph) {
        super(graph);
        this.store = graph.getRawGraph();
        this.id = graph.getGraphKey() + "/Edge/"+UUID.randomUUID().toString();
    }

    public KVEdge(final KVGraph graph, final Object id) {
        super(graph);
        this.store = graph.getRawGraph();
        this.id = graph.getGraphKey() + "/Edge/"+id.toString();
    }
    
    
    @Override
    public Vertex getVertex(Direction direction) throws IllegalArgumentException {
        KVVertex result = null;
        if (direction.equals(Direction.OUT))
        {
            String vertexId = (String)getValue(this.store, keyFromString(this.id+"/OUT"));
            result = new KVVertex(this.graph, vertexId);
        }
        else if (direction.equals(Direction.IN))
        {
            String vertexId = (String)getValue(this.store, keyFromString(this.id+"/IN"));
            result = new KVVertex(this.graph, vertexId);
        }
        else
          throw ExceptionFactory.bothIsNotSupported();
          
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