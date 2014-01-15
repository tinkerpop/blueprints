package com.tinkerpop.blueprints.impls.neo4j2;

import com.tinkerpop.blueprints.Vertex;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.event.KernelEventHandler;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.graphdb.traversal.BidirectionalTraversalDescription;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.kernel.TransactionBuilder;
import org.neo4j.kernel.impl.nioneo.store.StoreId;

import static org.junit.Assert.assertThat;

public class Neo4j2GraphUsingANonInternalAbstractGraphClassFail {
    private class LazyLoadedGraphDatabase implements GraphDatabaseService {
        private GraphDatabaseService lazy;

        public GraphDatabaseService getLazy() {
            if (lazy == null) {
                // load that lazy graph in a unique folder
                lazy = new GraphDatabaseFactory().newEmbeddedDatabase(getClass().getName() + "/" + System.currentTimeMillis());
            }
            return lazy;
        }

        /**
         * @return
         * @category delegate
         * @see org.neo4j.graphdb.GraphDatabaseService#createNode()
         */
        public Node createNode() {
            return getLazy().createNode();
        }

        /**
         * @param id
         * @return
         * @category delegate
         * @see org.neo4j.graphdb.GraphDatabaseService#getNodeById(long)
         */
        public Node getNodeById(long id) {
            return getLazy().getNodeById(id);
        }

        /**
         * @param id
         * @return
         * @category delegate
         * @see org.neo4j.graphdb.GraphDatabaseService#getRelationshipById(long)
         */
        public Relationship getRelationshipById(long id) {
            return getLazy().getRelationshipById(id);
        }

        /**
         * @return
         * @category delegate
         * @see org.neo4j.graphdb.GraphDatabaseService#getAllNodes()
         * @deprecated
         */
        public Iterable<Node> getAllNodes() {
            return getLazy().getAllNodes();
        }

        /**
         * @return
         * @category delegate
         * @see org.neo4j.graphdb.GraphDatabaseService#getRelationshipTypes()
         * @deprecated
         */
        public Iterable<RelationshipType> getRelationshipTypes() {
            return getLazy().getRelationshipTypes();
        }

        /**
         * @category delegate
         * @see org.neo4j.graphdb.GraphDatabaseService#shutdown()
         */
        public void shutdown() {
            getLazy().shutdown();
        }

        /**
         * @return
         * @category delegate
         * @see org.neo4j.graphdb.GraphDatabaseService#beginTx()
         */
        public Transaction beginTx() {
            return getLazy().beginTx();
        }

        /**
         * @param handler
         * @return
         * @category delegate
         * @see org.neo4j.graphdb.GraphDatabaseService#registerTransactionEventHandler(org.neo4j.graphdb.event.TransactionEventHandler)
         */
        public <T> TransactionEventHandler<T> registerTransactionEventHandler(TransactionEventHandler<T> handler) {
            return getLazy().registerTransactionEventHandler(handler);
        }

        /**
         * @param handler
         * @return
         * @category delegate
         * @see org.neo4j.graphdb.GraphDatabaseService#unregisterTransactionEventHandler(org.neo4j.graphdb.event.TransactionEventHandler)
         */
        public <T> TransactionEventHandler<T> unregisterTransactionEventHandler(TransactionEventHandler<T> handler) {
            return getLazy().unregisterTransactionEventHandler(handler);
        }

        /**
         * @param handler
         * @return
         * @category delegate
         * @see org.neo4j.graphdb.GraphDatabaseService#registerKernelEventHandler(org.neo4j.graphdb.event.KernelEventHandler)
         */
        public KernelEventHandler registerKernelEventHandler(KernelEventHandler handler) {
            return getLazy().registerKernelEventHandler(handler);
        }

        /**
         * @param handler
         * @return
         * @category delegate
         * @see org.neo4j.graphdb.GraphDatabaseService#unregisterKernelEventHandler(org.neo4j.graphdb.event.KernelEventHandler)
         */
        public KernelEventHandler unregisterKernelEventHandler(KernelEventHandler handler) {
            return getLazy().unregisterKernelEventHandler(handler);
        }

        /**
         * @return
         * @category delegate
         * @see org.neo4j.graphdb.GraphDatabaseService#index()
         */
        public IndexManager index() {
            return getLazy().index();
        }

        @Override
        public Node createNode(Label... labels) {
            return getLazy().createNode(labels);
        }

        @Override
        public ResourceIterable<Node> findNodesByLabelAndProperty(Label label, String key, Object value) {
            return getLazy().findNodesByLabelAndProperty(label,key,value);
        }

        @Override
        public boolean isAvailable(long timeout) {
            return getLazy().isAvailable(timeout);
        }

        @Override
        public Schema schema() {
            return getLazy().schema();
        }

        @Override
        public TraversalDescription traversalDescription() {
            return getLazy().traversalDescription();
        }

        @Override
        public BidirectionalTraversalDescription bidirectionalTraversalDescription() {
            return getLazy().bidirectionalTraversalDescription();
        }
    }

    private class LazyLoadableGraphAPI extends LazyLoadedGraphDatabase implements GraphDatabaseAPI {

        @Override
        public DependencyResolver getDependencyResolver() {
            return ((GraphDatabaseAPI) getLazy()).getDependencyResolver();
        }

        @Override
        @Deprecated
        public String getStoreDir() {
            return ((GraphDatabaseAPI) getLazy()).getStoreDir();
        }

        @Override
        @Deprecated
        public StoreId storeId() {
            return ((GraphDatabaseAPI) getLazy()).storeId();
        }

        @Override
        @Deprecated
        public TransactionBuilder tx() {
            return ((GraphDatabaseAPI) getLazy()).tx();
        }

    }

    /**
     * in this test, our class is a graph database service, but not an InternalAbstractGraphDatabase instance.As a consequence, {@link Neo4j2Graph#getInternalIndexKeys}
     * won't load any index, with an additional {@link ClassCastException}
     */
    @Test
    public void loadingANeo4jGraphFromAnyGraphDatabaseClassShouldWork() throws ClassCastException {
        Neo4j2Graph tested = new Neo4j2Graph(new LazyLoadedGraphDatabase());
        assertThat(tested, IsNull.notNullValue());
        // this one isn't a GraphDatabaseAPI. As a consequence, the indexing features are disgraded.
        assertThat(tested.getIndices().iterator().hasNext(), Is.is(false));
    }

    /**
     * in this test, our class is a graph database service, but not an InternalAbstractGraphDatabase instance.As a consequence, {@link Neo4j2Graph#getInternalIndexKeys}
     * won't load any index, with an additional {@link ClassCastException}
     */
    @Test
    public void loadingANeo4jGraphFromAnyGraphAPIClassShouldWork() throws ClassCastException {
        String METHOD_NAME = "#loadingANeo4jGraphFromAnyGraphAPIClassShouldWork";
        Neo4j2Graph tested = new Neo4j2Graph(new LazyLoadableGraphAPI());
        assertThat(tested, IsNull.notNullValue());
        // at startup there is no index
        assertThat(tested.getIndices().iterator().hasNext(), Is.is(false));
        // now create an index
        tested.createIndex(METHOD_NAME, Vertex.class);
        // and now this index exist
        assertThat(tested.getIndices().iterator().hasNext(), Is.is(true));
    }
}
