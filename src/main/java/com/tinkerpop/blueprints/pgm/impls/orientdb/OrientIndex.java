package com.tinkerpop.blueprints.pgm.impls.orientdb;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
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
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.impls.orientdb.util.OrientElementSequence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientIndex<T extends OrientElement> implements Index<T> {

    private static final String MAP_RID = "mapRid";
    private static final String SEPARATOR = "!=!";

    private OrientGraph graph;
    private OTreeMapDatabaseLazySave<String, List<ODocument>> map;
    private ODocument graphIndex;

    private final String indexName;
    private final Class<T> indexClass;

    public OrientIndex(final String indexName, final Class<T> indexClass, final OrientGraph graph) {
        this.graph = graph;

        this.indexName = indexName;
        this.indexClass = indexClass;

        // LOAD THE CONFIGURATION FROM THE DICTIONARY
        graphIndex = ((ODatabaseDocumentTx) this.graph.getRawGraph().getUnderlying()).getDictionary().get("graphIndex");

        if (graphIndex == null) {
            // CREATE THE MAP
            map = new OTreeMapDatabaseLazySave<String, List<ODocument>>((ODatabaseRecord<?>) ((ODatabaseRecord<?>) this.graph.getRawGraph().getUnderlying()).getUnderlying(), OStorage.CLUSTER_INDEX_NAME, OStreamSerializerString.INSTANCE, OStreamSerializerListRID.INSTANCE);
            try {
                map.save();
            } catch (IOException e) {
                throw new OIndexException("Unable to save index");
            }

            // CREATE THE CONFIGURATION FOR IT AND SAVE IT INTO THE DICTIONARY
            graphIndex = new ODocument((ODatabaseDocumentTx) this.graph.getRawGraph().getUnderlying());
            graphIndex.field(MAP_RID, map.getRecord().getIdentity().toString());
            ((ODatabaseDocumentTx) this.graph.getRawGraph().getUnderlying()).getDictionary().put("graphIndex", graphIndex);
        } else {
            // LOAD THE MAP
            map = new OTreeMapDatabaseLazySave<String, List<ODocument>>((ODatabaseRecord<?>) ((ODatabaseRecord<?>) this.graph.getRawGraph().getUnderlying()).getUnderlying(), new ORecordId((String) graphIndex.field(MAP_RID)));
            try {
                map.load();
            } catch (IOException e) {
                throw new OIndexException("Unable to load index");
            }
        }
    }

    protected OTreeMapDatabaseLazySave<String, List<ODocument>> getRawIndex() {
        return this.map;
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

        List<ODocument> values = map.get(keyTemp);
        if (values == null)
            values = new ArrayList<ODocument>();

        int pos = values.indexOf(elementTemp.getRawElement().getDocument());
        if (pos == -1)
            values.add(elementTemp.getRawElement().getDocument());

        map.put(keyTemp, values);
    }

    public Iterable<T> get(final String key, final Object value) {
        final String keyTemp = key + SEPARATOR + value;

        final List<ODocument> docList = map.get(keyTemp);

        if (docList == null || docList.isEmpty())
            return new LinkedList<T>();

        final OLazyObjectList<OGraphElement> list = new OLazyObjectList<OGraphElement>(graph.getRawGraph(), docList);
        return new OrientElementSequence(graph, list.iterator());
    }

    public void remove(final String key, final Object value, final T element) {

        final OrientElement elementTemp = (OrientElement) element;

        final String keyTemp = key + SEPARATOR + value;

        List<ODocument> values = map.get(keyTemp);

        if (values != null) {
            values.remove(elementTemp.getRawElement().getDocument());
            map.put(keyTemp, values);
        }
    }

    protected void clear() {
        try {
            if (null != this.map) {
                this.map.clear();
                this.map.save();
            }
            if (null != this.graphIndex) {
                this.graphIndex.clear();
                this.graphIndex.save();
            }
        } catch (Exception e) {
            //throw new RuntimeException(e.getMessage(), e);
        }
    }
}
