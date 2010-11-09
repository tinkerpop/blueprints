package com.tinkerpop.blueprints.pgm.impls.neo4j;


import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class Neo4jElement implements Element {

    protected final Neo4jGraph graph;
    protected PropertyContainer element;

    public Neo4jElement(final Neo4jGraph graph) {
        this.graph = graph;
    }

    public Object getProperty(final String key) {
        if (this.element.hasProperty(key))
            return this.element.getProperty(key);
        else
            return null;
    }

    public void setProperty(final String key, final Object value) {

        this.graph.autoStartTransaction();
        Object oldValue = this.getProperty(key);

        for (Neo4jAutomaticIndex autoIndex : this.graph.getAutoIndices()) {
            autoIndex.autoUpdate(key, value, oldValue, this);
        }

        this.element.setProperty(key, value);
        this.graph.autoStopTransaction(TransactionalGraph.Conclusion.SUCCESS);

    }

    public Object removeProperty(final String key) {
        try {
            this.graph.autoStartTransaction();
            Object oldValue = this.element.removeProperty(key);
            if (null != oldValue) {
                for (Neo4jAutomaticIndex autoIndex : this.graph.getAutoIndices()) {
                    autoIndex.autoRemove(key, oldValue, this);
                }
            }
            this.graph.autoStopTransaction(TransactionalGraph.Conclusion.SUCCESS);

            return oldValue;
        } catch (NotFoundException e) {
            return null;
        }
    }

    public Set<String> getPropertyKeys() {
        final Set<String> keys = new HashSet<String>();
        for (final String key : this.element.getPropertyKeys()) {
            keys.add(key);
        }
        return keys;
    }

    public int hashCode() {
        return this.getId().hashCode();
    }

    public PropertyContainer getRawElement() {
        return this.element;
    }

    public Object getId() {
        if (this.element instanceof Node) {
            return ((Node) this.element).getId();
        } else {
            return ((Relationship) this.element).getId();
        }
    }

    public boolean equals(Object object) {
        return (this.getClass().equals(object.getClass()) && this.getId().equals(((Element) object).getId()));
    }
}
