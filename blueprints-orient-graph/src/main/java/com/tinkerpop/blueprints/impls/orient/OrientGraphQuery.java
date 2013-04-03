package com.tinkerpop.blueprints.impls.orient;

import java.util.Collections;

import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.DefaultGraphQuery;

/**
 * OrientDB implementation for Graph query.
 * 
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientGraphQuery extends DefaultGraphQuery {

  public OrientGraphQuery(final Graph iGraph) {
    super(iGraph);
  }

  @Override
  public Iterable<Edge> edges() {
    if (limit == 0)
      return Collections.emptyList();

    if (((OrientBaseGraph) graph).isUseLightweightEdges())
      return super.edges();

    final StringBuilder text = new StringBuilder();

    // GO DIRECTLY AGAINST E CLASS AND SUB-CLASSES
    text.append("select from E where 1=1");

    for (HasContainer has : hasContainers) {
      text.append(" and ");

      text.append(has.key);
      switch (has.compare) {
      case EQUAL:
        if (has.value == null)
          text.append(" is ");
        else
          text.append(" = ");
        break;
      case GREATER_THAN:
        text.append(" > ");
        break;
      case GREATER_THAN_EQUAL:
        text.append(" >= ");
        break;
      case LESS_THAN:
        text.append(" < ");
        break;
      case LESS_THAN_EQUAL:
        text.append(" <= ");
        break;
      case NOT_EQUAL:
        if (has.value == null)
          text.append(" is not ");
        else
          text.append(" <> ");
        break;
      }

      if (has.value instanceof String)
        text.append('\'');
      text.append(has.value);
      if (has.value instanceof String)
        text.append('\'');
    }

    if (labels != null && labels.length > 0) {
      // APPEND LABELS
      text.append(" and label in [");
      for (int i = 0; i < labels.length; ++i) {
        if (i > 0)
          text.append(',');
        text.append('\'');
        text.append(labels[i]);
        text.append('\'');
      }
      text.append("]");
    }

    final OSQLSynchQuery<OIdentifiable> query = new OSQLSynchQuery<OIdentifiable>(text.toString());

    if (limit > 0 && limit < Long.MAX_VALUE)
      query.setLimit((int) limit);

    return new OrientElementIterable<Edge>(((OrientBaseGraph) graph), ((OrientBaseGraph) graph).getRawGraph().query(query));
  }

  @Override
  public Iterable<Vertex> vertices() {
    return super.vertices();
  }
}
