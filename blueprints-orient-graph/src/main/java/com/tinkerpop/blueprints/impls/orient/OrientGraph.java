package com.tinkerpop.blueprints.impls.orient;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.tx.OTransaction.TXSTATUS;
import com.orientechnologies.orient.core.tx.OTransactionNoTx;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.TransactionalGraph;

/**
 * A Blueprints implementation of the graph database OrientDB (http://www.orientechnologies.com)
 *
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientGraph extends OrientBaseGraph implements TransactionalGraph {
    private static final Features FEATURES = new Features();

    static {
        FEATURES.supportsDuplicateEdges = true;
        FEATURES.supportsSelfLoops = true;
        FEATURES.isPersistent = true;
        FEATURES.isRDFModel = false;
        FEATURES.supportsVertexIteration = true;
        FEATURES.supportsEdgeIteration = true;
        FEATURES.supportsVertexIndex = true;
        FEATURES.supportsEdgeIndex = true;
        FEATURES.ignoresSuppliedIds = true;
        FEATURES.supportsTransactions = true;
        FEATURES.supportsEdgeKeyIndex = true;
        FEATURES.supportsVertexKeyIndex = true;
        FEATURES.supportsKeyIndices = true;
        FEATURES.isWrapper = false;
        FEATURES.supportsIndices = true;
        FEATURES.supportsEdgeRetrieval = true;
        FEATURES.supportsVertexProperties = true;
        FEATURES.supportsEdgeProperties = true;


        // For more information on supported types, please see:
        // http://code.google.com/p/orient/wiki/Types
        FEATURES.supportsSerializableObjectProperty = true;
        FEATURES.supportsBooleanProperty = true;
        FEATURES.supportsDoubleProperty = true;
        FEATURES.supportsFloatProperty = true;
        FEATURES.supportsIntegerProperty = true;
        FEATURES.supportsPrimitiveArrayProperty = true;
        FEATURES.supportsUniformListProperty = true;
        FEATURES.supportsMixedListProperty = true;
        FEATURES.supportsLongProperty = true;
        FEATURES.supportsMapProperty = true;
        FEATURES.supportsStringProperty = true;
        FEATURES.supportsThreadedTransactions = true;
    }

    /**
     * Constructs a new object using an existent OGraphDatabase instance.
     *
     * @param iDatabase Underlying OGraphDatabase object to attach
     */
    public OrientGraph(final OGraphDatabase iDatabase) {
        super(iDatabase);
    }

    public OrientGraph(final String url) {
        super(url, ADMIN, ADMIN);
    }

    public OrientGraph(final String url, final String username, final String password) {
        super(url, username, password);
    }

    @Override
    public void stopTransaction(Conclusion conclusion) {
        if (Conclusion.SUCCESS == conclusion)
            commit();
        else
            rollback();
    }

    public void commit() {
        this.getRawGraph().commit();
    }

    public void rollback() {
        this.getRawGraph().rollback();
    }

    @Override
    protected void autoStartTransaction() {
        final OrientGraphContext context = getContext(true);
        if (context.rawGraph.getTransaction() instanceof OTransactionNoTx && context.rawGraph.getTransaction().getStatus() != TXSTATUS.BEGUN) {
            context.rawGraph.begin();
        }
    }

    public Features getFeatures() {
        return FEATURES;
    }
}