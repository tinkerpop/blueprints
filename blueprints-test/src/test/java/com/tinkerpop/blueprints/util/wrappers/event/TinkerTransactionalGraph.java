package com.tinkerpop.blueprints.util.wrappers.event;

import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory;
import com.tinkerpop.blueprints.util.GraphHelper;

class TinkerTransactionalGraph extends TinkerGraph implements TransactionalGraph {

    @Override
    public void stopTransaction(Conclusion conclusion) {
    }

    public void rollback() {

    }

    public void commit() {

    }

    public static TinkerTransactionalGraph createTinkerGraph() {

        final TinkerTransactionalGraph graph = new TinkerTransactionalGraph();
        GraphHelper.copyGraph(TinkerGraphFactory.createTinkerGraph(), graph);

        return graph;

    }
}
