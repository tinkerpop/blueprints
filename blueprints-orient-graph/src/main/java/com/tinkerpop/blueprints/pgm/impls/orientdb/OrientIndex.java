package com.tinkerpop.blueprints.pgm.impls.orientdb;

import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.index.OIndexTxAwareMultiValue;
import com.orientechnologies.orient.core.index.OSimpleKeyIndexDefinition;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.ORecord;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.tinkerpop.blueprints.pgm.CloseableSequence;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.impls.WrappingCloseableSequence;
import com.tinkerpop.blueprints.pgm.impls.orientdb.util.OrientElementSequence;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
@SuppressWarnings("unchecked")
public class OrientIndex<T extends OrientElement> implements Index<T> {
    private static final String VERTEX = "Vertex";
    private static final String EDGE = "Edge";
    protected static final String CONFIG_TYPE = "blueprintsIndexType";
    protected static final String CONFIG_CLASSNAME = "blueprintsIndexClass";

    protected static final String SEPARATOR = "!=!";

    protected OrientGraph graph;
    protected OIndex underlying;

    protected Class<? extends Element> indexClass;

    OrientIndex(final OrientGraph graph, final String indexName, final Class<? extends Element> indexClass,
                final com.tinkerpop.blueprints.pgm.Index.Type indexType, final OType iType) {
        this.graph = graph;
        this.indexClass = indexClass;
        create(indexName, this.indexClass, indexType, iType);
    }

    public OrientIndex(OrientGraph orientGraph, OIndex rawIndex) {
        this.graph = orientGraph;
        this.underlying = new OIndexTxAwareMultiValue(orientGraph.getRawGraph(), rawIndex);
        load(rawIndex.getConfiguration());
    }

    public OIndex getRawIndex() {
        return this.underlying;
    }

    public String getIndexName() {
        return underlying.getName();
    }

    public Class<T> getIndexClass() {
        return (Class<T>) this.indexClass;
    }

    public Type getIndexType() {
        return Type.MANUAL;
    }

    public void put(final String key, final Object value, final T element) {
        final String keyTemp = key + SEPARATOR + value;

        final ODocument doc = element.getRawElement();
        if (!doc.getIdentity().isValid())
            doc.save();

        graph.autoStartTransaction();
        try {
            underlying.put(keyTemp, doc);
            graph.autoStopTransaction(TransactionalGraph.Conclusion.SUCCESS);
        } catch (RuntimeException e) {
            graph.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw e;
        } catch (Exception e) {
            graph.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("rawtypes")
    public CloseableSequence<T> get(final String key, final Object value) {
        final String keyTemp = key + SEPARATOR + value;
        final Collection<OIdentifiable> records = (Collection<OIdentifiable>) underlying.get(keyTemp);

        if (records.isEmpty())
            return new WrappingCloseableSequence(Collections.emptySet());

        return new OrientElementSequence<T>(graph, records.iterator());
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
            underlying.remove(keyTemp, element.getRawElement());
            graph.autoStopTransaction(TransactionalGraph.Conclusion.SUCCESS);
        } catch (RuntimeException e) {
            graph.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw e;
        } catch (Exception e) {
            graph.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected void removeBasic(final String key, final T element) {
        underlying.remove(key, element.getRawElement());
    }

    protected void putBasic(final String key, final T element) {
        underlying.put(key, element.getRawElement());
    }

    public String toString() {
        return StringFactory.indexString(this);
    }

    protected void removeElement(final T vertex) {
        graph.autoStartTransaction();
        final ORecord<?> vertexDoc = vertex.getRawElement();
        underlying.remove(vertexDoc);
        graph.autoStopTransaction(TransactionalGraph.Conclusion.SUCCESS);
    }

    private void create(final String indexName, final Class<? extends Element> indexClass,
                        final com.tinkerpop.blueprints.pgm.Index.Type indexType, OType iKeyType) {
        this.indexClass = indexClass;

        if (iKeyType == null)
            iKeyType = OType.STRING;

        // CREATE THE MAP
        this.underlying = new OIndexTxAwareMultiValue(graph.getRawGraph(), (OIndex<Collection<OIdentifiable>>) graph.getRawGraph().getMetadata().getIndexManager().createIndex(indexName, OClass.INDEX_TYPE.NOTUNIQUE.toString(), new OSimpleKeyIndexDefinition(iKeyType), null, null));

        final String className;
        if (Vertex.class.isAssignableFrom(indexClass))
            className = VERTEX;
        else if (Edge.class.isAssignableFrom(indexClass))
            className = EDGE;
        else
            className = indexClass.getName();

        // CREATE THE CONFIGURATION FOR THE NEW INDEX
        underlying.getConfiguration().field(CONFIG_CLASSNAME, className);
        underlying.getConfiguration().field(CONFIG_TYPE, indexType.toString());
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
        underlying = null;
        graph = null;
    }

}
