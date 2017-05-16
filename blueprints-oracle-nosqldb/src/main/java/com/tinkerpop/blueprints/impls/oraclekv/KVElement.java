package com.tinkerpop.blueprints.impls.oraclekv;


import oracle.kv.Depth;
import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.Key;
import oracle.kv.Value;
import oracle.kv.ValueVersion;
import oracle.kv.KeyRange;
import com.tinkerpop.blueprints.impls.oraclekv.*;
import com.tinkerpop.blueprints.impls.oraclekv.util.KVUtil;
import static com.tinkerpop.blueprints.impls.oraclekv.util.KVUtil.*;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import com.tinkerpop.blueprints.util.StringFactory;
import java.util.*;

/**
 * @author Dan McClary
 */
public class KVElement implements Element {

    protected static final String ID_EXCEPTION_MESSAGE = "Id should be of type UUID";

    protected final KVGraph graph;
    protected Object id;

    protected KVElement(final KVGraph graph) {
        this.graph = graph;
        id = UUID.randomUUID();
    }

    public Object getId() {
        return id;
    }

    public Set<String> getPropertyKeys() {
        Key elementKey = majorKeyFromString(this.id.toString());
        SortedSet<Key> propertyKeys = this.graph.getRawGraph().multiGetKeys(elementKey, null, Depth.CHILDREN_ONLY);

        HashSet<String> finalproperties = new HashSet<String>();
        for (Key k : propertyKeys)
        {
            for (String p : k.getMinorPath())
            {
                finalproperties.add(p);
            }
        }
        finalproperties.remove("ID");
        finalproperties.remove("OUT");
        finalproperties.remove("IN");
        finalproperties.remove("LABEL");
        return finalproperties;
    }

    public Object getProperty(final String key) {
        Object element = null;
        if (!key.equals("")) {
            /* if this is a real property
            1) get the lookup key */
            Key elementKey = keyFromString(this.id.toString()+"/"+key);
            element = getValue(graph.getRawGraph(), elementKey);
        }
        return element;
    }

    public void setProperty(final String key, final Object value) {
        if (key.equals(StringFactory.ID))
            throw ExceptionFactory.propertyKeyIdIsReserved();
        if (key.equals(StringFactory.LABEL))
            throw new IllegalArgumentException("Property key is reserved for all nodes and edges: " + StringFactory.LABEL);
        if (key.equals(StringFactory.EMPTY_STRING))
            throw ExceptionFactory.elementKeyCanNotBeEmpty();
        
        Key elementKey = keyFromString(this.id.toString()+"/"+key);
        putValue(graph.getRawGraph(), elementKey, value);
    }

    public Object removeProperty(final String key) {
        Key elementKey = keyFromString(this.id.toString()+"/"+key);
        Object oldvalue = this.getProperty(key);
        
        graph.getRawGraph().delete(elementKey);
        return oldvalue;
    }

    public int hashCode() {
        return this.getId().hashCode();
    }

    public boolean equals(final Object object) {
        return (null != object) && (this.getClass().equals(object.getClass()) && this.getId().equals(((Element)object).getId()));
    }

}