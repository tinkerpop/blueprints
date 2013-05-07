package com.tinkerpop.blueprints.impls.orient;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.tx.OTransaction.TXSTATUS;
import com.orientechnologies.orient.core.tx.OTransactionNoTx;
import com.tinkerpop.blueprints.TransactionalGraph;

/**
 * A Blueprints implementation of the graph database OrientDB (http://www.orientechnologies.com)
 * 
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public abstract class OrientTransactionalGraph extends OrientBaseGraph implements TransactionalGraph {
  protected boolean autoStartTx = true;

  /**
   * Constructs a new object using an existent OGraphDatabase instance.
   * 
   * @param iDatabase
   *          Underlying OGraphDatabase object to attach
   */
  public OrientTransactionalGraph(final OGraphDatabase iDatabase) {
    super(iDatabase);
  }

  public OrientTransactionalGraph(final String url) {
    super(url, ADMIN, ADMIN);
  }

  public OrientTransactionalGraph(final String url, final String username, final String password) {
    super(url, username, password);
  }

  @SuppressWarnings("deprecation")
  @Override
  public void stopTransaction(final Conclusion conclusion) {
    final OrientGraphContext context = getContext(false);
    if (context.rawGraph.isClosed() || context.rawGraph.getTransaction() instanceof OTransactionNoTx
        || context.rawGraph.getTransaction().getStatus() != TXSTATUS.BEGUN)
      return;

    if (Conclusion.SUCCESS == conclusion)
      commit();
    else
      rollback();
  }

  public void commit() {
    this.getRawGraph().commit();
  }

  public void rollback() {
    this.getRawGraph().rollback();
  }

  @Override
  protected void autoStartTransaction() {
    if (!autoStartTx)
      return;

    final OrientGraphContext context = getContext(true);
    if (context.rawGraph.getTransaction() instanceof OTransactionNoTx
        && context.rawGraph.getTransaction().getStatus() != TXSTATUS.BEGUN) {
      context.rawGraph.begin();
    }
  }

  /**
   * Tells if a transaction is started automatically when the graph is changed. This affects only when a transaction hasn't been
   * started. Default is true.
   * 
   * @return
   */
  public boolean isAutoStartTx() {
    return autoStartTx;
  }

  /**
   * If enabled auto starts a new transaction right before the graph is changed. This affects only when a transaction hasn't been
   * started. Default is true.
   * 
   * @param autoStartTx
   */
  public void setAutoStartTx(final boolean autoStartTx) {
    this.autoStartTx = autoStartTx;
  }
}