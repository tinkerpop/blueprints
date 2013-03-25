package com.tinkerpop.blueprints.impls.orient;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.orientechnologies.common.collection.OLazyIterator;
import com.orientechnologies.common.util.OPair;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.db.record.ORecordLazyMultiValue;
import com.orientechnologies.orient.core.iterator.OLazyWrapperIterator;
import com.orientechnologies.orient.core.iterator.OMultiCollectionIterator;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.type.tree.OMVRBTreeRIDSet;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexQuery;
import com.tinkerpop.blueprints.util.StringFactory;

/**
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
@SuppressWarnings("unchecked")
public class OrientVertex extends OrientElement implements Vertex {
  private static final long  serialVersionUID      = 1L;
  public static final String CLASS_NAME            = "V";
  public static final String CONNECTION_OUT_PREFIX = OrientBaseGraph.CONNECTION_OUT + "_";
  public static final String CONNECTION_IN_PREFIX  = OrientBaseGraph.CONNECTION_IN + "_";

  public OrientVertex(final OrientBaseGraph graph, final String className) {
    super(graph, new ODocument(className == null ? CLASS_NAME : className));
  }

  protected OrientVertex(final OrientBaseGraph graph, final OIdentifiable record) {
    super(graph, record);
  }

  @Override
  public Set<String> getPropertyKeys() {
    final ODocument doc = getRecord();

    final Set<String> result = new HashSet<String>();
    for (String field : doc.fieldNames())
      if (!field.startsWith(CONNECTION_OUT_PREFIX) && !field.startsWith(CONNECTION_IN_PREFIX))
        result.add(field);

    return result;
  }

  /**
   * Returns a lazy iterable instance against vertices.
   */
  @Override
  public Iterable<Vertex> getVertices(final Direction direction, final String... labels) {

    return new OLazyWrapperIterator<Vertex>(getVerticesAsRecords(direction, labels).iterator()) {
      @Override
      public Vertex createWrapper(final Object iObject) {
        // CREATE THE OBJECT LAZILY
        return iObject instanceof Vertex ? (Vertex) iObject : new OrientVertex(graph, (OIdentifiable) iObject);
      }
    };
  }

  public Iterable<Vertex> getVerticesAsRecords(final Direction direction, final String... labels) {
    final ODocument doc = getRecord();

    final OMultiCollectionIterator<Vertex> iterable = new OMultiCollectionIterator<Vertex>();
    for (String fieldName : doc.fieldNames()) {
      final OPair<Direction, String> connection = getConnection(direction, fieldName, labels);
      if (connection == null)
        // SKIP THIS FIELD
        continue;

      final Object fieldValue = doc.field(fieldName);
      if (fieldValue != null)
        if (fieldValue instanceof OIdentifiable) {
          final ODocument fieldRecord = ((OIdentifiable) fieldValue).getRecord();
          if (fieldRecord.getSchemaClass().isSubClassOf(CLASS_NAME))
            // DIRECT VERTEX
            iterable.add(fieldValue);
          else if (fieldRecord.getSchemaClass().isSubClassOf(OrientEdge.CLASS_NAME))
            // EDGE
            iterable.add(OrientEdge.getConnection(fieldRecord, connection.getKey().opposite()));
          else
            throw new IllegalStateException("Invalid content found in " + fieldName + " field: " + fieldRecord);

        } else if (fieldValue instanceof Collection<?>) {
          iterable.add(fieldValue);
        }
    }

    return iterable;
  }

  @Override
  public VertexQuery query() {
    return new OrientVertexQuery(this);
  }

  @Override
  public void remove() {
    final ODocument doc = getRecord();
    if (doc == null)
      return;

    graph.autoStartTransaction();

    final Iterator<OrientIndex<? extends OrientElement>> it = graph.getManualIndices().iterator();

    if (it.hasNext()) {
      final Set<Edge> allEdges = new HashSet<Edge>();
      for (Edge e : getEdges(Direction.BOTH))
        allEdges.add(e);

      while (it.hasNext()) {
        final Index<? extends Element> index = it.next();

        if (Vertex.class.isAssignableFrom(index.getIndexClass())) {
          OrientIndex<OrientVertex> idx = (OrientIndex<OrientVertex>) index;
          idx.removeElement(this);
        }

        if (Edge.class.isAssignableFrom(index.getIndexClass())) {
          OrientIndex<OrientEdge> idx = (OrientIndex<OrientEdge>) index;
          for (Edge e : allEdges)
            idx.removeElement((OrientEdge) e);
        }
      }
    }

    for (String fieldName : doc.fieldNames()) {
      final OPair<Direction, String> connection = getConnection(Direction.BOTH, fieldName);
      if (connection == null)
        // SKIP THIS FIELD
        continue;

      removeEdges(doc, fieldName, null, true);
    }

    super.remove();
  }

  @Override
  public Edge addEdge(final String label, final Vertex inVertex) {
    return addEdge(label, inVertex, (Object[]) null);
  }

  public Edge addEdge(final String label, final Vertex in, final Object... fields) {
    if (in == null)
      throw new IllegalArgumentException("destination vertex is null");

    graph.autoStartTransaction();

    final ODocument outVertex = getRecord();
    final ODocument inVertex = ((OrientVertex) in).getRecord();

    final OrientEdge edge;
    final OIdentifiable to;
    final OIdentifiable from;

    if (graph.isUseDynamicEdges() && (fields == null || fields.length == 0)) {
      // CREATE A LIGHTWEIGHT EDGE
      from = rawElement;
      to = inVertex;
      edge = new OrientEdge(graph, from, to, label);
    } else {
      // CREATE THE EDGE DOCUMENT TO STORE FIELDS TOO
      final ODocument edgeDocument = new ODocument(OrientEdge.CLASS_NAME);
      if (graph.isUseClassesForLabels())
        edgeDocument.setClassName(label);
      edgeDocument.fields(OrientBaseGraph.CONNECTION_OUT, rawElement, OrientBaseGraph.CONNECTION_IN, inVertex);

      edge = new OrientEdge(graph, edgeDocument, fields);
      from = (OIdentifiable) edge;
      to = (OIdentifiable) edge;
    }

    // OUT-VERTEX ---> IN-VERTEX/EDGE
    final String outFieldName = getConnectionFieldName(Direction.OUT, label);
    createLink(outVertex, to, outFieldName);

    // IN-VERTEX ---> OUT-VERTEX/EDGE
    final String inFieldName = getConnectionFieldName(Direction.IN, label);
    createLink(inVertex, from, inFieldName);

    outVertex.save();
    inVertex.save();
    edge.save();

    return edge;
  }

  public Iterable<Edge> getEdges(final Direction iDirection, final String... iLabels) {
    final ODocument doc = getRecord();

    final OMultiCollectionIterator<Edge> iterable = new OMultiCollectionIterator<Edge>();
    for (String fieldName : doc.fieldNames()) {
      final OPair<Direction, String> connection = getConnection(iDirection, fieldName, iLabels);
      if (connection == null)
        // SKIP THIS FIELD
        continue;

      final Object fieldValue = doc.field(fieldName);
      if (fieldValue != null)
        if (fieldValue instanceof OIdentifiable) {
          addSingleItem(doc, iterable, fieldName, connection, fieldValue);

        } else if (fieldValue instanceof Collection<?>) {
          Collection<?> coll = (Collection<?>) fieldValue;

          if (coll.size() == 1) {
            // SINGLE ITEM: AVOID CALLING ITERATOR
            if (coll instanceof ORecordLazyMultiValue)
              addSingleItem(doc, iterable, fieldName, connection, ((ORecordLazyMultiValue) coll).rawIterator().next());
            else if (coll instanceof List<?>)
              addSingleItem(doc, iterable, fieldName, connection, ((List<?>) coll).get(0));
            else
              addSingleItem(doc, iterable, fieldName, connection, coll.iterator().next());
          } else {
            final Iterator<?> it = coll instanceof ORecordLazyMultiValue ? ((ORecordLazyMultiValue) coll).rawIterator() : coll
                .iterator();

            // CREATE LAZY Iterable AGAINST COLLECTION FIELD
            iterable.add(new OLazyWrapperIterator<OrientEdge>(it, connection) {
              @Override
              public OrientEdge createWrapper(final Object iObject) {
                if (iObject instanceof OrientEdge)
                  return (OrientEdge) iObject;

                final ODocument value = ((OIdentifiable) iObject).getRecord();
                if (value.getSchemaClass().isSubClassOf(CLASS_NAME)) {
                  // DIRECT VERTEX, CREATE DUMMY EDGE
                  final OPair<Direction, String> conn = (OPair<Direction, String>) additionalData;
                  if (conn.getKey() == Direction.OUT)
                    return new OrientEdge(graph, doc, value, conn.getValue());
                  else
                    return new OrientEdge(graph, value, doc, conn.getValue());
                } else if (value.getSchemaClass().isSubClassOf(OrientEdge.CLASS_NAME)) {
                  // EDGE
                  return new OrientEdge(graph, value);
                } else
                  throw new IllegalStateException("Invalid content found between connections:" + value);
              }
            });
          }
        }
    }

    return iterable;
  }

  private void addSingleItem(final ODocument doc, final OMultiCollectionIterator<Edge> iterable, String fieldName,
      final OPair<Direction, String> connection, final Object fieldValue) {
    final ODocument fieldRecord = ((OIdentifiable) fieldValue).getRecord();
    if (fieldRecord.getSchemaClass().isSubClassOf(CLASS_NAME)) {
      // DIRECT VERTEX, CREATE A DUMMY EDGE BETWEEN VERTICES
      if (connection.getKey() == Direction.OUT)
        iterable.add(new OrientEdge(graph, doc, fieldRecord, connection.getValue()));
      else
        iterable.add(new OrientEdge(graph, fieldRecord, doc, connection.getValue()));

    } else if (fieldRecord.getSchemaClass().isSubClassOf(OrientEdge.CLASS_NAME)) {
      // EDGE
      iterable.add(new OrientEdge(graph, fieldRecord));
    } else
      throw new IllegalStateException("Invalid content found in " + fieldName + " field: " + fieldRecord);
  }

  @Override
  public String getBaseClassName() {
    return CLASS_NAME;
  }

  @Override
  public String getElementType() {
    return "Vertex";
  }

  public String toString() {
    final String clsName = getRecord().getClassName();

    if (clsName.equals(CLASS_NAME))
      return StringFactory.vertexString(this);

    return StringFactory.V + "(" + clsName + ")" + StringFactory.L_BRACKET + getId() + StringFactory.R_BRACKET;
  }

  /**
   * Determines if a field is a connections or not.
   * 
   * @param iDirection
   *          Direction to check
   * @param iFieldName
   *          Field name
   * @param iClassNames
   *          Optional array of class names
   * @return The found direction if any
   */
  protected OPair<Direction, String> getConnection(final Direction iDirection, final String iFieldName, final String... iClassNames) {
    if ((iDirection == Direction.OUT || iDirection == Direction.BOTH) && iFieldName.startsWith(OrientBaseGraph.CONNECTION_OUT)) {
      if (iClassNames == null || iClassNames.length == 0)
        return new OPair<Direction, String>(Direction.OUT, getConnectionClass(Direction.OUT, iFieldName));

      // CHECK AGAINST ALL THE CLASS NAMES
      for (String clsName : iClassNames) {
        if (iFieldName.equals(CONNECTION_OUT_PREFIX + clsName))
          return new OPair<Direction, String>(Direction.OUT, clsName);
      }
    } else if ((iDirection == Direction.IN || iDirection == Direction.BOTH) && iFieldName.startsWith(OrientBaseGraph.CONNECTION_IN)) {
      if (iClassNames == null || iClassNames.length == 0)
        return new OPair<Direction, String>(Direction.IN, getConnectionClass(Direction.IN, iFieldName));

      // CHECK AGAINST ALL THE CLASS NAMES
      for (String clsName : iClassNames) {
        if (iFieldName.equals(CONNECTION_IN_PREFIX + clsName))
          return new OPair<Direction, String>(Direction.IN, clsName);
      }
    }

    // NOT FOUND
    return null;
  }

  public static String getConnectionFieldName(final Direction iDirection, final String iClassName) {
    if (iDirection == null || iDirection == Direction.BOTH)
      throw new IllegalArgumentException("Direction not valid");

    final String prefix = iDirection == Direction.OUT ? CONNECTION_OUT_PREFIX : CONNECTION_IN_PREFIX;
    if (iClassName == null || iClassName.isEmpty() || iClassName.equals(OrientEdge.CLASS_NAME))
      return prefix;

    return prefix + iClassName;
  }

  public static Object createLink(final ODocument iFromVertex, final OIdentifiable iTo, final String iFieldName) {
    final Object out;
    Object found = iFromVertex.field(iFieldName);
    if (found == null)
      // CREATE ONLY ONE LINK
      out = iTo;
    else if (found instanceof OIdentifiable) {
      if (!found.equals(iTo))
        // SAME LINK, SKIP IT
        return found;

      // DOUBLE: SCALE UP THE LINK INTO A COLLECTION
      out = new OMVRBTreeRIDSet(iFromVertex);
      ((OMVRBTreeRIDSet) out).add((OIdentifiable) found);
      ((OMVRBTreeRIDSet) out).add(iTo);
    } else if (found instanceof OMVRBTreeRIDSet) {
      // ADD THE LINK TO THE COLLECTION
      out = null;
      ((OMVRBTreeRIDSet) found).add(iTo);
    } else
      throw new IllegalStateException("Relationship content is invalid on field " + iFieldName + ". Found: " + found);

    if (out != null)
      // OVERWRITE IT
      iFromVertex.field(iFieldName, out);

    return out;
  }

  public static Direction getConnectionDirection(final String iConnectionField) {
    if (iConnectionField == null)
      throw new IllegalArgumentException("Cannot return direction of NULL connection ");

    if (iConnectionField.startsWith(CONNECTION_OUT_PREFIX))
      return Direction.OUT;
    else if (iConnectionField.startsWith(CONNECTION_IN_PREFIX))
      return Direction.IN;

    throw new IllegalArgumentException("Cannot return direction of connection " + iConnectionField);
  }

  public static String getConnectionClass(final Direction iDirection, final String iFieldName) {
    if (iDirection == Direction.OUT) {
      if (iFieldName.length() > CONNECTION_OUT_PREFIX.length())
        return iFieldName.substring(CONNECTION_OUT_PREFIX.length());
    } else if (iDirection == Direction.IN) {
      if (iFieldName.length() > CONNECTION_IN_PREFIX.length())
        return iFieldName.substring(CONNECTION_IN_PREFIX.length());
    }
    return CLASS_NAME;
  }

  public static String getInverseConnectionFieldName(final String iFieldName) {
    if (iFieldName.startsWith(CONNECTION_OUT_PREFIX)) {
      if (iFieldName.length() == CONNECTION_OUT_PREFIX.length())
        // "OUT" CASE
        return CONNECTION_IN_PREFIX;

      return CONNECTION_IN_PREFIX + iFieldName.substring(CONNECTION_OUT_PREFIX.length());

    } else if (iFieldName.startsWith(CONNECTION_IN_PREFIX)) {
      if (iFieldName.length() == CONNECTION_IN_PREFIX.length())
        // "IN" CASE
        return CONNECTION_OUT_PREFIX;

      return CONNECTION_OUT_PREFIX + iFieldName.substring(CONNECTION_IN_PREFIX.length());

    } else
      throw new IllegalArgumentException("Cannot find reverse connection name for field " + iFieldName);
  }

  public static int removeEdges(final ODocument iVertex, final String iFieldName, final OIdentifiable iVertexToRemove,
      final boolean iAlsoInverse) {
    if (iVertex == null)
      return 0;

    final Object fieldValue = iVertexToRemove != null ? iVertex.field(iFieldName) : iVertex.removeField(iFieldName);
    if (fieldValue == null)
      return 0;

    if (fieldValue instanceof OIdentifiable) {
      // SINGLE RECORD

      if (iVertexToRemove != null) {
        if (!fieldValue.equals(iVertexToRemove)) {
          // OLogManager.instance().warn(null, "[OrientVertex.removeEdges] connections %s not found in field %s", iVertexToRemove,
          // iFieldName);
          return 0;
        }
        iVertex.removeField(iFieldName);
      }

      if (iAlsoInverse)
        removeInverseEdge(iVertex, iFieldName, iVertexToRemove, fieldValue);

    } else if (fieldValue instanceof OMVRBTreeRIDSet) {
      // COLLECTION OF RECORDS: REMOVE THE ENTRY

      if (iVertexToRemove != null) {
        if (!((OMVRBTreeRIDSet) fieldValue).remove(iVertexToRemove)) {
          // SEARCH SEQUENTIALLY (SLOWER)
          for (OLazyIterator<OIdentifiable> it = ((OMVRBTreeRIDSet) fieldValue).iterator(false); it.hasNext();) {
            final ODocument curr = it.next().getRecord();

            if (iVertexToRemove.equals(curr)) {
              // FOUND AS VERTEX
              it.remove();
              if (iAlsoInverse)
                removeInverseEdge(iVertex, iFieldName, iVertexToRemove, fieldValue);
              break;

            } else if (curr.getSchemaClass().isSubClassOf(OrientEdge.CLASS_NAME)) {
              // EDGE
              if (curr.getSchemaClass().isSubClassOf(OrientEdge.CLASS_NAME)) {
                final Direction direction = getConnectionDirection(iFieldName);

                // EDGE, REMOVE THE EDGE
                if (iVertexToRemove.equals(OrientEdge.getConnection(curr, direction.opposite()))) {
                  it.remove();
                  if (iAlsoInverse)
                    removeInverseEdge(iVertex, iFieldName, iVertexToRemove, fieldValue);
                  break;
                }
              }
            }
          }
        }

        // OLogManager.instance().warn(null, "[OrientVertex.removeEdges] connections %s not found in field %s", iVertexToRemove,
        // iFieldName);
        return ((OMVRBTreeRIDSet) fieldValue).size();
      }
    }
    return 0;
  }

  private static void removeInverseEdge(final ODocument iVertex, final String iFieldName, final OIdentifiable iVertexToRemove,
      final Object iFieldValue) {
    final String inverseFieldName = getInverseConnectionFieldName(iFieldName);

    final ODocument r = ((OIdentifiable) iFieldValue).getRecord();
    if (r.getSchemaClass().isSubClassOf(CLASS_NAME)) {
      // DIRECT VERTEX
      removeEdges(r, inverseFieldName, iVertex, false);

    } else if (r.getSchemaClass().isSubClassOf(OrientEdge.CLASS_NAME)) {
      // EDGE, REMOVE THE EDGE
      final OIdentifiable otherVertex = OrientEdge.getConnection(r, getConnectionDirection(inverseFieldName));

      if (otherVertex != null) {
        if (iVertexToRemove == null || otherVertex.equals(iVertexToRemove))
          // BIDIRECTIONAL EDGE
          removeEdges((ODocument) otherVertex.getRecord(), inverseFieldName, iVertex, false);

      } else
        throw new IllegalStateException("Invalid content found in " + iFieldName + " field");
    }
  }
}