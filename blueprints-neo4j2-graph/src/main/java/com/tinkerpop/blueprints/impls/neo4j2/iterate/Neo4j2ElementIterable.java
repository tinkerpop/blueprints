package com.tinkerpop.blueprints.impls.neo4j2.iterate;

import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.index.IndexHits;

import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Graph;

abstract class Neo4j2ElementIterable<T extends Element, S extends PropertyContainer>  implements CloseableIterable<T>{

	protected final Iterable<S> elements;
	protected final Neo4j2Graph graph;
	
	public Neo4j2ElementIterable(Neo4j2Graph graph, Iterable<S> elements) {
		this.graph = graph;
		this.elements = elements;
	}

	@Override
	public void close() {
		if (this.elements instanceof IndexHits) {
            ((IndexHits<?>) this.elements).close();
        }
	}

}
