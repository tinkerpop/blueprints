package com.tinkerpop.blueprints.impls.orient;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.db.record.ODatabaseRecordAbstract;
import com.orientechnologies.orient.core.iterator.ORecordIteratorClass;
import com.orientechnologies.orient.core.record.ORecordInternal;
import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;

import java.util.Iterator;

/**
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientElementScanIterable<T extends Element> implements CloseableIterable<T> {
    private final Class<T> elementClass;
    private final OrientGraph graph;

    public OrientElementScanIterable(final OrientGraph graph, Class<T> elementClass) {
        this.graph = graph;
        this.elementClass = elementClass;
    }

    public Iterator<T> iterator() {
        if (elementClass.equals(Vertex.class)) {
            return new OrientElementIterator<T>(this.graph, new ORecordIteratorClass<ORecordInternal<?>>(this.graph.getRawGraph(), (ODatabaseRecordAbstract) this.graph.getRawGraph().getUnderlying(), OGraphDatabase.VERTEX_CLASS_NAME, true));
        } else {
            return new OrientElementIterator<T>(this.graph, new ORecordIteratorClass<ORecordInternal<?>>(this.graph.getRawGraph(), (ODatabaseRecordAbstract) this.graph.getRawGraph().getUnderlying(), OGraphDatabase.EDGE_CLASS_NAME, true));
        }
    }

    public void close() {

    }
}