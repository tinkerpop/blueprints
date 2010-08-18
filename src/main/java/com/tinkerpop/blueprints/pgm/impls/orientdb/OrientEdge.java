package com.tinkerpop.blueprints.pgm.impls.orientdb;

import java.util.Set;

import com.orientechnologies.orient.core.db.graph.OGraphEdge;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;

public class OrientEdge extends OrientElement implements Edge {
	public OrientEdge(final OGraphEdge iRaw) {
		super(iRaw);
	}

	public Vertex getOutVertex() {
		return new OrientVertex(getRaw().getOut());
	}

	public Vertex getInVertex() {
		return new OrientVertex(getRaw().getIn());
	}

	public OGraphEdge getRaw() {
		return (OGraphEdge) raw;
	}

	public void delete() {
		((OGraphEdge) raw).delete();
	}

	public void save() {
		((OGraphEdge) raw).save();
	}

	@Override
	public Set<String> getPropertyKeys() {
		final Set<String> set = super.getPropertyKeys();
		set.remove(OGraphEdge.IN);
		set.remove(OGraphEdge.OUT);
		return set;
	}
}
