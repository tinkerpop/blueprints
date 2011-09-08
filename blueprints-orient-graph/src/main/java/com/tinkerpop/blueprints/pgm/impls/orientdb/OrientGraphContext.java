package com.tinkerpop.blueprints.pgm.impls.orientdb;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * A Blueprints implementation of the graph database OrientDB (http://www.orientechnologies.com)
 *
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientGraphContext {
    public int txBuffer = 1;
    public int txCounter = 0;

    public Map<String, OrientIndex<? extends OrientElement>> manualIndices = new HashMap<String, OrientIndex<? extends OrientElement>>();
    public Map<String, OrientAutomaticIndex<? extends OrientElement>> autoIndices = new HashMap<String, OrientAutomaticIndex<? extends OrientElement>>();

    public OGraphDatabase rawGraph;
}
