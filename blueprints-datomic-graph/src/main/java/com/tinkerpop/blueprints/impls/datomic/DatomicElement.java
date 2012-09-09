package com.tinkerpop.blueprints.impls.datomic;

import clojure.lang.Keyword;
import com.tinkerpop.blueprints.TimeAwareElement;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import com.tinkerpop.blueprints.util.StringFactory;
import datomic.Database;
import datomic.Peer;
import datomic.Util;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import java.util.*;

/**
 * @author Davy Suvee (http://datablend.be)
 */
public abstract class DatomicElement implements TimeAwareElement {

    protected final Database database;
    protected final DatomicGraph datomicGraph;
    protected Object uuid;
    protected Object id;

    protected DatomicElement(final DatomicGraph datomicGraph, final Database database) {
        this.database = database;
        this.datomicGraph = datomicGraph;
        // UUID used to retrieve the actual datomic id later on
        uuid = Keyword.intern(UUID.randomUUID().toString());
        id = Peer.tempid(":graph");
    }

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public Object getTimeId() {
        return DatomicUtil.getActualTimeId(getDatabase(), this);
    }

    @Override
    public boolean isCurrentVersion() {
        return database == null;
    }

    @Override
    public boolean isDeleted() {
        // An element is deleted if we can no longer find any reference to it in the current version of the graph
        Collection<List<Object>> found = (Peer.q("[:find ?id " +
                                                  ":in $ ?id " +
                                                  ":where [?id _ _ ] ]", getDatabase(), id));
        return found.isEmpty();
    }

    @Override
    public Set<String> getPropertyKeys() {
        if (isDeleted()) {
            throw new IllegalArgumentException("It is not possible to get properties on a deleted element");
        }
        Set<String> finalproperties = new HashSet<String>();
        Set properties = getDatabase().entity(id).keySet();
        Iterator<Keyword> propertiesit = properties.iterator();
        while (propertiesit.hasNext()) {
            Keyword property = propertiesit.next();
            if (!DatomicUtil.isReservedKey(property.toString())) {
                finalproperties.add(DatomicUtil.getPropertyName(property));
            }
        }
        return finalproperties;
    }

    @Override
    public Object getProperty(final String key) {
        if (isDeleted()) {
            throw new IllegalArgumentException("It is not possible to get properties on a deleted element");
        }
        if (!DatomicUtil.isReservedKey(key)) {
            Set properties = getDatabase().entity(id).keySet();
            Iterator<Keyword> propertiesit = properties.iterator();
            // We need to iterate, as we don't know the exact type (although we ensured that only one attribute will have that name)
            while (propertiesit.hasNext()) {
                Keyword property = propertiesit.next();
                String propertyname = DatomicUtil.getPropertyName(property);
                if (key.equals(propertyname)) {
                    return getDatabase().entity(id).get(property);
                }
            }
            // We didn't find the value
            return null;
        }
        else {
            return getDatabase().entity(id).get(key);
        }
    }

    @Override
    public void setProperty(final String key, final Object value) {
        validate();
        if (key.equals(StringFactory.ID))
            throw ExceptionFactory.propertyKeyIdIsReserved();
        if (key.equals(StringFactory.LABEL))
            throw new IllegalArgumentException("Property key is reserved for all nodes and edges: " + StringFactory.LABEL);
        if (key.equals(StringFactory.EMPTY_STRING))
            throw ExceptionFactory.elementKeyCanNotBeEmpty();
        // A user-defined property
        if (!DatomicUtil.isReservedKey(key)) {
            // If the property does not exist yet, create the attribute if required and create the appropriate transaction
            if (getProperty(key) == null) {
                // We first need to create the new attribute on the fly
                DatomicUtil.createAttributeDefinition(key, value.getClass(), this.getClass(), datomicGraph);
                datomicGraph.addToTransaction(Util.map(":db/id", id,
                        DatomicUtil.createKey(key, value.getClass(), this.getClass()), value));
            }
            else {
                // Value types match, just perform an update
                if (getProperty(key).getClass().equals(value.getClass())) {
                    datomicGraph.addToTransaction(Util.map(":db/id", id,
                            DatomicUtil.createKey(key, value.getClass(), this.getClass()), value));
                }
                // Value types do not match. Retract original fact and add new one
                else {
                    DatomicUtil.createAttributeDefinition(key, value.getClass(), this.getClass(), datomicGraph);
                    datomicGraph.addToTransaction(Util.list(":db/retract", id,
                            DatomicUtil.createKey(key, value.getClass(), this.getClass()), getProperty(key)));
                    datomicGraph.addToTransaction(Util.map(":db/id", id,
                            DatomicUtil.createKey(key, value.getClass(), this.getClass()), value));
                }
            }
        }
        // A datomic graph specific property
        else {
            datomicGraph.addToTransaction(Util.map(":db/id", id,
                    key, value));
        }
        datomicGraph.addTransactionInfo(this);
        datomicGraph.transact();
    }

    public Interval getTimeInterval() {
        DateTime startTime = new DateTime(DatomicUtil.getTransactionDate(datomicGraph, getTimeId()));
        TimeAwareElement nextElement = this.getNextVersion();
        if (nextElement == null) {
            return new Interval(startTime, new DateTime(Long.MAX_VALUE));
        }
        else {
            DateTime stopTime = new DateTime(DatomicUtil.getTransactionDate(datomicGraph, nextElement.getTimeId()));
            return new Interval(startTime, stopTime);
        }
    }

    @Override
    public Object removeProperty(final String key) {
        validate();
        Object oldvalue = getProperty(key);
        if (oldvalue != null) {
            if (!DatomicUtil.isReservedKey(key)) {
                datomicGraph.addToTransaction(Util.list(":db/retract", id,
                                       DatomicUtil.createKey(key, oldvalue.getClass(), this.getClass()), oldvalue));
            }
        }
        datomicGraph.addTransactionInfo(this);
        datomicGraph.transact();
        return oldvalue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatomicElement that = (DatomicElement) o;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    protected Database getDatabase() {
        if (database == null) {
            return datomicGraph.getRawGraph();
        }
        return database;
    }

    private void validate() {
        if (!isCurrentVersion()) {
            throw new IllegalArgumentException("It is not possible to set a property on a non-current version of the element");
        }
        if (isDeleted()) {
            throw new IllegalArgumentException("It is not possible to set a property on a deleted element");
        }
    }

}