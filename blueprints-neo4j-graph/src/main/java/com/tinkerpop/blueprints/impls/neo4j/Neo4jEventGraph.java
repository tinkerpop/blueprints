package com.tinkerpop.blueprints.impls.neo4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.event.TransactionEventHandler;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.TransactionalEventGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.transaction.PropertyEntry;
import com.tinkerpop.blueprints.transaction.TransactionData;
import com.tinkerpop.blueprints.transaction.TransactionEventListener;

public class Neo4jEventGraph extends Neo4jGraph implements TransactionalEventGraph {
	Neo4jEventGraph neo4jEventGraph = null;
	protected List<TransactionEventListener<?>> transactionEventListeners;

	public Neo4jEventGraph(GraphDatabaseService rawGraph) {
		super(rawGraph);
		neo4jEventGraph = this;
		transactionEventListeners = new ArrayList<TransactionEventListener<?>>();

		getRawGraph().registerTransactionEventHandler(new TransactionEventHandler<Void>() {
			@Override
			public Void beforeCommit(org.neo4j.graphdb.event.TransactionData data)
					throws Exception {

				final Iterable<Vertex> createdVertices =
						new Neo4jVertexIterable(data.createdNodes(),
								neo4jEventGraph,
								neo4jEventGraph.checkElementsInTransaction());
				final Iterable<Vertex> deletedVertices =
						new Neo4jVertexIterable(data.deletedNodes(),
								neo4jEventGraph,
								neo4jEventGraph.checkElementsInTransaction());

				final Iterable<Edge> createdEdges =
						new Neo4jVertexIterable(data.createdRelationships(),
								neo4jEventGraph,
								neo4jEventGraph.checkElementsInTransaction());
				final Iterable<Edge> deletedEdges =
						new Neo4jVertexIterable(data.deletedRelationships(),
								neo4jEventGraph,
								neo4jEventGraph.checkElementsInTransaction());

				final Iterable<PropertyEntry<Vertex>> assignedVertexProps =
						getVertexProperties(data.assignedNodeProperties());
				final Iterable<PropertyEntry<Vertex>> removedVertexProps =
						getVertexProperties(data.removedNodeProperties());

				final Iterable<PropertyEntry<Edge>> assignedEdgeProps =
						getEdgeProperties(data.assignedRelationshipProperties());
				final Iterable<PropertyEntry<Edge>> removedEdgeProps =
						getEdgeProperties(data.removedRelationshipProperties());

				TransactionData tdata = new TransactionData() {

					@Override
					public Iterable<PropertyEntry<Vertex>> removedVertexProperties() {
						return removedVertexProps;
					}

					@Override
					public Iterable<PropertyEntry<Edge>> removedEdgeProperties() {
						return removedEdgeProps;
					}

					@Override
					public boolean isDeleted(Edge Edge) {
						throw new RuntimeException("Unspported method");
					}

					@Override
					public Iterable<Vertex> deletedVertices() {
						return deletedVertices;
					}

					@Override
					public Iterable<Edge> deletedEdges() {
						return deletedEdges;
					}

					@Override
					public Iterable<Vertex> createdVertices() {
						return createdVertices;
					}

					@Override
					public Iterable<Edge> createdEdges() {
						return createdEdges;
					}

					@Override
					public Iterable<PropertyEntry<Vertex>> assignedVertexProperties() {
						return assignedVertexProps;
					}

					@Override
					public Iterable<PropertyEntry<Edge>> assignedEdgeProperties() {
						return assignedEdgeProps;
					}
				};
				
				for ( TransactionEventListener<?> l : neo4jEventGraph.transactionEventListeners) {
					l.onBeforeCommit(tdata);
				}

				return null;
			}

			@Override
			public void afterCommit(org.neo4j.graphdb.event.TransactionData data, Void state) {

			}

			@Override
			public void afterRollback(org.neo4j.graphdb.event.TransactionData data, Void state) {

			}

			private Iterable<PropertyEntry<Vertex>> getVertexProperties(Iterable<org.neo4j.graphdb.event.PropertyEntry<Node>> props) {
				List<PropertyEntry<Vertex>> vertexProps = new ArrayList<PropertyEntry<Vertex>>();

				for (final org.neo4j.graphdb.event.PropertyEntry<Node> prop : props) {
					PropertyEntry<Vertex> p = new PropertyEntry<Vertex>() {

						@Override
						public Vertex entity() {
							return new Neo4jVertex(prop.entity(), neo4jEventGraph);
						}

						@Override
						public String key() {
							return prop.key();
						}

						@Override
						public Object previouslyCommitedValue() {
							return prop.previouslyCommitedValue();
						}

						@Override
						public Object value() {
							return prop.value();
						}
					};

					vertexProps.add(p);
				}

				return vertexProps;
			}

			private Iterable<PropertyEntry<Edge>> getEdgeProperties(Iterable<org.neo4j.graphdb.event.PropertyEntry<Relationship>> props) {
				List<PropertyEntry<Edge>> edgeProps = new ArrayList<PropertyEntry<Edge>>();

				for (final org.neo4j.graphdb.event.PropertyEntry<Relationship> prop : props) {
					PropertyEntry<Edge> p = new PropertyEntry<Edge>() {

						@Override
						public Edge entity() {
							return new Neo4jEdge(prop.entity(), neo4jEventGraph);
						}

						@Override
						public String key() {
							return prop.key();
						}

						@Override
						public Object previouslyCommitedValue() {
							return prop.previouslyCommitedValue();
						}

						@Override
						public Object value() {
							return prop.value();
						}
					};

					edgeProps.add(p);
				}

				return edgeProps;
			}
		});
	}

	public <T> TransactionEventListener<T> registerTransactionEventListener(
			TransactionEventListener<T> listener)
	{
		this.transactionEventListeners.add(listener);
		return listener;
	}

	public <T> TransactionEventListener<T> unregisterTransactionEventListener(
			TransactionEventListener<T> listener)
	{
		return unregisterListener(this.transactionEventListeners, listener);
	}
	
	public <T> T unregisterListener(Collection<?> listeners, T listener)
	{
		if (!listeners.remove(listener))
			throw new IllegalStateException(String.format("Cannot unregister listener %s [%s] as it is not registered", listener));

		return listener;
	}

	public boolean hasListeners()
	{
		return !transactionEventListeners.isEmpty();
	}
}