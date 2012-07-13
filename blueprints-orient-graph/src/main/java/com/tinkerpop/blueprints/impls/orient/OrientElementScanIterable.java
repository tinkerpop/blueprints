package com.tinkerpop.blueprints.impls.orient;

import java.util.Iterator;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.db.record.ODatabaseRecordAbstract;
import com.orientechnologies.orient.core.iterator.ORecordIteratorClass;
import com.orientechnologies.orient.core.record.ORecordInternal;
import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;

/**
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
class OrientElementScanIterable<T extends Element> implements CloseableIterable<T> {
    private final Class<T> elementClass;
    private final OrientBaseGraph graph;
    private final boolean polymorphic;

    public OrientElementScanIterable(final OrientBaseGraph graph, Class<T> elementClass, final boolean polymorphic) {
        this.graph = graph;
        this.elementClass = elementClass;
        this.polymorphic = polymorphic;
    }

    public Iterator<T> iterator() {
        final String className = elementClass.equals(Vertex.class) ? OGraphDatabase.VERTEX_CLASS_NAME : OGraphDatabase.EDGE_CLASS_NAME;
        final OGraphDatabase rawGraph = this.graph.getRawGraph();
        return new OrientElementIterator<T>(this.graph, new ORecordIteratorClass<ORecordInternal<?>>(rawGraph, (ODatabaseRecordAbstract) rawGraph.getUnderlying(), className, polymorphic));
    }

    public void close() {
    }
}