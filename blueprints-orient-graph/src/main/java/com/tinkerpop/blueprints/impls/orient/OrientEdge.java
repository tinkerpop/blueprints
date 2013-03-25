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
public class OrientEdge extends OrientElement implements Edge {
  private static final long  serialVersionUID = 1L;

  public static final String CLASS_NAME       = "E";

  protected OIdentifiable    vOut;
  protected OIdentifiable    vIn;
  protected String           className;

  public OrientEdge(final OrientBaseGraph rawGraph, final ODocument rawEdge) {
    super(rawGraph, rawEdge);
  }

  public OrientEdge(final OrientBaseGraph rawGraph, final ODocument rawEdge, final Object... fields) {
    super(rawGraph, rawEdge);
    setProperties(fields);
  }

  public OrientEdge(final OrientBaseGraph rawGraph, final OIdentifiable out, final OIdentifiable in) {
    super(rawGraph, null);
    vOut = out;
    vIn = in;
    this.className = CLASS_NAME;
  }

  public OrientEdge(final OrientBaseGraph rawGraph, final OIdentifiable out, final OIdentifiable in, final String className) {
    super(rawGraph, null);
    vOut = out;
    vIn = in;
    this.className = className;
  }

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
    return getRecord().rawField(OrientBaseGraph.CONNECTION_OUT);
  }

  public OIdentifiable getInVertex() {
    if (vIn != null)
      // LIGHTWEIGHT EDGE
      return vIn;
    return getRecord().rawField(OrientBaseGraph.CONNECTION_IN);
  }

  public String getLabel() {
    if (className != null)
      // LIGHTWEIGHT EDGE
      return className;
    return super.getLabel();
  }

  @Override
  public Object getId() {
    if (rawElement == null) {
      // CREATE A TEMPORARY ID
      if (className != null)
        return className + '@' + vOut.getIdentity() + "->" + vIn.getIdentity();

      return vOut.getIdentity() + "->" + vIn.getIdentity();
    }
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
      if (!field.startsWith(OrientBaseGraph.CONNECTION_OUT) && !field.startsWith(OrientBaseGraph.CONNECTION_IN))
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
        @SuppressWarnings("unchecked")
        OrientIndex<OrientEdge> idx = (OrientIndex<OrientEdge>) index;
        idx.removeElement(this);
      }
    }

    // OUT VERTEX
    final OIdentifiable inVertexEdge = vIn != null ? vIn : rawElement;
    final ODocument outVertex = getOutVertex().getRecord();

    final String edgeClassName = getLabel();

    final String outFieldName = OrientVertex.getConnectionFieldName(Direction.OUT, edgeClassName);
    dropEdgeFromVertex(inVertexEdge, outVertex, outFieldName, outVertex.field(outFieldName));

    // IN VERTEX
    final OIdentifiable outVertexEdge = vOut != null ? vOut : rawElement;
    final ODocument inVertex = getInVertex().getRecord();

    final String inFieldName = OrientVertex.getConnectionFieldName(Direction.IN, edgeClassName);
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
    if (rawElement == null)
      return StringFactory.E + StringFactory.L_BRACKET + getId() + StringFactory.R_BRACKET;
    return StringFactory.edgeString(this);
  }

  public static OIdentifiable getConnection(final ODocument iEdgeRecord, final Direction iDirection) {
    return iEdgeRecord.rawField(iDirection == Direction.OUT ? OrientBaseGraph.CONNECTION_OUT : OrientBaseGraph.CONNECTION_IN);
  }

  protected void convertToDocument() {
    if (rawElement != null)
      // ALREADY CONVERTED
      return;

    final ODocument vOutRecord = vOut.getRecord();
    final ODocument vInRecord = vIn.getRecord();

    final ODocument doc = new ODocument(CLASS_NAME);
    if (graph.isUseClassesForLabels()) {
      if (!graph.getRawGraph().getMetadata().getSchema().existsClass(className))
        // CREATE A NEW CLASS AT THE FLY
        graph.getRawGraph().getMetadata().getSchema()
            .createClass(className, graph.getRawGraph().getMetadata().getSchema().getClass(CLASS_NAME));
      doc.setClassName(className);
    }

    doc.field(OrientBaseGraph.CONNECTION_OUT, vOut);
    doc.field(OrientBaseGraph.CONNECTION_IN, vIn);
    rawElement = doc;

    final String outFieldName = OrientVertex.getConnectionFieldName(Direction.OUT, className);
    OrientVertex.removeEdges(vOutRecord, outFieldName, vInRecord, false);

    final String inFieldName = OrientVertex.getConnectionFieldName(Direction.IN, className);
    OrientVertex.removeEdges(vInRecord, inFieldName, vOutRecord, false);

    // OUT-VERTEX ---> IN-VERTEX/EDGE
    OrientVertex.createLink(vOutRecord, doc, outFieldName);

    // IN-VERTEX ---> OUT-VERTEX/EDGE
    OrientVertex.createLink(vInRecord, doc, inFieldName);

    vOutRecord.save();
    vInRecord.save();
    doc.save();

    vOut = null;
    vIn = null;
    className = null;
  }

  @SuppressWarnings("unchecked")
  protected void dropEdgeFromVertex(final OIdentifiable iEdge, final ODocument iVertex, final String iFieldName,
      final Object iFieldValue) {
    if (iFieldValue == null) {
      // NO EDGE? WARN
      OLogManager.instance().warn(this, "Edge not found in vertex's property %s.%s while removing the edge %s",
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