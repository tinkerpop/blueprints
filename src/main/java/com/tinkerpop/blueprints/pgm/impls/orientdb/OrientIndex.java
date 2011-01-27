package com.tinkerpop.blueprints.pgm.impls.orientdb;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import com.orientechnologies.orient.core.db.graph.OGraphElement;
import com.orientechnologies.orient.core.db.object.OLazyObjectList;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.index.OIndexNotUnique;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.record.ORecord;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.orientdb.util.OrientElementSequence;

/**
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
@SuppressWarnings("unchecked")
public class OrientIndex<T extends OrientElement> implements Index<T> {
	protected static final String				CONFIG_TYPE				= "blueprintsIndexType";
	protected static final String				CONFIG_CLASSNAME	= "blueprintsIndexClass";

	protected static final String				SEPARATOR					= "!=!";

	protected OrientGraph								graph;
	protected OIndex										underlying;

	protected Class<? extends Element>	indexClass;

	OrientIndex(final OrientGraph graph, final String indexName, final Class<? extends Element> iIndexClass,
			final com.tinkerpop.blueprints.pgm.Index.Type type) {
		this.graph = graph;
		this.indexClass = iIndexClass;
		create(indexName, indexClass, type);
	}

	public OrientIndex(OrientGraph orientGraph, OIndex iIndex) {
		this.graph = orientGraph;
		this.underlying = iIndex;
		load(iIndex.getConfiguration());
	}

	protected OIndex getRawIndex() {
		return this.underlying;
	}

	public String getIndexName() {
		return underlying.getName();
	}

	public Class<T> getIndexClass() {
		return (Class<T>) this.indexClass;
	}

	public Type getIndexType() {
		return Type.MANUAL;
	}

	public void put(final String key, final Object value, final T element) {
		final String keyTemp = key + SEPARATOR + value;

		final ODocument doc = element.getRawElement().getDocument();
		if (doc.getIdentity().isTemporary())
			doc.save();

		final boolean txBegun = graph.autoStartTransaction();
		try {
			underlying.put(keyTemp, doc);

			if (txBegun)
				graph.autoStopTransaction(TransactionalGraph.Conclusion.SUCCESS);

		} catch (RuntimeException e) {
			if (txBegun)
				graph.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
			throw e;
		} catch (Exception e) {
			if (txBegun)
				graph.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@SuppressWarnings("rawtypes")
	public Iterable<T> get(final String key, final Object value) {
		final String keyTemp = key + SEPARATOR + value;
		final List<ORecord<?>> recList = underlying.get(keyTemp);

		if (recList.isEmpty())
			return new LinkedList<T>();

		final OLazyObjectList<OGraphElement> list = new OLazyObjectList<OGraphElement>(graph.getRawGraph(), null, recList);
		return new OrientElementSequence(graph, list.iterator());
	}

	public void remove(final String key, final Object value, final T element) {
		final String keyTemp = key + SEPARATOR + value;

		final boolean txBegun = graph.autoStartTransaction();
		try {
			underlying.remove(keyTemp, element.getRawElement().getDocument());

			if (txBegun)
				graph.autoStopTransaction(TransactionalGraph.Conclusion.SUCCESS);
		} catch (RuntimeException e) {
			if (txBegun)
				graph.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
			throw e;
		} catch (Exception e) {
			if (txBegun)
				graph.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public String toString() {
		return underlying != null ? underlying.toString() : "?";
	}

	protected void clear() {
		try {
			if (null != this.underlying) {
				final boolean txBegun = graph.autoStartTransaction();
				try {
					this.underlying.clear();
					this.underlying.lazySave();

					if (txBegun)
						graph.autoStopTransaction(TransactionalGraph.Conclusion.SUCCESS);
				} catch (RuntimeException e) {
					if (txBegun)
						graph.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
					throw e;
				} catch (Exception e) {
					if (txBegun)
						graph.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
					throw new RuntimeException(e.getMessage(), e);
				}
			}
		} catch (Exception e) {
			// FIXME: is there a reason this exception is gobbled? (dw 2010-Dec-6)
			// throw new RuntimeException(e.getMessage(), e);
		}
	}

	protected void removeElement(final T vertex) {
		final ORecord<?> vertexDoc = (ORecord<?>) vertex.getRawElement().getDocument();
		List<ORecord<?>> rids;
		for (Entry<String, List<ORecord<?>>> entries : getRawIndex()) {
			rids = entries.getValue();

			if (rids != null) {
				ORecord<?> rec;
				for (int i = 0; i < rids.size(); ++i) {
					rec = rids.get(i);

					if (rec.equals(vertexDoc)) {
						underlying.remove(entries.getKey(), vertexDoc);
					}
				}
			}
		}
	}

	private void create(final String indexName, final Class<? extends Element> indexClass,
			final com.tinkerpop.blueprints.pgm.Index.Type type) {
		this.indexClass = indexClass;

		// CREATE THE MAP
		this.underlying = graph.getRawGraph().getMetadata().getIndexManager()
				.createIndex(indexName, OProperty.INDEX_TYPE.NOTUNIQUE.toString(), null, null, null);
		underlying.setName(indexName);

		final String className;
		if (Vertex.class.isAssignableFrom(indexClass))
			className = OrientGraph.VERTEX;
		else if (Edge.class.isAssignableFrom(indexClass))
			className = OrientGraph.EDGE;
		else
			className = indexClass.getName();

		// CREATE THE CONFIGURATION FOR THE NEW INDEX
		underlying.getConfiguration().field(CONFIG_CLASSNAME, className);
		underlying.getConfiguration().field(CONFIG_TYPE, type.toString());
	}

	private void load(final ODocument indexCfg) {
		// LOAD TREEMAP
		final String indexClassName = indexCfg.field(CONFIG_CLASSNAME);

		if ("Vertex".equals(indexClassName))
			this.indexClass = OrientVertex.class;
		else if ("Edge".equals(indexClassName))
			this.indexClass = OrientEdge.class;
		else
			try {
				this.indexClass = (Class<T>) Class.forName(indexClassName);
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException("Index class '" + indexClassName
						+ "' is not registered. Supported ones: Vertex, Edge and custom class that extends them");
			}

		// LOAD THE TREE-MAP
		underlying = new OIndexNotUnique().loadFromConfiguration(indexCfg);
	}

}
