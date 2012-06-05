package com.tinkerpop.blueprints.impls.orient;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.intent.OIntentMassiveInsert;
import com.tinkerpop.blueprints.Features;

/**
 * A Blueprints implementation of the graph database OrientDB (http://www.orientechnologies.com)
 *
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientBatchGraph extends OrientBaseGraph {
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
        FEATURES.supportsTransactions = false;
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
    public OrientBatchGraph(final OGraphDatabase iDatabase) {
        super(iDatabase);
        getRawGraph().declareIntent( new OIntentMassiveInsert() );
    }

    public OrientBatchGraph(final String url) {
        super(url, ADMIN, ADMIN);
        getRawGraph().declareIntent( new OIntentMassiveInsert() );
    }

    public OrientBatchGraph(final String url, final String username, final String password) {
        super(url, username, password);
        getRawGraph().declareIntent( new OIntentMassiveInsert() );
    }

    public Features getFeatures() {
        return FEATURES;
    }
}