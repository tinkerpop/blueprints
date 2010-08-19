package com.tinkerpop.blueprints.pgm.impls.orientdb;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.orientechnologies.orient.core.db.graph.ODatabaseGraphTx;
import com.orientechnologies.orient.core.db.graph.OGraphEdge;
import com.orientechnologies.orient.core.db.graph.OGraphVertex;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;

public class OrientVertex extends OrientElement implements Vertex {

	public OrientVertex(final ODatabaseGraphTx iDatabase) {
		super(new OGraphVertex(iDatabase));
		raw.save();
	}

	public OrientVertex(final OGraphVertex iRaw) {
		super(iRaw);
	}

	public Iterable<Edge> getOutEdges() {
		final List<Edge> result = new ArrayList<Edge>();
		final List<OGraphEdge> coll = ((OGraphVertex) raw).getOutEdges();

		for (OGraphEdge e : coll) {
			result.add(new OrientEdge(e));
		}

		return result;
	}

	public Iterable<Edge> getInEdges() {
		final List<Edge> result = new ArrayList<Edge>();
		final List<OGraphEdge> coll = ((OGraphVertex) raw).getInEdges();

		for (OGraphEdge e : coll) {
			result.add(new OrientEdge(e));
		}

		return result;
	}

	@Override
	public Set<String> getPropertyKeys() {
		final Set<String> set = super.getPropertyKeys();
		set.remove(OGraphVertex.FIELD_IN_EDGES);
		set.remove(OGraphVertex.FIELD_OUT_EDGES);
		return set;
	}

	public OGraphVertex getRaw() {
		return (OGraphVertex) raw;
	}
}
