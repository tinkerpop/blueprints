package com.tinkerpop.blueprints.impls.orient;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.tinkerpop.blueprints.Features;

/**
 * A Blueprints implementation of the graph database OrientDB (http://www.orientechnologies.com)
 * 
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientGraph extends OrientTransactionalGraph {
  protected final Features FEATURES = new Features();

  /**
   * Constructs a new object using an existent OGraphDatabase instance.
   * 
   * @param iDatabase
   *          Underlying OGraphDatabase object to attach
   */
  public OrientGraph(final OGraphDatabase iDatabase) {
    super(iDatabase);
    config();
  }

  public OrientGraph(final String url) {
    super(url, ADMIN, ADMIN);
    config();
  }

  public OrientGraph(final String url, final String username, final String password) {
    super(url, username, password);
    config();
  }

  public Features getFeatures() {
    // DYNAMIC FEATURES BASED ON CONFIGURATION
    FEATURES.supportsEdgeIndex = !useLightweightEdges;
    FEATURES.supportsEdgeKeyIndex = !useLightweightEdges;
    FEATURES.supportsEdgeIteration = !useLightweightEdges;
    FEATURES.supportsEdgeRetrieval = !useLightweightEdges;
    return FEATURES;
  }

  @SuppressWarnings("deprecation")
  protected void config() {
    FEATURES.supportsDuplicateEdges = true;
    FEATURES.supportsSelfLoops = true;
    FEATURES.isPersistent = true;
    FEATURES.isRDFModel = false;
    FEATURES.supportsVertexIteration = true;
    FEATURES.supportsVertexIndex = true;
    FEATURES.ignoresSuppliedIds = true;
    FEATURES.supportsTransactions = true;
    FEATURES.supportsVertexKeyIndex = true;
    FEATURES.supportsKeyIndices = true;
    FEATURES.isWrapper = false;
    FEATURES.supportsIndices = true;
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
}