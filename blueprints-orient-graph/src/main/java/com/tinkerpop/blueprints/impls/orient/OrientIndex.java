package com.tinkerpop.blueprints.impls.orient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.index.OIndexTxAware;
import com.orientechnologies.orient.core.index.OIndexTxAwareMultiValue;
import com.orientechnologies.orient.core.index.OSimpleKeyIndexDefinition;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.ORecord;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.tx.OTransactionIndexChanges;
import com.orientechnologies.orient.core.tx.OTransactionIndexChanges.OPERATION;
import com.orientechnologies.orient.core.tx.OTransactionIndexChangesPerKey;
import com.orientechnologies.orient.core.tx.OTransactionIndexChangesPerKey.OTransactionIndexEntry;
import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.StringFactory;
import com.tinkerpop.blueprints.util.WrappingCloseableIterable;

/**
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
@SuppressWarnings("unchecked")
public class OrientIndex<T extends OrientElement> implements Index<T> {
  protected static final String      VERTEX           = "Vertex";
  protected static final String      EDGE             = "Edge";
  protected static final String      CONFIG_CLASSNAME = "blueprintsIndexClass";

  protected static final String      SEPARATOR        = "!=!";

  protected OrientBaseGraph          graph;
  protected OIndex<?>                underlying;

  protected Class<? extends Element> indexClass;

  protected OrientIndex(final OrientBaseGraph graph, final String indexName, final Class<? extends Element> indexClass,
      final OType iType) {
    this.graph = graph;
    this.indexClass = indexClass;
    create(indexName, this.indexClass, iType);
  }

  protected OrientIndex(final OrientBaseGraph orientGraph, final OIndex<?> rawIndex) {
    this.graph = orientGraph;
    this.underlying = rawIndex instanceof OIndexTxAwareMultiValue ? rawIndex : new OIndexTxAwareMultiValue(
        orientGraph.getRawGraph(), (OIndex<Collection<OIdentifiable>>) rawIndex);
    load(rawIndex.getConfiguration());
  }

  public OIndex<?> getRawIndex() {
    return this.underlying;
  }

  public String getIndexName() {
    return underlying.getName();
  }

  public Class<T> getIndexClass() {
    return (Class<T>) this.indexClass;
  }

  public void put(final String key, final Object value, final T element) {
    final String keyTemp = key + SEPARATOR + value;

    final ODocument doc = element.getRecord();
    if (!doc.getIdentity().isValid())
      doc.save();

    graph.autoStartTransaction();
    underlying.put(keyTemp, doc);
  }

  @SuppressWarnings("rawtypes")
  public CloseableIterable<T> get(final String key, final Object iValue) {
    final String keyTemp = key + SEPARATOR + iValue;
    Collection<OIdentifiable> records = null;

    // PATCH FOR 1.1.0. REMOVE IT WITH > 1.2.x
    if (underlying instanceof OIndexTxAware<?>) {
      try {
        records = (Collection<OIdentifiable>) underlying.get(keyTemp);
      } catch (NullPointerException e) {
        records = new ArrayList<OIdentifiable>();
      }

      final OTransactionIndexChanges indexChanges = graph.getRawGraph().getTransaction().getIndexChanges(underlying.getName());
      if (indexChanges != null) {
        if (indexChanges.containsChangesPerKey(keyTemp)) {
          final OTransactionIndexChangesPerKey value = indexChanges.getChangesPerKey(keyTemp);
          if (value != null) {
            for (final OTransactionIndexEntry entry : value.entries) {
              if (entry.operation == OPERATION.REMOVE) {
                if (entry.value == null) {
                  // REMOVE THE ENTIRE KEY, SO RESULT SET IS EMPTY
                  records.clear();
                  break;
                } else
                  // REMOVE ONLY THIS RID
                  records.remove(entry.value);
              } else if (entry.operation == OPERATION.PUT) {
                // ADD ALSO THIS RID
                records.add(entry.value);
              }
            }
          }
        }

        if (indexChanges.containsChangesCrossKey()) {
          final OTransactionIndexChangesPerKey value = indexChanges.getChangesCrossKey();
          if (value != null) {
            for (final OTransactionIndexEntry entry : value.entries) {
              if (entry.operation == OPERATION.REMOVE) {
                if (entry.value == null) {
                  // REMOVE THE ENTIRE KEY, SO RESULT SET IS EMPTY
                  records.clear();
                  break;
                } else
                  // REMOVE ONLY THIS RID
                  records.remove(entry.value);
              } else if (entry.operation == OPERATION.PUT) {
                // ADD ALSO THIS RID
                records.add(entry.value);
              }
            }
          }
        }
      }

      // FILTER THE RESULT SET REMOVING NULL VALUES
      final Iterator<OIdentifiable> it = records.iterator();
      while (it.hasNext())
        if (it.next() == null)
          it.remove();

    } else
      records = (Collection<OIdentifiable>) underlying.get(keyTemp);

    if (records == null || records.isEmpty())
      return new WrappingCloseableIterable(Collections.emptySet());

    return new OrientElementIterable<T>(graph, records);
  }

  public CloseableIterable<T> query(final String key, final Object query) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  public long count(final String key, final Object value) {
    final String keyTemp = key + SEPARATOR + value;
    final Collection<OIdentifiable> records = (Collection<OIdentifiable>) underlying.get(keyTemp);
    return records.size();
  }

  public void remove(final String key, final Object value, final T element) {
    final String keyTemp = key + SEPARATOR + value;
    graph.autoStartTransaction();
    try {
      underlying.remove(keyTemp, element.getRecord());
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  protected void putBasic(final String key, final T element) {
    underlying.put(key, element.getRecord());
  }

  public String toString() {
    return StringFactory.indexString(this);
  }

  protected void removeElement(final T vertex) {
    graph.autoStartTransaction();
    final ORecord<?> vertexDoc = vertex.getRecord();
    underlying.remove(vertexDoc);
  }

  private void create(final String indexName, final Class<? extends Element> indexClass, OType iKeyType) {
    this.indexClass = indexClass;

    if (iKeyType == null)
      iKeyType = OType.STRING;

    // CREATE THE MAP
    this.underlying = new OIndexTxAwareMultiValue(graph.getRawGraph(), (OIndex<Collection<OIdentifiable>>) graph.getRawGraph()
        .getMetadata().getIndexManager()
        .createIndex(indexName, OClass.INDEX_TYPE.NOTUNIQUE.toString(), new OSimpleKeyIndexDefinition(iKeyType), null, null));

    final String className;
    if (Vertex.class.isAssignableFrom(indexClass))
      className = VERTEX;
    else if (Edge.class.isAssignableFrom(indexClass))
      className = EDGE;
    else
      className = indexClass.getName();

    // CREATE THE CONFIGURATION FOR THE NEW INDEX
    underlying.getConfiguration().field(CONFIG_CLASSNAME, className);
  }

  private void load(final ODocument indexConfiguration) {
    // LOAD TREEMAP
    final String indexClassName = indexConfiguration.field(CONFIG_CLASSNAME);

    if (VERTEX.equals(indexClassName))
      this.indexClass = OrientVertex.class;
    else if (EDGE.equals(indexClassName))
      this.indexClass = OrientEdge.class;
    else
      try {
        this.indexClass = (Class<T>) Class.forName(indexClassName);
      } catch (ClassNotFoundException e) {
        throw new IllegalArgumentException("Index class '" + indexClassName
            + "' is not registered. Supported ones: Vertex, Edge and custom class that extends them");
      }
  }

  public void close() {
    if (underlying != null) {
      underlying.lazySave();
      underlying = null;
    }
    graph = null;
  }

}