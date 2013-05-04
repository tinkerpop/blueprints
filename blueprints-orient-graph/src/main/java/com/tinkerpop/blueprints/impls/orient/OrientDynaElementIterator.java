package com.tinkerpop.blueprints.impls.orient;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
class OrientDynaElementIterator implements Iterator<Object> {

  private final Iterator<?>     itty;
  private final OrientBaseGraph graph;

  public OrientDynaElementIterator(final OrientBaseGraph graph, final Iterator<?> itty) {
    this.itty = itty;
    this.graph = graph;
  }

  public boolean hasNext() {
    return this.itty.hasNext();
  }

  public Object next() {
    OrientElement currentElement = null;

    if (!hasNext())
      throw new NoSuchElementException();

    Object current = itty.next();

    if (null == current)
      throw new NoSuchElementException();

    if (current instanceof OIdentifiable)
      current = ((OIdentifiable) current).getRecord();

    if (current instanceof ODocument) {
      final ODocument currentDocument = (ODocument) current;

      if (currentDocument.getInternalStatus() == ODocument.STATUS.NOT_LOADED)
        currentDocument.load();

      if (currentDocument.getSchemaClass() == null)
        return currentDocument;

      if (currentDocument.getSchemaClass().isSubClassOf(graph.getVertexBaseType()))
        currentElement = new OrientVertex(graph, currentDocument);
      else if (currentDocument.getSchemaClass().isSubClassOf(graph.getEdgeBaseType()))
        currentElement = new OrientEdge(graph, currentDocument);
      else
        return currentDocument;
    }

    return currentElement;
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }
}