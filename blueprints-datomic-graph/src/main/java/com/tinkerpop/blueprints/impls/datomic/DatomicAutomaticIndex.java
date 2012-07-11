package com.tinkerpop.blueprints.impls.datomic;

import clojure.lang.Keyword;
import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.impls.datomic.util.DatomicUtil;
import datomic.Peer;
import java.util.*;

/**
 * @author Davy Suvee (http://datablend.be)
 */
public class DatomicAutomaticIndex<T extends Element> implements Index<T> {

    private DatomicGraph graph = null;
    private Class<T> clazz = null;
    private String name = null;
    private Set<String> indexKeys = null;

    public DatomicAutomaticIndex(final String name, final DatomicGraph g, final Class<T> clazz) {
        this.name = name;
        this.graph = g;
        this.clazz = clazz;
    }

    public DatomicAutomaticIndex(final String name, final DatomicGraph g, final Class<T> clazz, Set<String> indexKeys) {
        this.name = name;
        this.graph = g;
        this.clazz = clazz;
        this.indexKeys = indexKeys;
    }

    public String getIndexName() {
        return name;
    }

    public Class<T> getIndexClass() {
        return clazz;
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
            attribute = DatomicUtil.createKey(key, value);
        }
        if (matched && DatomicUtil.existingAttributeDefinition(attribute, graph)) {
            if (this.getIndexClass().isAssignableFrom(DatomicVertex.class)) {
                return DatomicUtil.getVertexSequence(getElements(attribute, value, Keyword.intern("graph.element.type/vertex")).iterator(), graph);
            }
            if (this.getIndexClass().isAssignableFrom(DatomicEdge.class)) {
                return DatomicUtil.getEdgeSequence(getElements(attribute, value, Keyword.intern("graph.element.type/edge")).iterator(), graph);
            }
            throw new RuntimeException(DatomicGraph.DATOMIC_ERROR_EXCEPTION_MESSAGE);
        }
        else {
            if (this.getIndexClass().isAssignableFrom(DatomicVertex.class)) {
                return DatomicUtil.getVertexSequence(new ArrayList<List<Object>>().iterator(), graph);
            }
            if (this.getIndexClass().isAssignableFrom(DatomicEdge.class)) {
                return DatomicUtil.getEdgeSequence(new ArrayList<List<Object>>().iterator(), graph);
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
            attribute = DatomicUtil.createKey(key, value);
        }
        if (matched && DatomicUtil.existingAttributeDefinition(attribute, graph)) {
            if (this.getIndexClass().isAssignableFrom(DatomicVertex.class)) {
                return getElements(attribute, value, Keyword.intern("graph.element.type/vertex")).size();
            }
            if (this.getIndexClass().isAssignableFrom(DatomicEdge.class)) {
                return getElements(attribute, value, Keyword.intern("graph.element.type/edge")).size();
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

    private Collection<List<Object>> getElements(Keyword attribute, Object value, Keyword type) {
        return Peer.q("[:find ?element " +
                ":in $ ?attribute ?value ?type " +
                ":where [?element :graph.element/type ?type] " +
                "[?element ?attribute ?value] ]", graph.getRawGraph(), attribute, value, type);
    }

    private Collection<List<Object>> getAttributes(Keyword type) {
        return Peer.q("[:find ?key " +
                ":in $ ?type " +
                ":where [?element :graph.element/type ?type] " +
                "[?element ?attribute _] " +
                "[?attribute :db/ident ?key] ]", graph.getRawGraph(), type);
    }

}
