package com.tinkerpop.blueprints.transaction;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

public interface TransactionData
{
	Iterable<Vertex> createdVertices();

	Iterable<Vertex> deletedVertices();

	Iterable<PropertyEntry<Vertex>> assignedVertexProperties();

	Iterable<PropertyEntry<Vertex>> removedVertexProperties();

	Iterable<Edge> createdEdges();

	Iterable<Edge> deletedEdges();

	boolean isDeleted(Edge Edge);

	Iterable<PropertyEntry<Edge>> assignedEdgeProperties();

	Iterable<PropertyEntry<Edge>> removedEdgeProperties();
}
