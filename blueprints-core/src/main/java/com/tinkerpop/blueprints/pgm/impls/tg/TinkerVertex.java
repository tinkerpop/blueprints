package com.tinkerpop.blueprints.pgm.impls.tg;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.impls.tg.util.TinkerEdgeSequence;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class TinkerVertex extends TinkerElement implements Vertex, Serializable {

    protected Set<Edge> outEdges = new HashSet<Edge>();
    protected Set<Edge> inEdges = new HashSet<Edge>();

    protected TinkerVertex(final String id, final TinkerGraph graph) {
        super(id, graph);
    }

    public TinkerVertex getRawVertex() {
        return this;
    }

    public Iterable<Edge> getOutEdges() {
        return this.outEdges;
    }

    public Iterable<Edge> getInEdges() {
        return this.inEdges;
    }

    public Iterable<Edge> getOutEdges(final String label) {
        return new TinkerEdgeSequence(this.outEdges.iterator(), label);
    }

    public Iterable<Edge> getInEdges(final String label) {
        return new TinkerEdgeSequence(this.inEdges.iterator(), label);
    }
    
		public String toJSON() {
    		StringBuilder vertex = new StringBuilder(this.getId().toString() + ": {");

    		vertex.append("\"_id\": " + this.getId().toString() + ",");
    		vertex.append("\"_type: \"vertex\",");
    		
    		for(String key : this.getPropertyKeys()) {
    				vertex.append("\"" + key + "\": \"" + this.getProperty(key).toString() + "\",");
    		}
    		
    		vertex.deleteCharAt(vertex.length() - 1);
    		vertex.append("}");
    		
    		return vertex.toString();
    }

    public String toString() {
        return StringFactory.vertexString(this);
    }

    public boolean equals(final Object object) {
        return object instanceof TinkerVertex && ((TinkerVertex) object).getId().equals(this.getId());
    }
}
