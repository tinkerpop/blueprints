package com.tinkerpop.blueprints.pgm.impls.orientdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.db.record.ODatabaseRecordAbstract;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.iterator.ORecordIteratorClass;
import com.orientechnologies.orient.core.record.ORecordInternal;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.tx.OTransaction.TXSTATUS;
import com.orientechnologies.orient.core.tx.OTransactionNoTx;
import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.orientdb.util.OrientElementSequence;
import com.tinkerpop.blueprints.pgm.util.AutomaticIndexHelper;

/**
 * A Blueprints implementation of the graph database OrientDB (http://www.orientechnologies.com)
 * 
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientGraph implements TransactionalGraph, IndexableGraph {
	public static final String									VERTEX				= "Vertex";
	public static final String									EDGE					= "Edge";
	private final static String									ADMIN					= "admin";

	private OGraphDatabase											rawGraph;

	private final String												url;
	private final String												username;
	private final String												password;

	private Mode																mode					= Mode.AUTOMATIC;

	protected Map<String, OrientIndex>					manualIndices	= new HashMap<String, OrientIndex>();
	protected Map<String, OrientAutomaticIndex>	autoIndices		= new HashMap<String, OrientAutomaticIndex>();
	protected Map<ORecordId, OrientElement>			cache					= new HashMap<ORecordId, OrientElement>();

	public OrientGraph(final String url) {
		this(url, ADMIN, ADMIN);
	}

	public OrientGraph(final String url, final String username, final String password) {
		this.url = url;
		this.username = username;
		this.password = password;
		this.openOrCreate(true);
	}

	public <T extends Element> AutomaticIndex<T> createAutomaticIndex(final String indexName, final Class<T> indexClass,
			final Set<String> indexKeys) {
		if (this.autoIndices.containsKey(indexName))
			throw new RuntimeException("Index already exists: " + indexName);

		final OrientAutomaticIndex index = new OrientAutomaticIndex<OrientElement>(this, indexName, (Class<OrientElement>) indexClass,
				indexKeys);
		this.autoIndices.put(index.getIndexName(), index);

		// SAVE THE CONFIGURATION INTO THE GLOBAL CONFIG
		saveIndexConfiguration();

		return index;
	}

	public <T extends Element> Index<T> createManualIndex(final String indexName, final Class<T> indexClass) {
		if (this.manualIndices.containsKey(indexName))
			throw new RuntimeException("Index already exists: " + indexName);

		final OrientIndex index = new OrientIndex(this, indexName, indexClass, Index.Type.MANUAL);
		this.manualIndices.put(index.getIndexName(), index);

		// SAVE THE CONFIGURATION INTO THE GLOBAL CONFIG
		saveIndexConfiguration();

		return index;
	}

	public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
		Index<?> index = this.manualIndices.get(indexName);
		if (null == index) {
			index = this.autoIndices.get(indexName);
			if (null == index)
				throw new RuntimeException("No such index exists: " + indexName);
		}

		if (indexClass.isAssignableFrom(index.getIndexClass()))
			return (Index<T>) index;
		else
			throw new RuntimeException("Can not convert " + index.getIndexClass() + " to " + indexClass);
	}

	public Iterable<Index<? extends Element>> getIndices() {
		final List<Index<? extends Element>> list = new ArrayList<Index<? extends Element>>();
		for (Index<?> index : this.manualIndices.values()) {
			list.add(index);
		}
		for (Index<?> index : this.autoIndices.values()) {
			list.add(index);
		}
		return list;
	}

	protected Iterable<OrientIndex> getManualIndices() {
		return this.manualIndices.values();
	}

	protected Iterable<OrientAutomaticIndex> getAutoIndices() {
		return this.autoIndices.values();
	}

	public void dropIndex(final String iIndexName) {
		if (this.manualIndices.remove(iIndexName) == null)
			this.autoIndices.remove(iIndexName);

		rawGraph.getMetadata().getIndexManager().deleteIndex(iIndexName);
		saveIndexConfiguration();
	}

	public Vertex addVertex(final Object id) {
		try {
			autoStartTransaction();
			final OrientVertex vertex = new OrientVertex(this, this.rawGraph.createVertex(null));
			vertex.save();

			autoStopTransaction(Conclusion.SUCCESS);

			cache.put((ORecordId) vertex.getId(), vertex);

			return vertex;
		} catch (RuntimeException e) {
			autoStopTransaction(Conclusion.FAILURE);
			throw e;
		} catch (Exception e) {
			autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
		try {
			autoStartTransaction();

			final ODocument edgeDoc = rawGraph.createEdge(((OrientVertex) outVertex).getRawElement(),
					((OrientVertex) inVertex).getRawElement());
			final OrientEdge edge = new OrientEdge(this, edgeDoc);
			edge.setLabel(label);

			// SAVE THE VERTICES TO ASSURE THEY ARE IN TX
			rawGraph.save(((OrientVertex) outVertex).getRawElement());
			rawGraph.save(((OrientVertex) inVertex).getRawElement());
			edge.save();

			autoStopTransaction(Conclusion.SUCCESS);

			cache.put((ORecordId) edge.getId(), edge);

			return edge;

		} catch (RuntimeException e) {
			autoStopTransaction(Conclusion.FAILURE);
			throw e;
		} catch (Exception e) {
			autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public Vertex getVertex(final Object id) {
		ORID rid;
		if (id instanceof ORID)
			rid = (ORID) id;
		else
			rid = new ORecordId(id.toString());

		if (!rid.isValid())
			return null;

		final OrientElement e = cache.get(rid);
		if (e != null)
			return (Vertex) e;

		final ODocument doc = this.rawGraph.load(rid);
		if (doc != null) {
			final OrientVertex vertex = new OrientVertex(this, doc);
			cache.put((ORecordId) vertex.getId(), vertex);
			return vertex;
		}

		return null;
	}

	public void removeVertex(final Vertex vertex) {
		final OrientVertex oVertex = (OrientVertex) vertex;
		if (oVertex == null || oVertex.getRawElement() == null)
			return;

		try {
			AutomaticIndexHelper.removeElement(this, vertex);

			autoStartTransaction();

			for (Index index : this.getManualIndices()) {
				if (Vertex.class.isAssignableFrom(index.getIndexClass())) {
					OrientIndex<OrientVertex> idx = (OrientIndex<OrientVertex>) index;
					idx.removeElement(oVertex);
				}
			}

			cache.remove(oVertex.getId());

			rawGraph.removeVertex(oVertex.rawElement);

			autoStopTransaction(Conclusion.SUCCESS);
		} catch (RuntimeException e) {
			autoStopTransaction(Conclusion.FAILURE);
			throw e;
		} catch (Exception e) {
			autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public Iterable<Vertex> getVertices() {
		return getVertices(true);
	}

	public Iterable<Vertex> getVertices(final boolean iPolymorphic) {
		return new OrientElementSequence<Vertex>(this, new ORecordIteratorClass<ORecordInternal<?>>(rawGraph,
				(ODatabaseRecordAbstract) rawGraph.getUnderlying(), OGraphDatabase.VERTEX_CLASS_NAME, iPolymorphic));
	}

	public Iterable<Edge> getEdges() {
		return getEdges(true);
	}

	public Iterable<Edge> getEdges(final boolean iPolymorphic) {
		return new OrientElementSequence<Edge>(this, new ORecordIteratorClass<ORecordInternal<?>>(rawGraph,
				(ODatabaseRecordAbstract) rawGraph.getUnderlying(), OGraphDatabase.EDGE_CLASS_NAME, iPolymorphic));
	}

	public Edge getEdge(final Object id) {
		final ORID rid;
		if (id instanceof ORID)
			rid = (ORID) id;
		else
			rid = new ORecordId(id.toString());

		final OrientElement e = cache.get(rid);
		if (e != null)
			return (Edge) e;

		final ODocument doc = this.rawGraph.load(rid);
		if (doc != null) {
			final OrientEdge edge = new OrientEdge(this, doc);
			cache.put((ORecordId) edge.getId(), edge);
			return edge;
		}

		return null;
	}

	public void removeEdge(final Edge edge) {
		final OrientEdge oEdge = (OrientEdge) edge;
		if (oEdge == null || oEdge.getRawElement() == null)
			return;

		try {
			AutomaticIndexHelper.removeElement(this, edge);
			autoStartTransaction();

			for (Index index : this.getManualIndices()) {
				if (Edge.class.isAssignableFrom(index.getIndexClass())) {
					OrientIndex<OrientEdge> idx = (OrientIndex<OrientEdge>) index;
					idx.removeElement(oEdge);
				}
			}

			cache.remove(oEdge.getId());

			rawGraph.removeEdge(oEdge.rawElement);

			autoStopTransaction(Conclusion.SUCCESS);
		} catch (RuntimeException e) {
			autoStopTransaction(Conclusion.FAILURE);
			throw e;
		} catch (Exception e) {
			autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void clear() {
		this.manualIndices.clear();
		this.autoIndices.clear();
		this.rawGraph.delete();
		this.rawGraph = null;
		this.cache.clear();
		openOrCreate(false);
	}

	public void shutdown() {
		for (OrientElement element : this.cache.values()) {
			if (element.getRawElement().isDirty())
				rawGraph.save(element.getRawElement());
		}
		this.cache.clear();

		if (this.rawGraph != null) {
			this.rawGraph.rollback();
			this.rawGraph.close();
			this.rawGraph = null;
		}

		this.manualIndices.clear();
		this.autoIndices.clear();
	}

	public String toString() {
		return "orientgraph[" + this.rawGraph.getURL() + "]";
	}

	public OGraphDatabase getRawGraph() {
		return this.rawGraph;
	}

	public void startTransaction() {
		if (Mode.AUTOMATIC == this.mode)
			throw new RuntimeException(TransactionalGraph.TURN_OFF_MESSAGE);
		if (rawGraph.getTransaction() instanceof OTransactionNoTx || rawGraph.getTransaction().getStatus() != TXSTATUS.BEGUN) {
			this.rawGraph.begin();
		} else
			throw new RuntimeException(TransactionalGraph.NESTED_MESSAGE);
	}

	public void stopTransaction(final Conclusion conclusion) {
		if (Mode.AUTOMATIC == this.mode)
			throw new RuntimeException(TransactionalGraph.TURN_OFF_MESSAGE);

		if (conclusion == Conclusion.FAILURE) {
			this.rawGraph.rollback();
			for (Index<?> index : this.manualIndices.values())
				((OrientIndex<?>) index).getRawIndex().unload();
			for (Index<?> index : this.autoIndices.values())
				((OrientIndex<?>) index).getRawIndex().unload();
		} else
			this.rawGraph.commit();
	}

	public void setTransactionMode(final Mode mode) {
		this.mode = mode;
	}

	public Mode getTransactionMode() {
		return this.mode;
	}

	public Map<ORecordId, OrientElement> getCache() {
		return cache;
	}

	void saveIndexConfiguration() {
		rawGraph.getMetadata().getIndexManager().setDirty();
		rawGraph.getMetadata().getIndexManager().save();
	}

	protected boolean autoStartTransaction() {
		if (getTransactionMode() == Mode.AUTOMATIC
				&& (rawGraph.getTransaction() instanceof OTransactionNoTx || rawGraph.getTransaction().getStatus() != TXSTATUS.BEGUN)) {
			this.rawGraph.begin();
			return true;
		}
		return false;
	}

	protected void autoStopTransaction(final Conclusion conclusion) {
		if (getTransactionMode() == Mode.AUTOMATIC) {
			if (conclusion == Conclusion.SUCCESS)
				this.rawGraph.commit();
			else {
				this.rawGraph.rollback();
				for (Index<?> index : this.manualIndices.values())
					((OrientIndex<?>) index).getRawIndex().unload();
				for (Index<?> index : this.autoIndices.values())
					((OrientIndex<?>) index).getRawIndex().unload();
			}

		}
	}

	private void openOrCreate(final boolean createDefaultIndices) {
		this.rawGraph = new OGraphDatabase(url);
		if (this.rawGraph.exists()) {
			this.rawGraph.open(username, password);

			// LOAD THE INDEX CONFIGURATION FROM INTO THE DICTIONARY
			final ODocument indexConfiguration = rawGraph.getMetadata().getIndexManager().getDocument();
			if (indexConfiguration == null)
				createIndexConfiguration(createDefaultIndices);

			for (OIndex idx : rawGraph.getMetadata().getIndexManager().getIndexes()) {
				if (idx.getConfiguration().field(OrientIndex.CONFIG_TYPE) != null)
					// LOAD THE INDEXES
					loadIndex(idx);
			}

		} else {
			this.rawGraph.create();

			// CREATE THE INDEX CONFIGURATION FOR IT AND SAVE IT INTO THE DICTIONARY
			createIndexConfiguration(createDefaultIndices);
		}
	}

	private void createIndexConfiguration(final boolean createDefaultIndices) {
		if (createDefaultIndices) {
			this.createAutomaticIndex(Index.VERTICES, OrientVertex.class, null);
			this.createAutomaticIndex(Index.EDGES, OrientEdge.class, null);
		}
	}

	@SuppressWarnings("rawtypes")
	private OrientIndex<?> loadIndex(final OIndex rawIndex) {
		String indexType = rawIndex.getConfiguration().field(OrientIndex.CONFIG_TYPE);
		/*
		 * if (null == indexType) indexType = "MANUAL";
		 */
		final OrientIndex<?> index;

		switch (Index.Type.valueOf(indexType.toUpperCase())) {
		case MANUAL:
			index = new OrientIndex(this, rawIndex);

			// REGISTER THE INDEX
			this.manualIndices.put(index.getIndexName(), index);
			break;

		case AUTOMATIC:
			index = new OrientAutomaticIndex(this, rawIndex);

			// REGISTER THE INDEX INTO THE AUTOMATIC INDEXES
			this.autoIndices.put(index.getIndexName(), (OrientAutomaticIndex<?>) index);
			break;
		default:
			throw new IllegalArgumentException("Index type '" + indexType + "' is not supported. Supported indicies: MANUAL, AUTOMATIC");
		}

		return index;
	}
}
