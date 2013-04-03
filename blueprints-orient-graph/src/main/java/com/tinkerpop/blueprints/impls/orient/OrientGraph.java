package com.tinkerpop.blueprints.impls.orient;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.tinkerpop.blueprints.Features;

/**
 * A Blueprints implementation of the graph database OrientDB (http://www.orientechnologies.com)
 * 
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientGraph extends OrientTransactionalGraph {
  private static final Features FEATURES    = new Features();

  static {
    FEATURES.supportsDuplicateEdges = true;
    FEATURES.supportsSelfLoops = true;
    FEATURES.isPersistent = true;
    FEATURES.isRDFModel = false;
    FEATURES.supportsVertexIteration = true;
    FEATURES.supportsEdgeIteration = false;
    FEATURES.supportsVertexIndex = true;
    FEATURES.supportsEdgeIndex = false;
    FEATURES.ignoresSuppliedIds = true;
    FEATURES.supportsTransactions = true;
    FEATURES.supportsEdgeKeyIndex = false;
    FEATURES.supportsVertexKeyIndex = true;
    FEATURES.supportsKeyIndices = true;
    FEATURES.isWrapper = false;
    FEATURES.supportsIndices = true;
    FEATURES.supportsEdgeRetrieval = false;
    FEATURES.supportsVertexProperties = true;
    FEATURES.supportsEdgeProperties = true; // ACTUALLY THIS IS DYNAMIC

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
   * @param iDatabase
   *          Underlying OGraphDatabase object to attach
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

  public Features getFeatures() {
    return FEATURES;
  }
}