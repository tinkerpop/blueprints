package com.tinkerpop.blueprints.pgm.impls.orientdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.db.graph.ODatabaseGraphTx;
import com.orientechnologies.orient.core.db.graph.OGraphEdge;
import com.orientechnologies.orient.core.db.graph.OGraphVertex;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.iterator.OGraphEdgeIterator;
import com.orientechnologies.orient.core.iterator.OGraphVertexIterator;
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

	private ODatabaseGraphTx										rawGraph;

	private final String												url;
	private final String												username;
	private final String												password;

	private Mode																mode					= Mode.AUTOMATIC;

	protected Map<String, OrientIndex>					manualIndices	= new HashMap<String, OrientIndex>();
	protected Map<String, OrientAutomaticIndex>	autoIndices		= new HashMap<String, OrientAutomaticIndex>();

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
		this.manualIndices.remove(iIndexName);
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

			final OrientEdge edge = new OrientEdge(this, ((OrientVertex) outVertex).getRawVertex().link(
					((OrientVertex) inVertex).getRawVertex()));
			edge.setLabel(label);

			((OrientVertex) outVertex).getRawVertex().getDocument().setDirty();
			((OrientVertex) inVertex).getRawVertex().getDocument().setDirty();

			// SAVE THE VERTICES TO ASSURE THEY ARE IN TX
			((OrientVertex) outVertex).save();
			((OrientVertex) inVertex).save();
			edge.save();

			autoStopTransaction(Conclusion.SUCCESS);

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

		final ODocument doc = this.rawGraph.getRecordById(rid);
		if (doc != null)
			return new OrientVertex(this, (OGraphVertex) this.rawGraph.getUserObjectByRecord(doc, null));
		else {
			OGraphVertex v = (OGraphVertex) this.rawGraph.load(rid);
			if (v != null)
				return new OrientVertex(this, (OGraphVertex) this.rawGraph.load(rid));
			else
				return null;
		}
	}

	public void removeVertex(final Vertex vertex) {
		try {
			AutomaticIndexHelper.unIndexElement(this, vertex);

			autoStartTransaction();

			for (Index index : this.getManualIndices()) {
				if (Vertex.class.isAssignableFrom(index.getIndexClass())) {
					OrientIndex<OrientVertex> idx = (OrientIndex<OrientVertex>) index;
					idx.removeElement((OrientVertex) vertex);
				}
			}

			((OrientVertex) vertex).delete();
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
		return new OrientElementSequence<Vertex>(this, new OGraphVertexIterator(this.rawGraph, true));
	}

	public Iterable<Edge> getEdges() {
		return new OrientElementSequence<Edge>(this, new OGraphEdgeIterator(this.rawGraph, true));
	}

	public Edge getEdge(final Object id) {
		final ORID rid;
		if (id instanceof ORID)
			rid = (ORID) id;
		else
			rid = new ORecordId(id.toString());

		final ODocument doc = this.rawGraph.getRecordById(rid);
		if (doc != null)
			return new OrientEdge(this, (OGraphEdge) this.rawGraph.getUserObjectByRecord(doc, null));
		else {
			OGraphEdge edge = (OGraphEdge) this.rawGraph.load(rid);
			if (edge != null)
				return new OrientEdge(this, edge);
			else
				return null;
		}
	}

	public void removeEdge(final Edge edge) {
		try {
			AutomaticIndexHelper.unIndexElement(this, edge);
			autoStartTransaction();

			for (Index index : this.getManualIndices()) {
				if (Edge.class.isAssignableFrom(index.getIndexClass())) {
					OrientIndex<OrientEdge> idx = (OrientIndex<OrientEdge>) index;
					idx.removeElement((OrientEdge) edge);
				}
			}

			((OrientEdge) edge).delete();
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
		openOrCreate(false);
	}

	public void shutdown() {
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

	public ODatabaseGraphTx getRawGraph() {
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
		} else
			this.rawGraph.commit();
	}

	public void setTransactionMode(final Mode mode) {
		this.mode = mode;
	}

	public Mode getTransactionMode() {
		return this.mode;
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
		this.rawGraph = new ODatabaseGraphTx(url);
		if (this.rawGraph.exists()) {
			this.rawGraph.open(username, password);

			// LOAD THE INDEX CONFIGURATION FROM INTO THE DICTIONARY
			final ODocument indexConfiguration = ((ODatabaseDocumentTx) rawGraph.getUnderlying()).getMetadata().getIndexManager()
					.getDocument();
			if (indexConfiguration == null)
				createIndexConfiguration(createDefaultIndices);

			for (OIndex idx : ((ODatabaseDocumentTx) rawGraph.getUnderlying()).getMetadata().getIndexManager().getIndexes()) {
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
	private OrientIndex<?> loadIndex(final OIndex iIndex) {
		final String indexType = iIndex.getConfiguration().field(OrientIndex.CONFIG_TYPE);

		final OrientIndex<?> index;

		switch (Index.Type.valueOf(indexType.toUpperCase())) {
		case MANUAL:
			index = new OrientIndex(this, iIndex);
			break;
		case AUTOMATIC:
			// index = new OrientAutomaticIndex(indexName, null, this, indexCfg);
			index = new OrientAutomaticIndex(this, iIndex);
			// REGISTER THE INDEX INTO THE AUTOMATIC INDEXES
			this.autoIndices.put(index.getIndexName(), (OrientAutomaticIndex<?>) index);
			break;
		default:
			throw new IllegalArgumentException("Index type '" + indexType + "' is not supported. Supported indicies: MANUAL, AUTOMATIC");
		}

		// REGISTER THE INDEX
		this.manualIndices.put(index.getIndexName(), index);

		return index;
	}
}
