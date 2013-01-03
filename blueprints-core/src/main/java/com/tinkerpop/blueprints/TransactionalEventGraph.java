package com.tinkerpop.blueprints;

import java.util.Collection;

import com.tinkerpop.blueprints.transaction.TransactionEventListener;

public interface TransactionalEventGraph extends TransactionalGraph {
	public <T> TransactionEventListener<T> registerTransactionEventListener(
			TransactionEventListener<T> listener);

	public <T> TransactionEventListener<T> unregisterTransactionEventListener(
			TransactionEventListener<T> listener);

	public <T> T unregisterListener(Collection<?> listeners, T listener);

	public boolean hasListeners();
}