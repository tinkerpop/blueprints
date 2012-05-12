package com.tinkerpop.blueprints.pgm.impls.neo4j;


import com.tinkerpop.blueprints.pgm.CloseableIterable;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jVertex;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.IndexHits;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jVertexIterable<T extends Vertex> implements CloseableIterable<Neo4jVertex> {

    private final Iterable<Node> nodes;
    private final Neo4jGraph graph;

    public Neo4jVertexIterable(final Iterable<Node> nodes, final Neo4jGraph graph) {
        this.graph = graph;
        this.nodes = nodes;
    }


    public Iterator<Neo4jVertex> iterator() {
        return new Iterator<Neo4jVertex>() {
            private final Iterator<Node> itty = nodes.iterator();

            public void remove() {
                this.itty.remove();
            }

            public Neo4jVertex next() {
                return new Neo4jVertex(this.itty.next(), graph);
            }

            public boolean hasNext() {
                return this.itty.hasNext();
            }
        };
    }

    public void close() {
        if (this.nodes instanceof IndexHits) {
            ((IndexHits) this.nodes).close();
        }
    }

}