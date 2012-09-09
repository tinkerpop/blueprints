package com.tinkerpop.blueprints.impls.datomic;

import clojure.lang.Keyword;
import com.tinkerpop.blueprints.*;
import datomic.Database;
import datomic.Peer;
import java.util.*;

/**
 * @author Davy Suvee (http://datablend.be)
 * Helper class to retrieve elements. If an Datomic index exists for a particular attribute, it will be used under the covers.
 */
public class DatomicIndex<T extends Element> implements Index<T> {

    private DatomicGraph graph = null;
    private Class<T> clazz = null;
    private String name = null;
    private Set<String> indexKeys = null;
    private Database database;

    public DatomicIndex(final String name, final DatomicGraph g, final Database database, final Class<T> clazz) {
        this.name = name;
        this.graph = g;
        this.clazz = clazz;
        this.database = database;
    }

    public DatomicIndex(final String name, final DatomicGraph g, final Database database, final Class<T> clazz, Set<String> indexKeys) {
        this.name = name;
        this.graph = g;
        this.clazz = clazz;
        this.indexKeys = indexKeys;
        this.database = database;
    }

    public String getIndexName() {
        return name;
    }

    public Class<T> getIndexClass() {
        return clazz;
    }

    public Database getDatabase() {
        if (database == null) {
            return graph.getRawGraph();
        }
        return database;
    }


    public void put(final String key, final Object value, final T element) {
        throw new UnsupportedOperationException();
    }

    public CloseableIterable<T> get(final String key, final Object value) {
        boolean matched = ((indexKeys == null) || ((indexKeys != null) && indexKeys.contains(key)));
        Keyword attribute = null;
        if ((this.getIndexClass().isAssignableFrom(DatomicEdge.class)) && ("label".equals(key))) {
            attribute = Keyword.intern("graph.edge/label");
        }
        else {
            attribute = DatomicUtil.createKey(key, value.getClass(), clazz);
        }
        if (matched && DatomicUtil.existingAttributeDefinition(attribute, graph)) {
            if (this.getIndexClass().isAssignableFrom(DatomicVertex.class)) {
                return new DatomicIterable(getElements(attribute, value, Keyword.intern("graph.element.type/vertex"), getDatabase()), graph, database, Vertex.class);
            }
            if (this.getIndexClass().isAssignableFrom(DatomicEdge.class)) {
                return new DatomicIterable(getElements(attribute, value, Keyword.intern("graph.element.type/edge"), getDatabase()), graph, database, Edge.class);
            }
            throw new RuntimeException(DatomicGraph.DATOMIC_ERROR_EXCEPTION_MESSAGE);
        }
        else {
            if (this.getIndexClass().isAssignableFrom(DatomicVertex.class)) {
                return new DatomicIterable(new ArrayList<List<Object>>(), graph, database, Vertex.class);
            }
            if (this.getIndexClass().isAssignableFrom(DatomicEdge.class)) {
                return new DatomicIterable(new ArrayList<List<Object>>(), graph, database, Edge.class);
            }
            throw new RuntimeException(DatomicGraph.DATOMIC_ERROR_EXCEPTION_MESSAGE);
        }
    }

    @Override
    public CloseableIterable<T> query(String key, Object query) {
        throw new UnsupportedOperationException();
    }

    public long count(final String key, final Object value) {
        boolean matched = ((indexKeys == null) || ((indexKeys != null) && indexKeys.contains(key)));
        Keyword attribute = null;
        if ((this.getIndexClass().isAssignableFrom(DatomicEdge.class)) && ("label".equals(key))) {
            attribute = Keyword.intern("graph.edge/label");
        }
        else {
            attribute = DatomicUtil.createKey(key, value.getClass(), clazz);
        }
        if (matched && DatomicUtil.existingAttributeDefinition(attribute, graph)) {
            if (this.getIndexClass().isAssignableFrom(DatomicVertex.class)) {
                return getElements(attribute, value, Keyword.intern("graph.element.type/vertex"), getDatabase()).size();
            }
            if (this.getIndexClass().isAssignableFrom(DatomicEdge.class)) {
                return getElements(attribute, value, Keyword.intern("graph.element.type/edge"), getDatabase()).size();
            }
            throw new RuntimeException(DatomicGraph.DATOMIC_ERROR_EXCEPTION_MESSAGE);
        }
        else {
            return 0;
        }
    }

    public void remove(final String key, final Object value, final T element) {
        throw new UnsupportedOperationException();
    }

    private Collection<List<Object>> getElements(Keyword attribute, Object value, Keyword type, Database database) {
        return Peer.q("[:find ?element " +
                       ":in $ ?attribute ?value ?type " +
                       ":where [?element :graph.element/type ?type] " +
                              "[?element ?attribute ?value] ]", database, attribute, value, type);
    }

}
