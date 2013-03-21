package com.tinkerpop.blueprints.impls.orient;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.tinkerpop.blueprints.Features;

/**
 * A Blueprints implementation of the graph database OrientDB (http://www.orientechnologies.com) that supports indexable edges.
 * 
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientGraphNonDynamic extends OrientTransactionalGraph {
  private static final Features FEATURES    = new Features();

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

  protected boolean             autoStartTx = true;

  /**
   * Constructs a new object using an existent OGraphDatabase instance.
   * 
   * @param iDatabase
   *          Underlying OGraphDatabase object to attach
   */
  public OrientGraphNonDynamic(final OGraphDatabase iDatabase) {
    super(iDatabase);
    useDynamicEdges = false;
  }

  public OrientGraphNonDynamic(final String url) {
    super(url, ADMIN, ADMIN);
    useDynamicEdges = false;
  }

  public OrientGraphNonDynamic(final String url, final String username, final String password) {
    super(url, username, password);
    useDynamicEdges = false;
  }

  public Features getFeatures() {
    return FEATURES;
  }
}