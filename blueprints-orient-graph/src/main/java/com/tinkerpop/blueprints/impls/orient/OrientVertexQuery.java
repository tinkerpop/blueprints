package com.tinkerpop.blueprints.impls.orient;

import com.tinkerpop.blueprints.util.DefaultVertexQuery;

/**
 * OrientDB implementation for vertex query.
 * 
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientVertexQuery extends DefaultVertexQuery {

  public OrientVertexQuery(final OrientVertex vertex) {
    super(vertex);
  }

  @Override
  public long count() {
    return ((OrientVertex) vertex).countEdges(direction, labels);
  }
}
