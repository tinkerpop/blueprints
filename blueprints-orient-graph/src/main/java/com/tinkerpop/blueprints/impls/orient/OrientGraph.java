package com.tinkerpop.blueprints.impls.orient;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.tx.OTransaction.TXSTATUS;
import com.orientechnologies.orient.core.tx.OTransactionNoTx;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.util.ExceptionFactory;

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
        FEATURES.supportsThreadedTransactions = false;
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
    public void startTransaction() {
        final OrientGraphContext context = getContext(true);

        if (context.rawGraph.getTransaction() instanceof OTransactionNoTx && context.rawGraph.getTransaction().getStatus() != TXSTATUS.BEGUN) {
            context.rawGraph.begin();
        } else
            throw ExceptionFactory.transactionAlreadyStarted();
    }

    @Override
    public void stopTransaction(final Conclusion conclusion) {
        if (conclusion == Conclusion.FAILURE) {
            this.getRawGraph().rollback();
        } else
            this.getRawGraph().commit();
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