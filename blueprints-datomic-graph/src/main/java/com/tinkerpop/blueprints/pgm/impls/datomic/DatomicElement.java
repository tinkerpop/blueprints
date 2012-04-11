package com.tinkerpop.blueprints.pgm.impls.datomic;

import clojure.lang.Keyword;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.impls.datomic.util.DatomicUtil;
import datomic.Peer;
import datomic.Util;
import java.util.*;

/**
 * @author Davy Suvee (http://datablend.be)
 */
public abstract class DatomicElement implements Element {

    protected static final String ID_EXCEPTION_MESSAGE = "Id should be of type UUID";

    protected final DatomicGraph graph;
    protected Object uuid;
    protected Object datomicId;

    protected DatomicElement(final DatomicGraph graph) {
        this.graph = graph;
        uuid = Keyword.intern(UUID.randomUUID().toString());
        datomicId = Peer.tempid(":db.part/user");
    }

    // Each graph element is assigned an UUID. (Used for retrieving the final com.tinkerpop.blueprints.pgm.impl.com.tinkerpop.blueprints.pgm.impls.datomic id once persisted)
    public Object getId() {
        return uuid;
    }

    public Object getDatomicId() {
        return datomicId;
    }


    public void setDatomicId(Object datomicId) {
        this.datomicId = datomicId;
    }

    public Set<String> getPropertyKeys() {
        Set<String> finalproperties = new HashSet<String>();
        Set properties = graph.getRawGraph().entity(datomicId).keySet();
        Iterator<Keyword> propertiesit = properties.iterator();
        while (propertiesit.hasNext()) {
            Keyword property = propertiesit.next();
            if (!DatomicUtil.isReservedKey(property.toString())) {
                finalproperties.add(DatomicUtil.getPropertyName(property));
            }
        }
        return finalproperties;
    }

    public Object getProperty(final String key) {
        if (!DatomicUtil.isReservedKey(key)) {
            Set properties = graph.getRawGraph().entity(datomicId).keySet();
            Iterator<Keyword> propertiesit = properties.iterator();
            // We need to iterate, as we don't know the exact type (although we ensured that only one attribute will have that name)
            while (propertiesit.hasNext()) {
                Keyword property = propertiesit.next();
                String propertyname = DatomicUtil.getPropertyName(property);
                if (key.equals(propertyname)) {
                    return  graph.getRawGraph().entity(datomicId).get(property);
                }
            }
            // We didn't find the value
            return null;
        }
        else {
            return graph.getRawGraph().entity(datomicId).get(key);
        }
    }

    public void setProperty(final String key, final Object value) {
        if (key.equals(StringFactory.ID) || (key.equals(StringFactory.LABEL) && this instanceof Edge))
            throw new RuntimeException(key + StringFactory.PROPERTY_EXCEPTION_MESSAGE);
        // A user-defined property
        if (!DatomicUtil.isReservedKey(key)) {
            // If the property does not exist yet, create the attribute if required and create the appropriate transaction
            if (getProperty(key) == null) {
                // We first need to create the new attribute on the fly
                DatomicUtil.createAttributeDefinition(key, value, graph);
                this.graph.addToTransaction(Util.map(":db/id", datomicId,
                                                      DatomicUtil.createKey(key, value), value));
            }
            else {
                // Value types match, just perform an update
                if (getProperty(key).getClass().equals(value.getClass())) {
                    this.graph.addToTransaction(Util.map(":db/id", datomicId,
                                                          DatomicUtil.createKey(key, value), value));
                }
                // Value types do not match. Retract original fact and add new one
                else {
                    DatomicUtil.createAttributeDefinition(key, value, graph);
                    this.graph.addToTransaction(Util.list(":db/retract", datomicId, DatomicUtil.createKey(key, value), getProperty(key)));
                    this.graph.addToTransaction(Util.map(":db/id", datomicId,
                                                          DatomicUtil.createKey(key, value), value));
                }
            }
        }
        // A com.tinkerpop.blueprints.pgm.impl.com.tinkerpop.blueprints.pgm.impls.datomic graph specific property
        else {
            this.graph.addToTransaction(Util.map(":db/id", datomicId,
                                                  key, value));
        }
        this.graph.transact();
    }

    public Object removeProperty(final String key) {
        Object oldvalue = getProperty(key);
        if (oldvalue != null) {
            if (!DatomicUtil.isReservedKey(key)) {
                this.graph.addToTransaction(Util.list(":db/retract", datomicId,
                                                       DatomicUtil.createKey(key, oldvalue), oldvalue));
            }
        }
        this.graph.transact();
        return oldvalue;
    }

    public int hashCode() {
        return this.getId().hashCode();
    }

    public boolean equals(final Object object) {
        return (null != object) && (this.getClass().equals(object.getClass()) && this.getId().equals(((Element)object).getId()));
    }

}