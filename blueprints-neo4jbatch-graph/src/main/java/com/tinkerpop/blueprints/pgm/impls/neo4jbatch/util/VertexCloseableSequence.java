package com.tinkerpop.blueprints.pgm.impls.neo4jbatch.util;

import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.neo4jbatch.Neo4jBatchGraph;
import com.tinkerpop.blueprints.pgm.impls.neo4jbatch.Neo4jBatchVertex;
import org.neo4j.graphdb.index.IndexHits;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class VertexCloseableSequence extends ElementCloseableSequence<Vertex> {

    public VertexCloseableSequence(final Neo4jBatchGraph graph, final IndexHits<Long> hits) {
        super(graph, hits);
    }

    public Vertex next() {
        return new Neo4jBatchVertex(this.graph, this.hits.next());
    }
}
