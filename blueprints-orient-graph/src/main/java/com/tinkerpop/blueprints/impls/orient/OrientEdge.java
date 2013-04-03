package com.tinkerpop.blueprints.impls.orient;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.orientechnologies.common.log.OLogManager;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.type.tree.OMVRBTreeRIDSet;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import com.tinkerpop.blueprints.util.StringFactory;

/**
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
@SuppressWarnings("unchecked")
public class OrientEdge extends OrientElement implements Edge {
  private static final long  serialVersionUID = 1L;

  public static final String CLASS_NAME       = "E";
  public static final String LABEL_FIELD_NAME = "label";

  protected OIdentifiable    vOut;
  protected OIdentifiable    vIn;
  protected String           className;

  public OrientEdge(final OrientBaseGraph rawGraph, final ODocument rawEdge) {
    super(rawGraph, rawEdge);
  }

  public OrientEdge(final OrientBaseGraph rawGraph, final String iClassName, final Object... fields) {
    super(rawGraph, null);
    rawElement = createDocument(iClassName);
    setProperties(fields);
  }

  public OrientEdge(final OrientBaseGraph rawGraph, final OIdentifiable out, final OIdentifiable in) {
    this(rawGraph, out, in, CLASS_NAME);
  }

  public OrientEdge(final OrientBaseGraph rawGraph, final OIdentifiable out, final OIdentifiable in, final String className) {
    super(rawGraph, null);
    vOut = out;
    vIn = in;
    this.className = className;
  }

  @Override
  public Vertex getVertex(final Direction direction) {
    if (direction.equals(Direction.OUT))
      return new OrientVertex(graph, getOutVertex());
    else if (direction.equals(Direction.IN))
      return new OrientVertex(graph, getInVertex());
    else
      throw ExceptionFactory.bothIsNotSupported();
  }

  public OIdentifiable getOutVertex() {
    if (vOut != null)
      // LIGHTWEIGHT EDGE
      return vOut;
    if (graph.isKeepInMemoryReferences())
      // AVOID LAZY RESOLVING+SETTING OF RECORD
      return getRecord().rawField(OrientBaseGraph.CONNECTION_OUT);
    else
      return getRecord().field(OrientBaseGraph.CONNECTION_OUT);
  }

  public OIdentifiable getInVertex() {
    if (vIn != null)
      // LIGHTWEIGHT EDGE
      return vIn;
    if (graph.isKeepInMemoryReferences())
      // AVOID LAZY RESOLVING+SETTING OF RECORD
      return getRecord().rawField(OrientBaseGraph.CONNECTION_IN);
    else
      return getRecord().field(OrientBaseGraph.CONNECTION_IN);
  }

  @Override
  public String getLabel() {
    if (className != null)
      // LIGHTWEIGHT EDGE
      return className;
    else if (rawElement != null) {
      String label = ((ODocument) rawElement.getRecord()).field(LABEL_FIELD_NAME);
      if (label != null)
        return OrientBaseGraph.decodeClassName(label);
      label = ((ODocument) rawElement.getRecord()).getClassName();
      if (label != null && !label.equals(CLASS_NAME))
        return OrientBaseGraph.decodeClassName(label);
    }
    return null;
  }

  @Override
  public boolean equals(final Object object) {
    if (rawElement == null && object instanceof OrientEdge) {
      final OrientEdge other = (OrientEdge) object;
      return vOut.equals(other.vOut) && vIn.equals(other.vIn)
          && (className == null && other.className == null || className.equals(other.className));
    }
    return super.equals(object);
  }

  @Override
  public Object getId() {
    if (rawElement == null)
      // CREATE A TEMPORARY ID
      return vOut.getIdentity() + "->" + vIn.getIdentity();

    return super.getId();
  }

  @Override
  public <T> T getProperty(final String key) {
    if (rawElement == null)
      // LIGHTWEIGHT EDGE
      return null;

    return super.getProperty(key);
  }

  @Override
  public Set<String> getPropertyKeys() {
    if (rawElement == null)
      // LIGHTWEIGHT EDGE
      return Collections.emptySet();

    final Set<String> result = new HashSet<String>();

    for (String field : getRecord().fieldNames())
      if (!field.equals(OrientBaseGraph.CONNECTION_OUT) && !field.equals(OrientBaseGraph.CONNECTION_IN)
          && (graph.isUseCustomClassesForEdges() || !field.equals(LABEL_FIELD_NAME)))
        result.add(field);

    return result;
  }

  @Override
  public void setProperty(final String key, final Object value) {
    if (rawElement == null)
      // LIGHTWEIGHT EDGE
      convertToDocument();

    super.setProperty(key, value);
  }

  @Override
  public <T> T removeProperty(String key) {
    if (rawElement != null)
      // NON LIGHTWEIGHT EDGE
      return super.removeProperty(key);
    return null;
  }

  @Override
  public void remove() {
    graph.autoStartTransaction();
    for (final Index<? extends Element> index : graph.getManualIndices()) {
      if (Edge.class.isAssignableFrom(index.getIndexClass())) {
        OrientIndex<OrientEdge> idx = (OrientIndex<OrientEdge>) index;
        idx.removeElement(this);
      }
    }

    // OUT VERTEX
    final OIdentifiable inVertexEdge = vIn != null ? vIn : rawElement;
    final ODocument outVertex = getOutVertex().getRecord();

    final String edgeClassName = OrientBaseGraph.encodeClassName(getLabel());

    final boolean useVertexFieldsForEdgeLabels = graph.isUseVertexFieldsForEdgeLabels();

    final String outFieldName = OrientVertex.getConnectionFieldName(Direction.OUT, edgeClassName, useVertexFieldsForEdgeLabels);
    dropEdgeFromVertex(inVertexEdge, outVertex, outFieldName, outVertex.field(outFieldName));

    // IN VERTEX
    final OIdentifiable outVertexEdge = vOut != null ? vOut : rawElement;
    final ODocument inVertex = getInVertex().getRecord();

    final String inFieldName = OrientVertex.getConnectionFieldName(Direction.IN, edgeClassName, useVertexFieldsForEdgeLabels);
    dropEdgeFromVertex(outVertexEdge, inVertex, inFieldName, inVertex.field(inFieldName));

    outVertex.save();
    inVertex.save();

    if (rawElement != null)
      // NON-LIGHTWEIGHT EDGE
      super.remove();
  }

  public final String getBaseClassName() {
    return CLASS_NAME;
  }

  @Override
  public String getElementType() {
    return "Edge";
  }

  public String toString() {
    if (getLabel() == null)
      return StringFactory.E + StringFactory.L_BRACKET + getId() + StringFactory.R_BRACKET + StringFactory.L_BRACKET
          + getVertex(Direction.OUT).getId() + StringFactory.ARROW + getVertex(Direction.IN).getId() + StringFactory.R_BRACKET;

    return StringFactory.edgeString(this);
  }

  public static OIdentifiable getConnection(final ODocument iEdgeRecord, final Direction iDirection) {
    return iEdgeRecord.rawField(iDirection == Direction.OUT ? OrientBaseGraph.CONNECTION_OUT : OrientBaseGraph.CONNECTION_IN);
  }

  /**
   * Returns true if the edge is labeled with any of the passed strings.
   * 
   * @param iEdge
   *          Edge
   * @param iLabels
   *          Labels as array of Strings
   * @return
   */
  protected boolean isLabeled(final String[] iLabels) {
    return isLabeled(getLabel(), iLabels);
  }

  /**
   * Returns true if the edge is labeled with any of the passed strings.
   * 
   * @param iEdge
   *          Edge
   * @param iLabels
   *          Labels as array of Strings
   * @return
   */
  public static boolean isLabeled(final String iEdgeLabel, final String[] iLabels) {
    if (iLabels != null && iLabels.length > 0) {
      // FILTER LABEL
      if (iEdgeLabel != null)
        for (String l : iLabels)
          if (l.equals(iEdgeLabel))
            // FOUND
            return true;

      // NOT FOUND
      return false;
    }
    // NO LABELS
    return true;
  }

  public static String getRecordLabel(final OIdentifiable iEdge) {
    if (iEdge == null)
      return null;

    final ODocument edge = iEdge.getRecord();
    if (edge == null)
      return null;

    return edge.field(LABEL_FIELD_NAME);
  }

  protected void convertToDocument() {
    if (rawElement != null)
      // ALREADY CONVERTED
      return;

    graph.autoStartTransaction();

    final ODocument vOutRecord = vOut.getRecord();
    final ODocument vInRecord = vIn.getRecord();

    final ODocument doc = createDocument(className);

    doc.field(OrientBaseGraph.CONNECTION_OUT, graph.isKeepInMemoryReferences() ? vOutRecord.getIdentity() : vOutRecord);
    doc.field(OrientBaseGraph.CONNECTION_IN, graph.isKeepInMemoryReferences() ? vInRecord.getIdentity() : vInRecord);
    rawElement = doc;

    final boolean useVertexFieldsForEdgeLabels = graph.isUseVertexFieldsForEdgeLabels();

    final String outFieldName = OrientVertex.getConnectionFieldName(Direction.OUT, className, useVertexFieldsForEdgeLabels);
    OrientVertex.removeEdges(vOutRecord, outFieldName, vInRecord, false, useVertexFieldsForEdgeLabels);

    final String inFieldName = OrientVertex.getConnectionFieldName(Direction.IN, className, useVertexFieldsForEdgeLabels);
    OrientVertex.removeEdges(vInRecord, inFieldName, vOutRecord, false, useVertexFieldsForEdgeLabels);

    // OUT-VERTEX ---> IN-VERTEX/EDGE
    OrientVertex.createLink(vOutRecord, doc, outFieldName);

    // IN-VERTEX ---> OUT-VERTEX/EDGE
    OrientVertex.createLink(vInRecord, doc, inFieldName);

    doc.save();
    vOutRecord.save();
    vInRecord.save();

    vOut = null;
    vIn = null;
    className = null;
  }

  protected ODocument createDocument(final String iClassName) {
    final ODocument doc;

    if (iClassName != null && graph.isUseCustomClassesForEdges()) {
      checkForClassInSchema(iClassName);
      doc = new ODocument(iClassName);
    } else {
      doc = new ODocument(CLASS_NAME);
      if (iClassName != null)
        doc.field(LABEL_FIELD_NAME, iClassName);
    }

    return doc;
  }

  protected void dropEdgeFromVertex(final OIdentifiable iEdge, final ODocument iVertex, final String iFieldName,
      final Object iFieldValue) {
    if (iFieldValue == null) {
      // NO EDGE? WARN
      OLogManager.instance().debug(this, "Edge not found in vertex's property %s.%s while removing the edge %s",
          iVertex.getIdentity(), iFieldName, iEdge.getIdentity());

    } else if (iFieldValue instanceof OIdentifiable) {
      // FOUND A SINGLE ITEM: JUST REMOVE IT

      if (iFieldValue.equals(iEdge))
        iVertex.field(iFieldName, (OIdentifiable) null);
      else
        // NO EDGE? WARN
        OLogManager.instance().warn(this, "Edge not found in vertex's property %s.%s link while removing the edge %s",
            iVertex.getIdentity(), iFieldName, iEdge.getIdentity());

    } else if (iFieldValue instanceof OMVRBTreeRIDSet) {
      // ALREADY A SET: JUST REMOVE THE NEW EDGE
      if (!((OMVRBTreeRIDSet) iFieldValue).remove(iEdge))
        OLogManager.instance().warn(this, "Edge not found in vertex's property %s.%s set while removing the edge %s",
            iVertex.getIdentity(), iFieldName, iEdge.getIdentity());
    } else if (iFieldValue instanceof Collection<?>) {
      // CONVERT COLLECTION IN TREE-SET AND REMOVE THE EDGE
      final OMVRBTreeRIDSet out = new OMVRBTreeRIDSet(iVertex, (Collection<OIdentifiable>) iFieldValue);
      if (!out.remove(iEdge))
        OLogManager.instance().warn(this, "Edge not found in vertex's property %s.%s collection while removing the edge %s",
            iVertex.getIdentity(), iFieldName, iEdge.getIdentity());
      else
        iVertex.field(iFieldName, out);
    } else
      throw new IllegalStateException("Wrong type found in the field '" + iFieldName + "': " + iFieldValue.getClass());
  }
}