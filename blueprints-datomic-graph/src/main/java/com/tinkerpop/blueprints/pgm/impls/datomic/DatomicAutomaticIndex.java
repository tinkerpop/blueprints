package com.tinkerpop.blueprints.pgm.impls.datomic;

import clojure.lang.Keyword;
import com.tinkerpop.blueprints.pgm.*;
import com.tinkerpop.blueprints.pgm.impls.datomic.util.DatomicUtil;
import datomic.Peer;
import java.util.*;

/**
* @author Davy Suvee (http://datablend.be)
*/
public class DatomicAutomaticIndex<T extends Element> implements AutomaticIndex<T> {

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

    public Type getIndexType() {
        return Type.AUTOMATIC;
    }

    public void put(final String key, final Object value, final T element) {
        throw new UnsupportedOperationException();
    }

    public CloseableSequence<T> get(final String key, final Object value) {
        boolean matched = ((indexKeys == null) || ((indexKeys != null) && indexKeys.contains(key)));
        Keyword attribute = null;
        if ((this.getIndexClass().isAssignableFrom(DatomicEdge.class)) && (AutomaticIndex.LABEL.equals(key))) {
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

    public long count(final String key, final Object value) {
       boolean matched = ((indexKeys == null) || ((indexKeys != null) && indexKeys.contains(key)));
       Keyword attribute = null;
        if ((this.getIndexClass().isAssignableFrom(DatomicEdge.class)) && (AutomaticIndex.LABEL.equals(key))) {
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

    public Set<String> getAutoIndexKeys() {
        if (this.indexKeys == null) {
            Set<String> keys = new HashSet<String>();
            Iterator<List<Object>> attributesit = null;
            if (this.getIndexClass().isAssignableFrom(DatomicVertex.class)) {
                attributesit = getAttributes(Keyword.intern("graph.element.type/vertex")).iterator();
            }
            if (this.getIndexClass().isAssignableFrom(DatomicEdge.class)) {
                attributesit = getAttributes(Keyword.intern("graph.element.type/edge")).iterator();
            }
            if (attributesit != null) {
                while (attributesit.hasNext()) {
                    Keyword attribute = (Keyword)attributesit.next().get(0);
                    if (attribute.toString().equals(":graph.edge/label")) {
                        keys.add(AutomaticIndex.LABEL);
                    }
                    else {
                        if (!DatomicUtil.isReservedKey(attribute.toString())) {
                            keys.add(DatomicUtil.getPropertyName(attribute));
                        }
                    }
                }
            }
            return keys;
        }
        else {
            return indexKeys;
        }
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
