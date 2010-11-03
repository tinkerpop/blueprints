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

    private static final String SEPARATOR = "!=!";

    private OrientGraph graph;
    private OTreeMapDatabaseLazySave<String, List<ODocument>> treeMap;

    private final String indexName;
    private final Class<T> indexClass;

    public OrientIndex(final String indexName, final Class<T> indexClass, final OrientGraph graph) {
        this.graph = graph;
        this.indexName = indexName;
        this.indexClass = indexClass;

        // CREATE THE MAP
        treeMap = new OTreeMapDatabaseLazySave<String, List<ODocument>>((ODatabaseRecord<?>) ((ODatabaseRecord<?>) this.graph.getRawGraph().getUnderlying()).getUnderlying(), OStorage.CLUSTER_INDEX_NAME, OStreamSerializerString.INSTANCE, OStreamSerializerListRID.INSTANCE);
        try {
            treeMap.save();
        } catch (IOException e) {
            throw new OIndexException("Unable to save index");
        }
    }

    /**
     * Load constructor.
     */
    public OrientIndex(final String indexName, final Class<T> indexClass, final OrientGraph graph, final ORecordId indexTreeMap) {
        this.graph = graph;
        this.indexName = indexName;
        this.indexClass = indexClass;

        // LOAD THE TREE-MAP
        treeMap = new OTreeMapDatabaseLazySave<String, List<ODocument>>((ODatabaseRecord<?>) ((ODatabaseRecord<?>) this.graph.getRawGraph().getUnderlying()).getUnderlying(), indexTreeMap);
        try {
            treeMap.load();
        } catch (IOException e) {
            throw new OIndexException("Unable to load index");
        }
    }

    protected OTreeMapDatabaseLazySave<String, List<ODocument>> getRawIndex() {
        return this.treeMap;
    }

    public String getIndexName() {
        return this.indexName;
    }

    public Class<T> getIndexClass() {
        return this.indexClass;
    }

    public void put(final String key, final Object value, final T element) {

        final OrientElement elementTemp = (OrientElement) element;

        final String keyTemp = key + SEPARATOR + value;

        List<ODocument> values = treeMap.get(keyTemp);
        if (values == null)
            values = new ArrayList<ODocument>();

        int pos = values.indexOf(elementTemp.getRawElement().getDocument());
        if (pos == -1)
            values.add(elementTemp.getRawElement().getDocument());

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
        return new OrientElementSequence(graph, list.iterator());
    }

    public void remove(final String key, final Object value, final T element) {

        final OrientElement elementTemp = (OrientElement) element;

        final String keyTemp = key + SEPARATOR + value;

        final List<ODocument> values = treeMap.get(keyTemp);

        if (values != null) {
            final boolean txBegun = graph.autoStartTransaction();

            values.remove(elementTemp.getRawElement().getDocument());
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

    public int removeElement(final Element vertex) {
        if (!getIndexClass().isAssignableFrom(vertex.getClass()))
            return 0;

        int removed = 0;

        for (List<ODocument> docs : getRawIndex().values()) {
            if (docs != null) {
                ODocument doc;
                for (int i = 0; i < docs.size(); ++i) {
                    doc = docs.get(i);

                    if (doc.getIdentity().equals(vertex.getId())) {
                        docs.remove(i);
                        ++removed;
                    }
                }
            }
        }
        return removed;
    }
}
