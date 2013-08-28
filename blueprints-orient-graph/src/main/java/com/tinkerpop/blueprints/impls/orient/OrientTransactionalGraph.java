package com.tinkerpop.blueprints.impls.orient;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.tx.OTransaction.TXSTATUS;
import com.orientechnologies.orient.core.tx.OTransactionNoTx;

/**
 * A Blueprints implementation of the graph database OrientDB
 * (http://www.orientechnologies.com)
 * 
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public abstract class OrientTransactionalGraph extends OrientBaseGraph {
	protected boolean autoStartTx = true;

	/**
	 * Constructs a new object using an existent OGraphDatabase instance.
	 * 
	 * @param iDatabase
	 *            Underlying OGraphDatabase object to attach
	 */
	public OrientTransactionalGraph(final ODatabaseDocumentTx iDatabase) {
		super(iDatabase);
		autoStartTransaction();
	}

	public OrientTransactionalGraph(final String url) {
		super(url, ADMIN, ADMIN);
		autoStartTransaction();
	}

	public OrientTransactionalGraph(final String url, final String username,
			final String password) {
		super(url, username, password);
		autoStartTransaction();
	}

	public void commit() {
		final OrientGraphContext context = getContext(false);
		if (context == null)
			return;

		context.rawGraph.commit();
		autoStartTransaction();
	}

	public void rollback() {
		final OrientGraphContext context = getContext(false);
		if (context == null)
			return;

		context.rawGraph.rollback();
		autoStartTransaction();
	}

	@Override
	protected void autoStartTransaction() {
		super.autoStartTransaction();

		if (!autoStartTx)
			return;

		final OrientGraphContext context = getContext(true);
		if (context.rawGraph.getTransaction() instanceof OTransactionNoTx
				&& context.rawGraph.getTransaction().getStatus() != TXSTATUS.BEGUN) {
			context.rawGraph.begin();
		}
	}

	/**
	 * Tells if a transaction is started automatically when the graph is
	 * changed. This affects only when a transaction hasn't been started.
	 * Default is true.
	 * 
	 * @return
	 */
	public boolean isAutoStartTx() {
		return autoStartTx;
	}

	/**
	 * If enabled auto starts a new transaction right before the graph is
	 * changed. This affects only when a transaction hasn't been started.
	 * Default is true.
	 * 
	 * @param autoStartTx
	 */
	public void setAutoStartTx(final boolean autoStartTx) {
		this.autoStartTx = autoStartTx;
	}
}