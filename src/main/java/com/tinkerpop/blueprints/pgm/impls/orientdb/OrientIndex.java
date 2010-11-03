package com.tinkerpop.blueprints.pgm.impls.orientdb;

import com.orientechnologies.orient.core.db.graph.OGraphElement;
import com.orientechnologies.orient.core.db.object.OLazyObjectList;
import com.orientechnologies.orient.core.db.record.ODatabaseRecord;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.index.OIndexException;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.serialization.serializer.stream.OStreamSerializerListRID;
import com.orientechnologies.orient.core.serialization.serializer.stream.OStreamSerializerString;
import com.orientechnologies.orient.core.storage.OStorage;
import com.orientechnologies.orient.core.type.tree.OTreeMapDatabaseLazySave;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.impls.orientdb.util.OrientElementSequence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientIndex<T extends OrientElement> implements Index<T> {

    protected static final String SEPARATOR = "!=!";

    protected OrientGraph graph;
    protected OTreeMapDatabaseLazySave<String, List<ODocument>> treeMap;

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

    protected OTreeMapDatabaseLazySave<String, List<ODocument>> getRawIndex() {
        return this.treeMap;
    }

    public String getIndexName() {
        return this.indexName;
    }

    public Class<T> getIndexClass() {
        return (Class<T>) this.indexClass;
    }

    public void put(final String key, final Object value, final T element) {
        final String keyTemp = key + SEPARATOR + value;

        List<ODocument> values = treeMap.get(keyTemp);
        if (values == null)
            values = new ArrayList<ODocument>();

        int pos = values.indexOf(element.getRawElement().getDocument());
        if (pos == -1)
            values.add(element.getRawElement().getDocument());

        final boolean txBegun = graph.autoStartTransaction();

        treeMap.put(keyTemp, values);

        if (txBegun)
            graph.autoStopTransaction(TransactionalGraph.Conclusion.SUCCESS);
    }

    public Iterable<T> get(final String key, final Object value) {
        final String keyTemp = key + SEPARATOR + value;
        final List<ODocument> docList = treeMap.get(keyTemp);

        if (docList == null || docList.isEmpty())
            return new LinkedList<T>();

        final OLazyObjectList<OGraphElement> list = new OLazyObjectList<OGraphElement>(graph.getRawGraph(), docList);
        return new OrientElementSequence<T>(graph, list.iterator());
    }

    public void remove(final String key, final Object value, final T element) {
        final String keyTemp = key + SEPARATOR + value;
        final List<ODocument> values = treeMap.get(keyTemp);

        if (values != null) {
            final boolean txBegun = graph.autoStartTransaction();

            values.remove(element.getRawElement().getDocument());
            treeMap.put(keyTemp, values);

            if (txBegun)
                graph.autoStopTransaction(TransactionalGraph.Conclusion.SUCCESS);
        }
    }

    protected void clear() {
        try {
            if (null != this.treeMap) {
                final boolean txBegun = graph.autoStartTransaction();

                this.treeMap.clear();
                this.treeMap.save();

                if (txBegun)
                    graph.autoStopTransaction(TransactionalGraph.Conclusion.SUCCESS);
            }
        } catch (Exception e) {
            // throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected int removeElement(final Element element) {
        if (!getIndexClass().isAssignableFrom(element.getClass()))
            return 0;
        int removed = 0;
        for (List<ODocument> docs : getRawIndex().values()) {
            if (docs != null) {
                ODocument doc;
                for (int i = 0; i < docs.size(); ++i) {
                    doc = docs.get(i);

                    if (doc.getIdentity().equals(element.getId())) {
                        docs.remove(i);
                        ++removed;
                    }
                }
            }
        }
        return removed;
    }

    private void create(final Class<T> indexClass) {
        this.indexClass = indexClass;

        // CREATE THE MAP
        treeMap = new OTreeMapDatabaseLazySave<String, List<ODocument>>((ODatabaseRecord<?>) ((ODatabaseRecord<?>) this.graph.getRawGraph().getUnderlying()).getUnderlying(), OStorage.CLUSTER_INDEX_NAME, OStreamSerializerString.INSTANCE, OStreamSerializerListRID.INSTANCE);
        try {
            treeMap.save();
        } catch (IOException e) {
            throw new OIndexException("Unable to save index");
        }
    }

    private void load(final ODocument indexCfg) {
        // LOAD TREEMAP
        final String indexClassName = indexCfg.field(OrientGraph.FIELD_CLASSNAME);
        final ORecordId indexTreeMapRid = indexCfg.field(OrientGraph.FIELD_TREEMAP_RID);

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
        treeMap = new OTreeMapDatabaseLazySave<String, List<ODocument>>((ODatabaseRecord<?>) ((ODatabaseRecord<?>) this.graph.getRawGraph().getUnderlying()).getUnderlying(), indexTreeMapRid);
        try {
            treeMap.load();
        } catch (IOException e) {
            throw new OIndexException("Unable to load index");
        }
    }
}
