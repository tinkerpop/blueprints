package com.tinkerpop.blueprints.pgm.impls.neo4j;


import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
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
    protected PropertyContainer rawElement;

    public Neo4jElement(final Neo4jGraph graph) {
        this.graph = graph;
    }

    public Object getProperty(final String key) {
        if (this.rawElement.hasProperty(key))
            return this.rawElement.getProperty(key);
        else
            return null;
    }

    public void setProperty(final String key, final Object value) {
        if (key.equals(StringFactory.ID) || (key.equals(StringFactory.LABEL) && this instanceof Edge))
            throw new RuntimeException(key + StringFactory.PROPERTY_EXCEPTION_MESSAGE);

        try {
            this.graph.autoStartTransaction();
            Object oldValue = this.getProperty(key);

            for (Neo4jAutomaticIndex autoIndex : this.graph.getAutoIndices(this.getClass())) {
                autoIndex.autoUpdate(key, value, oldValue, this);
            }

            this.rawElement.setProperty(key, value);
            this.graph.autoStopTransaction(TransactionalGraph.Conclusion.SUCCESS);
        } catch (RuntimeException e) {
            this.graph.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw e;
        } catch (Exception e) {
            this.graph.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Object removeProperty(final String key) {
        try {
            this.graph.autoStartTransaction();
            Object oldValue = this.rawElement.removeProperty(key);
            if (null != oldValue) {
                for (Neo4jAutomaticIndex autoIndex : this.graph.getAutoIndices(this.getClass())) {
                    autoIndex.autoRemove(key, oldValue, this);
                }
            }
            this.graph.autoStopTransaction(TransactionalGraph.Conclusion.SUCCESS);

            return oldValue;
        } catch (NotFoundException e) {
            this.graph.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            return null;
        } catch (RuntimeException e) {
            this.graph.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw e;
        } catch (Exception e) {
            this.graph.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Set<String> getPropertyKeys() {
        final Set<String> keys = new HashSet<String>();
        for (final String key : this.rawElement.getPropertyKeys()) {
            keys.add(key);
        }
        return keys;
    }

    public int hashCode() {
        return this.getId().hashCode();
    }

    public PropertyContainer getRawElement() {
        return this.rawElement;
    }

    public Object getId() {
        if (this.rawElement instanceof Node) {
            return ((Node) this.rawElement).getId();
        } else {
            return ((Relationship) this.rawElement).getId();
        }
    }

    public boolean equals(final Object object) {
        return (null != object) && (this.getClass().equals(object.getClass()) && this.getId().equals(((Element) object).getId()));
    }
}
