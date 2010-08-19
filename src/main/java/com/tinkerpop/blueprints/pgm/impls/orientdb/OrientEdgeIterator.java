/*
 * Copyright 1999-2010 Luca Garulli (l.garulli--at--orientechnologies.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tinkerpop.blueprints.pgm.impls.orientdb;

import java.util.Iterator;

import com.orientechnologies.orient.core.db.graph.ODatabaseGraphTx;
import com.orientechnologies.orient.core.db.graph.OGraphEdge;
import com.orientechnologies.orient.core.iterator.OGraphEdgeIterator;
import com.tinkerpop.blueprints.pgm.Edge;

/**
 * Iterator to browse all the edges.
 * 
 * @author Luca Garulli
 * 
 */
public class OrientEdgeIterator implements Iterator<Edge>, Iterable<Edge> {
	private OGraphEdgeIterator	underlying;

	public OrientEdgeIterator(final ODatabaseGraphTx iDatabase) {
		underlying = new OGraphEdgeIterator(iDatabase);
	}

	public boolean hasNext() {
		return underlying.hasNext();
	}

	public Edge next() {
		final OGraphEdge v = underlying.next();

		if (v == null)
			return null;

		return new OrientEdge(v);
	}

	public void remove() {
		underlying.remove();
	}

	public Iterator<Edge> iterator() {
		return this;
	}
}
