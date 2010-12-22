package com.tinkerpop.blueprints.pgm.impls.orientdb;

import com.orientechnologies.orient.core.db.graph.OGraphElement;
import com.orientechnologies.orient.core.db.object.OLazyObjectList;
import com.orientechnologies.orient.core.db.record.ODatabaseRecord;
import com.orientechnologies.orient.core.db.record.ORecordLazyList;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.index.OIndexException;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.serialization.serializer.stream.OStreamSerializerListRID;
import com.orientechnologies.orient.core.serialization.serializer.stream.OStreamSerializerString;
import com.orientechnologies.orient.core.storage.OStorage;
import com.orientechnologies.orient.core.type.tree.OMVRBTreeDatabaseLazySave;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.impls.orientdb.util.OrientElementSequence;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Map.Entry;

/**
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
@SuppressWarnings("unchecked")
public class OrientIndex<T extends OrientElement> implements Index<T> {

    protected static final String SEPARATOR = "!=!";

    protected OrientGraph graph;
    protected OMVRBTreeDatabaseLazySave<String, ORecordLazyList> treeMap;

    protected final String indexName;
    protected Class<? extends Element> indexClass;
    protected final ODocument indexCfg;

    OrientIndex(final String indexName, final Class<T> indexClass, final OrientGraph graph, final ODocument indexCfg) {
        this.graph = graph;
        this.indexName = indexName;
        this.indexCfg = indexCfg;

        if (indexClass == null) {
            load(indexCfg);
        } else {
            create(indexClass);
        }
    }

    protected OMVRBTreeDatabaseLazySave<String, ORecordLazyList> getRawIndex() {
        return this.treeMap;
    }

    public String getIndexName() {
        return this.indexName;
    }

    public Class<T> getIndexClass() {
        return (Class<T>) this.indexClass;
    }

    public Type getIndexType() {
        return Type.MANUAL;
    }

    public void put(final String key, final Object value, final T element) {

        final String keyTemp = key + SEPARATOR + value;

        ORecordLazyList values = treeMap.get(keyTemp);
        if (values == null)
            values = new ORecordLazyList((ODatabaseRecord<?>) graph.getRawGraph().getUnderlying(), ODocument.RECORD_TYPE);

        int pos = values.indexOf(element.getRawElement().getDocument());
        if (pos == -1)
            values.add(element.getRawElement().getDocument());

        final boolean txBegun = graph.autoStartTransaction();
        try {
            treeMap.put(keyTemp, values);

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
        final ORecordLazyList docList = treeMap.get(keyTemp);

        if (docList == null || docList.isEmpty())
            return new LinkedList<T>();

        final OLazyObjectList<OGraphElement> list = new OLazyObjectList<OGraphElement>(graph.getRawGraph(), null, docList);
        return new OrientElementSequence(graph, list.iterator());
    }

    public void remove(final String key, final Object value, final T element) {
        final String keyTemp = key + SEPARATOR + value;
        final ORecordLazyList values = treeMap.get(keyTemp);

        if (values != null) {
            final boolean txBegun = graph.autoStartTransaction();
            try {
                values.remove(element.getRawElement().getDocument());
                treeMap.put(keyTemp, values);

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
    }

    protected void clear() {
        try {
            if (null != this.treeMap) {
                final boolean txBegun = graph.autoStartTransaction();
                try {
                    this.treeMap.clear();
                    this.treeMap.save();

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
        } catch (Exception e) {
            // FIXME: is there a reason this exception is gobbled? (dw 2010-Dec-6)
            // throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void removeElement(final T vertex) {
        ORecordLazyList docs;
        for (Entry<String, ORecordLazyList> entries : getRawIndex().entrySet()) {
            docs = entries.getValue();

            if (docs != null) {
                ODocument doc;
                for (int i = 0; i < docs.size(); ++i) {
                    doc = (ODocument) docs.get(i);

                    if (doc.getIdentity().equals(vertex.getId())) {
                        docs.remove(i);
                        getRawIndex().put(entries.getKey(), docs);
                    }
                }
            }
        }
    }

    private void create(final Class<T> indexClass) {
        this.indexClass = indexClass;

        final ODatabaseRecord<?> db = (ODatabaseRecord<?>) ((ODatabaseRecord<?>) this.graph.getRawGraph().getUnderlying()).getUnderlying();

        // CREATE THE MAP
        treeMap = new OMVRBTreeDatabaseLazySave<String, ORecordLazyList>(db, OStorage.CLUSTER_INDEX_NAME, OStreamSerializerString.INSTANCE, OStreamSerializerListRID.INSTANCE);
        try {
            treeMap.save();
        } catch (IOException e) {
            throw new OIndexException("Unable to save index");
        }
    }

    private void load(final ODocument indexCfg) {
        // LOAD TREEMAP
        final String indexClassName = indexCfg.field(OrientGraph.FIELD_CLASSNAME);
        final ORecordId indexTreeMapRid = indexCfg.field(OrientGraph.FIELD_TREEMAP_RID, ORID.class);

        if ("Vertex".equals(indexClassName))
            this.indexClass = OrientVertex.class;
        else if ("Edge".equals(indexClassName))
            this.indexClass = OrientEdge.class;
        else
            try {
                this.indexClass = (Class<T>) Class.forName(indexClassName);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Index class '" + indexClassName + "' is not registered. Supported ones: Vertex, Edge and custom class that extends them");
            }

        // LOAD THE TREE-MAP
        treeMap = new OMVRBTreeDatabaseLazySave<String, ORecordLazyList>((ODatabaseRecord<?>) ((ODatabaseRecord<?>) this.graph.getRawGraph().getUnderlying()).getUnderlying(), indexTreeMapRid);
        treeMap.load();
    }

}
