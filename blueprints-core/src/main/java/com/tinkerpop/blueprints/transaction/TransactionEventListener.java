package com.tinkerpop.blueprints.transaction;


public interface TransactionEventListener<T> {
	T onBeforeCommit(TransactionData data) throws Exception;

	void onAfterCommit(TransactionData data, T state);

	void onAfterRollback(TransactionData data, T state);
}
