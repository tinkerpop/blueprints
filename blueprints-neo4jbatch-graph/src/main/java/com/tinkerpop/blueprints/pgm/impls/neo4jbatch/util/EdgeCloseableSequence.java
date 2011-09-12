package com.tinkerpop.blueprints.pgm.impls.neo4jbatch.util;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.impls.neo4jbatch.Neo4jBatchEdge;
import com.tinkerpop.blueprints.pgm.impls.neo4jbatch.Neo4jBatchGraph;
import org.neo4j.graphdb.index.IndexHits;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class EdgeCloseableSequence extends ElementCloseableSequence<Edge> {

    public EdgeCloseableSequence(final Neo4jBatchGraph graph, final IndexHits<Long> hits) {
        super(graph, hits);
    }

    public Edge next() {
        return new Neo4jBatchEdge(this.graph, this.hits.next(), null);
    }
}