package com.tinkerpop.blueprints.impls.orient;

import java.util.Iterator;

import com.orientechnologies.common.util.OPair;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.iterator.OLazyWrapperIterator;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.tinkerpop.blueprints.Direction;

/**
 * Lazy iterator of edges.
 * 
 * @author Luca Garulli (http://www.orientechnologies.com)
 * 
 */
public class OrientEdgeIterator extends OLazyWrapperIterator<OrientEdge> {
  private final OrientVertex             vertex;
  private final OPair<Direction, String> connection;
  private final String[]                 labels;

  public OrientEdgeIterator(final OrientVertex iVertex, final Iterator<?> iterator, final OPair<Direction, String> connection,
      final String[] iLabels) {
    super(iterator);
    this.vertex = iVertex;
    this.connection = connection;
    this.labels = iLabels;
  }

  @Override
  public OrientEdge createWrapper(final Object iObject) {
    if (iObject instanceof OrientEdge)
      return (OrientEdge) iObject;

    final ODocument value = ((OIdentifiable) iObject).getRecord();

    if (value == null)
      return null;

    final OrientEdge edge;
    if (value.getSchemaClass().isSubClassOf(OrientVertex.CLASS_NAME)) {
      // DIRECT VERTEX, CREATE DUMMY EDGE
      if (connection.getKey() == Direction.OUT)
        edge = new OrientEdge(this.vertex.graph, this.vertex.getRecord(), value, connection.getValue());
      else
        edge = new OrientEdge(this.vertex.graph, value, this.vertex.getRecord(), connection.getValue());
    } else if (value.getSchemaClass().isSubClassOf(OrientEdge.CLASS_NAME)) {
      // EDGE
      edge = new OrientEdge(this.vertex.graph, value);
    } else
      throw new IllegalStateException("Invalid content found between connections:" + value);

    if (this.vertex.graph.isUseVertexFieldsForEdgeLabels() || edge.isLabeled(labels))
      return edge;

    return null;
  }

  public boolean filter(final OrientEdge iObject) {
    return this.vertex.graph.isUseVertexFieldsForEdgeLabels() || iObject.isLabeled(labels);
  }
}