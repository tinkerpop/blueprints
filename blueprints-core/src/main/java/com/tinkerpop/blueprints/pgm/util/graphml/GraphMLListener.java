package com.tinkerpop.blueprints.pgm.util.graphml;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * GraphMLReader listener to receive callbacks during importing.
 * 
 * @author Luca Garulli (l.garulli@orientechnologies.com)
 */
public interface GraphMLListener {
	public void onVertex(Vertex edgeInVertex);

	public void onEdge(Edge currentEdge);
}
