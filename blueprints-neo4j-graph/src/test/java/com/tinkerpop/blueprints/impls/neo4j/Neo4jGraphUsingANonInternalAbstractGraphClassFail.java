package com.tinkerpop.blueprints.impls.neo4j;

import static org.junit.Assert.*;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.event.KernelEventHandler;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.kernel.EmbeddedGraphDatabase;

public class Neo4jGraphUsingANonInternalAbstractGraphClassFail {
	private class LazyLoadedGraphDatabase implements GraphDatabaseService {
		private GraphDatabaseService lazy;
		
		public GraphDatabaseService getLazy() {
			if(lazy==null) {
				// load that lazy graph in a unique folder
				lazy = new EmbeddedGraphDatabase(getClass().getName()+"/"+System.currentTimeMillis());
			}
			return lazy;
		}

		/**
		 * @return
		 * @see org.neo4j.graphdb.GraphDatabaseService#createNode()
		 * @category delegate
		 */
		public Node createNode() {
			return getLazy().createNode();
		}

		/**
		 * @param id
		 * @return
		 * @see org.neo4j.graphdb.GraphDatabaseService#getNodeById(long)
		 * @category delegate
		 */
		public Node getNodeById(long id) {
			return getLazy().getNodeById(id);
		}

		/**
		 * @param id
		 * @return
		 * @see org.neo4j.graphdb.GraphDatabaseService#getRelationshipById(long)
		 * @category delegate
		 */
		public Relationship getRelationshipById(long id) {
			return getLazy().getRelationshipById(id);
		}

		/**
		 * @return
		 * @deprecated
		 * @see org.neo4j.graphdb.GraphDatabaseService#getReferenceNode()
		 * @category delegate
		 */
		public Node getReferenceNode() {
			return getLazy().getReferenceNode();
		}

		/**
		 * @return
		 * @deprecated
		 * @see org.neo4j.graphdb.GraphDatabaseService#getAllNodes()
		 * @category delegate
		 */
		public Iterable<Node> getAllNodes() {
			return getLazy().getAllNodes();
		}

		/**
		 * @return
		 * @deprecated
		 * @see org.neo4j.graphdb.GraphDatabaseService#getRelationshipTypes()
		 * @category delegate
		 */
		public Iterable<RelationshipType> getRelationshipTypes() {
			return getLazy().getRelationshipTypes();
		}

		/**
		 * 
		 * @see org.neo4j.graphdb.GraphDatabaseService#shutdown()
		 * @category delegate
		 */
		public void shutdown() {
			getLazy().shutdown();
		}

		/**
		 * @return
		 * @see org.neo4j.graphdb.GraphDatabaseService#beginTx()
		 * @category delegate
		 */
		public Transaction beginTx() {
			return getLazy().beginTx();
		}

		/**
		 * @param handler
		 * @return
		 * @see org.neo4j.graphdb.GraphDatabaseService#registerTransactionEventHandler(org.neo4j.graphdb.event.TransactionEventHandler)
		 * @category delegate
		 */
		public <T> TransactionEventHandler<T> registerTransactionEventHandler(TransactionEventHandler<T> handler) {
			return getLazy().registerTransactionEventHandler(handler);
		}

		/**
		 * @param handler
		 * @return
		 * @see org.neo4j.graphdb.GraphDatabaseService#unregisterTransactionEventHandler(org.neo4j.graphdb.event.TransactionEventHandler)
		 * @category delegate
		 */
		public <T> TransactionEventHandler<T> unregisterTransactionEventHandler(TransactionEventHandler<T> handler) {
			return getLazy().unregisterTransactionEventHandler(handler);
		}

		/**
		 * @param handler
		 * @return
		 * @see org.neo4j.graphdb.GraphDatabaseService#registerKernelEventHandler(org.neo4j.graphdb.event.KernelEventHandler)
		 * @category delegate
		 */
		public KernelEventHandler registerKernelEventHandler(KernelEventHandler handler) {
			return getLazy().registerKernelEventHandler(handler);
		}

		/**
		 * @param handler
		 * @return
		 * @see org.neo4j.graphdb.GraphDatabaseService#unregisterKernelEventHandler(org.neo4j.graphdb.event.KernelEventHandler)
		 * @category delegate
		 */
		public KernelEventHandler unregisterKernelEventHandler(KernelEventHandler handler) {
			return getLazy().unregisterKernelEventHandler(handler);
		}

		/**
		 * @return
		 * @see org.neo4j.graphdb.GraphDatabaseService#index()
		 * @category delegate
		 */
		public IndexManager index() {
			return getLazy().index();
		}
	}
	
	/**
	 * in this test, our class is a graph database service, but not an InternalAbstractGraphDatabase instance.As a consequence, {@link Neo4jGraph#getInternalIndexKeys}
	 * won't load any index, with an additional {@link ClassCastException}
	 */
	@Test
	public void loadingANeo4jGraphFromAnyGraphDatabaseClassShouldWork() throws ClassCastException {
		Neo4jGraph tested = new Neo4jGraph(new LazyLoadedGraphDatabase());
		assertThat(tested, IsNull.notNullValue());
		assertThat(tested.getIndices().iterator().hasNext(), Is.is(true));
	}
}
