package com.tinkerpop.blueprints.pgm.util;

import com.tinkerpop.blueprints.pgm.TransactionalGraph;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class TransactionalGraphHelper {

    /**
     * An OperationCounter puts the transactional graph into manual transaction mode.
     * Next, a transaction is started. When the internal counter has reached the provided buffer size, the transaction is committed.
     * Upon closing the OperationCounter, the open transaction is committed and the graph is put back into its original transaction mode.
     *
     * @param graph      the transactional graph to maintain a counter for
     * @param bufferSize the number of counters before the transaction is committed
     * @return the OperationCounter to use when manipulating the transactional graph
     */
    public static OperationCounter createOperationCounter(final TransactionalGraph graph, final int bufferSize) {
        return new OperationCounter(graph, bufferSize);
    }

    public static class OperationCounter {
        private final TransactionalGraph graph;
        private final TransactionalGraph.Mode startMode;
        private final int bufferSize;
        private long counter = 0;

        public OperationCounter(final TransactionalGraph graph, final int bufferSize) {
            this.graph = graph;
            this.startMode = graph.getTransactionMode();
            this.bufferSize = bufferSize;

            this.graph.setTransactionMode(TransactionalGraph.Mode.MANUAL);
            this.graph.startTransaction();
        }

        /**
         * After performing some operation (or set of operations), increment the counter.
         * If the counter % bufferSize == 0, then the current transaction is committed and a new transaction is started.
         */
        public void incrCounter() {
            this.counter++;
            if ((this.counter % this.bufferSize) == 0) {
                this.graph.stopTransaction(TransactionalGraph.Conclusion.SUCCESS);
                this.graph.startTransaction();
            }
        }

        /**
         * Get the current number of operations counted.
         *
         * @return the current counter
         */
        public long getCounter() {
            return this.counter;
        }

        /**
         * Determine is counter % bufferSize == 0. That is, if the operation counter just committed a transaction.
         * This is useful for printing information to the screen when parsing large data sets.
         * For example: if(counter.atCommit()) { System.out.print('.'); }
         *
         * @return whether the operation counter committed a transaction on the last incrCounter()
         */
        public boolean atCommit() {
            return (this.counter % this.bufferSize) == 0;
        }

        /**
         * Close the OperationCounter by committing the current transaction.
         * Finally, set the transactional graph to its original transaction mode.
         */
        public void close() {
            this.graph.stopTransaction(TransactionalGraph.Conclusion.SUCCESS);
            this.graph.setTransactionMode(this.startMode);
        }

    }
}
