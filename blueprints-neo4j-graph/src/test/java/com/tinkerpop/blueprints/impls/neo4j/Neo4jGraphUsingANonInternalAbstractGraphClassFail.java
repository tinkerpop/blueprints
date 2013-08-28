package com.tinkerpop.blueprints.impls.neo4j;

import com.tinkerpop.blueprints.Vertex;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.Test;
import org.neo4j.graphdb.DependencyResolver;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.event.KernelEventHandler;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.kernel.IdGeneratorFactory;
import org.neo4j.kernel.KernelData;
import org.neo4j.kernel.TransactionBuilder;
import org.neo4j.kernel.guard.Guard;
import org.neo4j.kernel.impl.core.KernelPanicEventGenerator;
import org.neo4j.kernel.impl.core.NodeManager;
import org.neo4j.kernel.impl.core.RelationshipTypeHolder;
import org.neo4j.kernel.impl.nioneo.store.StoreId;
import org.neo4j.kernel.impl.persistence.PersistenceSource;
import org.neo4j.kernel.impl.transaction.LockManager;
import org.neo4j.kernel.impl.transaction.XaDataSourceManager;
import org.neo4j.kernel.impl.transaction.xaframework.TxIdGenerator;
import org.neo4j.kernel.impl.util.StringLogger;
import org.neo4j.kernel.info.DiagnosticsManager;

import javax.transaction.TransactionManager;

import static org.junit.Assert.assertThat;

public class Neo4jGraphUsingANonInternalAbstractGraphClassFail {
    private class LazyLoadedGraphDatabase implements GraphDatabaseService {
        private GraphDatabaseService lazy;

        public GraphDatabaseService getLazy() {
            if (lazy == null) {
                // load that lazy graph in a unique folder
                lazy = new EmbeddedGraphDatabase(getClass().getName() + "/" + System.currentTimeMillis());
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
         * @see org.neo4j.graphdb.GraphDatabaseService#getReferenceNode()
         * @deprecated
         */
        public Node getReferenceNode() {
            return getLazy().getReferenceNode();
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
    }

    private class LazyLoadableGraphAPI extends LazyLoadedGraphDatabase implements GraphDatabaseAPI {

        @Override
        public DependencyResolver getDependencyResolver() {
            return ((GraphDatabaseAPI) getLazy()).getDependencyResolver();
        }

        @Override
        @Deprecated
        public DiagnosticsManager getDiagnosticsManager() {
            return ((GraphDatabaseAPI) getLazy()).getDiagnosticsManager();
        }

        @Override
        @Deprecated
        public Guard getGuard() {
            return ((GraphDatabaseAPI) getLazy()).getGuard();
        }

        @Override
        @Deprecated
        public IdGeneratorFactory getIdGeneratorFactory() {
            return ((GraphDatabaseAPI) getLazy()).getIdGeneratorFactory();
        }

        @Override
        @Deprecated
        public KernelData getKernelData() {
            return ((GraphDatabaseAPI) getLazy()).getKernelData();
        }

        @Override
        @Deprecated
        public KernelPanicEventGenerator getKernelPanicGenerator() {
            return ((GraphDatabaseAPI) getLazy()).getKernelPanicGenerator();
        }

        @Override
        @Deprecated
        public LockManager getLockManager() {
            return ((GraphDatabaseAPI) getLazy()).getLockManager();
        }

        @Override
        @Deprecated
        public StringLogger getMessageLog() {
            return ((GraphDatabaseAPI) getLazy()).getMessageLog();
        }

        @Override
        @Deprecated
        public NodeManager getNodeManager() {
            return ((GraphDatabaseAPI) getLazy()).getNodeManager();
        }

        @Override
        @Deprecated
        public PersistenceSource getPersistenceSource() {
            return ((GraphDatabaseAPI) getLazy()).getPersistenceSource();
        }

        @Override
        @Deprecated
        public RelationshipTypeHolder getRelationshipTypeHolder() {
            return ((GraphDatabaseAPI) getLazy()).getRelationshipTypeHolder();
        }

        @Override
        @Deprecated
        public String getStoreDir() {
            return ((GraphDatabaseAPI) getLazy()).getStoreDir();
        }

        @Override
        @Deprecated
        public StoreId getStoreId() {
            return ((GraphDatabaseAPI) getLazy()).getStoreId();
        }

        @Override
        @Deprecated
        public TxIdGenerator getTxIdGenerator() {
            return ((GraphDatabaseAPI) getLazy()).getTxIdGenerator();
        }

        @Override
        @Deprecated
        public TransactionManager getTxManager() {
            return ((GraphDatabaseAPI) getLazy()).getTxManager();
        }

        @Override
        @Deprecated
        public XaDataSourceManager getXaDataSourceManager() {
            return ((GraphDatabaseAPI) getLazy()).getXaDataSourceManager();
        }

        @Override
        @Deprecated
        public TransactionBuilder tx() {
            return ((GraphDatabaseAPI) getLazy()).tx();
        }

    }

    /**
     * in this test, our class is a graph database service, but not an InternalAbstractGraphDatabase instance.As a consequence, {@link Neo4jGraph#getInternalIndexKeys}
     * won't load any index, with an additional {@link ClassCastException}
     */
    /*@Test
    public void loadingANeo4jGraphFromAnyGraphDatabaseClassShouldWork() throws ClassCastException {
        Neo4jGraph tested = new Neo4jGraph(new LazyLoadedGraphDatabase());
        assertThat(tested, IsNull.notNullValue());
        // this one isn't a GraphDatabaseAPI. As a consequence, the indexing features are disgraded.
        assertThat(tested.getIndices().iterator().hasNext(), Is.is(false));
    }*/

    /**
     * in this test, our class is a graph database service, but not an InternalAbstractGraphDatabase instance.As a consequence, {@link Neo4jGraph#getInternalIndexKeys}
     * won't load any index, with an additional {@link ClassCastException}
     */
    /*@Test
    public void loadingANeo4jGraphFromAnyGraphAPIClassShouldWork() throws ClassCastException {
        String METHOD_NAME = "#loadingANeo4jGraphFromAnyGraphAPIClassShouldWork";
        Neo4jGraph tested = new Neo4jGraph(new LazyLoadableGraphAPI());
        assertThat(tested, IsNull.notNullValue());
        // at startup there is no index
        assertThat(tested.getIndices().iterator().hasNext(), Is.is(false));
        // now create an index
        tested.createIndex(METHOD_NAME, Vertex.class);
        // and now this index exist
        assertThat(tested.getIndices().iterator().hasNext(), Is.is(true));
    }*/
}
