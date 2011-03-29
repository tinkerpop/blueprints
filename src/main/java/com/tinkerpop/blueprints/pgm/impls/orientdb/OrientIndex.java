package com.tinkerpop.blueprints.pgm.impls.orientdb;

import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.index.OIndexNotUnique;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.record.ORecord;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.tinkerpop.blueprints.pgm.*;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.impls.orientdb.util.OrientElementSequence;

import java.util.Collections;
import java.util.Map.Entry;
import java.util.Set;

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

    OrientIndex(final OrientGraph graph, final String indexName, final Class<? extends Element> indexClass, final com.tinkerpop.blueprints.pgm.Index.Type indexType) {
        this.graph = graph;
        this.indexClass = indexClass;
        create(indexName, this.indexClass, indexType);
    }

    public OrientIndex(OrientGraph orientGraph, OIndex rawIndex) {
        this.graph = orientGraph;
        this.underlying = rawIndex;
        load(rawIndex.updateConfiguration());
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

        final boolean txBegun = graph.autoStartTransaction();
        try {
            underlying.put(keyTemp, doc);

            if (txBegun)
                graph.autoStopTransaction(TransactionalGraph.Conclusion.SUCCESS);

        } catch (RuntimeException e) {
            if (txBegun)
                graph.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw e;
        } catch (Exception e) {
            if (txBegun)
                graph.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("rawtypes")
    public Iterable<T> get(final String key, final Object value) {
        final String keyTemp = key + SEPARATOR + value;
        final Set<OIdentifiable> records = underlying.get(keyTemp);

        if (records.isEmpty())
            return Collections.emptySet();

        return new OrientElementSequence(graph, records.iterator());
    }

    public long count(final String key, final Object value) {
        final String keyTemp = key + SEPARATOR + value;
        final Set<OIdentifiable> records = underlying.get(keyTemp);
        return records.size();
    }

    public void remove(final String key, final Object value, final T element) {
        final String keyTemp = key + SEPARATOR + value;

        final boolean txBegun = graph.autoStartTransaction();
        try {
            underlying.remove(keyTemp, element.getRawElement());

            if (txBegun)
                graph.autoStopTransaction(TransactionalGraph.Conclusion.SUCCESS);
        } catch (RuntimeException e) {
            if (txBegun)
                graph.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw e;
        } catch (Exception e) {
            if (txBegun)
                graph.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public String toString() {
        return StringFactory.indexString(this);
    }

    protected void removeElement(final T vertex) {
        final ORecord<?> vertexDoc = vertex.getRawElement();

        Set<OIdentifiable> rids;
        for (Entry<Object, Set<OIdentifiable>> entries : getRawIndex()) {
            rids = entries.getValue();
            if (rids != null) {
                if (rids.contains(vertexDoc))
                    underlying.remove(entries.getKey(), vertexDoc);
            }
        }
    }

    private void create(final String indexName, final Class<? extends Element> indexClass, final com.tinkerpop.blueprints.pgm.Index.Type indexType) {
        this.indexClass = indexClass;

        // CREATE THE MAP
        this.underlying = graph.getRawGraph().getMetadata().getIndexManager().createIndex(indexName, OProperty.INDEX_TYPE.NOTUNIQUE.toString(), null, null, null);
        underlying.setName(indexName);

        final String className;
        if (Vertex.class.isAssignableFrom(indexClass))
            className = VERTEX;
        else if (Edge.class.isAssignableFrom(indexClass))
            className = EDGE;
        else
            className = indexClass.getName();

        // CREATE THE CONFIGURATION FOR THE NEW INDEX
        underlying.updateConfiguration().field(CONFIG_CLASSNAME, className);
        underlying.updateConfiguration().field(CONFIG_TYPE, indexType.toString());
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
                throw new IllegalArgumentException("Index class '" + indexClassName + "' is not registered. Supported ones: Vertex, Edge and custom class that extends them");
            }

        // LOAD THE TREE-MAP
        underlying = new OIndexNotUnique().loadFromConfiguration(indexConfiguration);
    }

}
