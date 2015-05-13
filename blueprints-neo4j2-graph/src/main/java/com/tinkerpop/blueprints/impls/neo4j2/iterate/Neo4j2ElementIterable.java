package com.tinkerpop.blueprints.impls.neo4j2.iterate;

import java.util.Iterator;

import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.index.IndexHits;

import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Graph;
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Graph.ElementWrapper;

public abstract class Neo4j2ElementIterable<T extends Element, S extends PropertyContainer>  implements CloseableIterable<T>{

	protected final Iterable<S> elements;
	protected final Neo4j2Graph graph;
	protected final ElementWrapper<? extends T, S> elementWrapper;
	
	public Neo4j2ElementIterable(Iterable<S> elements, Neo4j2Graph graph, ElementWrapper<? extends T, S> elementWrapper) {
		this.elements = elements;
		this.graph = graph;
		this.elementWrapper = elementWrapper;
	}
	

	@Override
	public void close() {
		if (this.elements instanceof IndexHits) {
            ((IndexHits<?>) this.elements).close();
        }
	}
	
	@Override
	public Iterator<T> iterator() {
		this.graph.autoStartTransaction(false);
		final Iterator<S> elementIterator = elements.iterator(); 
		return new Iterator<T>() {

			@Override
			public boolean hasNext() {
				graph.autoStartTransaction(false);
				return elementIterator.hasNext();
			}

			@Override
			public T next() {
				graph.autoStartTransaction(false);
				return elementWrapper.wrap(elementIterator.next());
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

		};
	}

}
