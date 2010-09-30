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
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.impls.orientdb.util.OrientElementSequence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientIndex implements Index {
    private static final String GRAPH_INDEX = "graphIndex";
    private static final String MAP_RID = "mapRid";
    private static final String SEPARATOR = "!=!";

    private OrientGraph graph;
    private boolean indexAll = true;
    private Set<String> indexedKeys = new HashSet<String>();
    private OTreeMapDatabaseLazySave<String, List<ODocument>> map;
    private ODocument graphIndex;

    public OrientIndex(final OrientGraph graph) {
        this.graph = graph;

        // LOAD THE CONFIGURATION FROM THE DICTIONARY
        graphIndex = ((ODatabaseDocumentTx) this.graph.getRawGraph().getUnderlying()).getDictionary().get(GRAPH_INDEX);

        if (graphIndex == null) {
            // CREATE THE MAP
            map = new OTreeMapDatabaseLazySave<String, List<ODocument>>((ODatabaseRecord<?>) ((ODatabaseRecord<?>) this.graph.getRawGraph()
                    .getUnderlying()).getUnderlying(), OStorage.CLUSTER_INDEX_NAME, OStreamSerializerString.INSTANCE,
                    OStreamSerializerListRID.INSTANCE);
            try {
                map.save();
            } catch (IOException e) {
                throw new OIndexException("Unable to save index");
            }

            // CREATE THE CONFIGURATION FOR IT AND SAVE IT INTO THE DICTIONARY
            graphIndex = new ODocument((ODatabaseDocumentTx) this.graph.getRawGraph().getUnderlying());
            graphIndex.field(MAP_RID, map.getRecord().getIdentity().toString());
            ((ODatabaseDocumentTx) this.graph.getRawGraph().getUnderlying()).getDictionary().put(GRAPH_INDEX, graphIndex);
        } else {
            // LOAD THE MAP
            map = new OTreeMapDatabaseLazySave<String, List<ODocument>>((ODatabaseRecord<?>) this.graph.getRawGraph().getUnderlying(),
                    new ORecordId((String) graphIndex.field(MAP_RID)));
            try {
                map.load();
            } catch (IOException e) {
                throw new OIndexException("Unable to load index");
            }
        }
    }

    public void put(final String key, final Object value, final Element element) {
        if (!indexAll && !indexedKeys.contains(key))
            return;

        final OrientElement tempElement = (OrientElement) element;

        final String tempKey = key + SEPARATOR + value;

        List<ODocument> values = map.get(tempKey);
        if (values == null)
            values = new ArrayList<ODocument>();

        int pos = values.indexOf(element);
        if (pos == -1)
            values.add(tempElement.getRawElement().getDocument());

        map.put(tempKey, values);
    }

    public Iterable<Element> get(final String key, final Object value) {
        final String tempKey = key + SEPARATOR + value;

        final List<ODocument> docList = map.get(tempKey);

        if (docList == null || docList.isEmpty())
            return null;

        final OLazyObjectList<OGraphElement> list = new OLazyObjectList<OGraphElement>(graph.getRawGraph(), docList);

        return new OrientElementSequence(this.graph, list.iterator());
    }

    public long count(final String iKey, final Object iValue) {
        final String key = iKey + SEPARATOR + iValue;
        final List<ODocument> docList = map.get(key);
        if (null == docList) {
            return 0;
        } else {
            return docList.size();
        }
    }

    public void remove(final String key, final Object value, final Element element) {
        if (!indexAll && !indexedKeys.contains(key))
            return;

        final OrientElement tempElement = (OrientElement) element;

        final String tempKey = key + SEPARATOR + value;

        List<ODocument> values = map.get(tempKey);

        if (values != null) {
            values.remove(tempElement.getRawElement().getDocument());
            map.put(tempKey, values);
        }
    }

    public void addIndexKey(final String key) {
        indexedKeys.add(key);
    }

    public void removeIndexKey(final String key) {
        indexedKeys.remove(key);
    }

    public void indexAll(final boolean indexAll) {
        this.indexAll = indexAll;
    }

    public boolean isIndexAll() {
        return indexAll;
    }

    public void clear() {
        this.map.clear();
    }
}
