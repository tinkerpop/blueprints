package com.tinkerpop.blueprints.pgm.impls.orientdb;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.tinkerpop.blueprints.pgm.TransactionalGraph.Mode;

import java.util.HashMap;
import java.util.Map;

/**
 * A Blueprints implementation of the graph database OrientDB (http://www.orientechnologies.com)
 *
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientGraphContext {
    public Mode txMode = Mode.AUTOMATIC;

    public Map<String, OrientIndex> manualIndices = new HashMap<String, OrientIndex>();
    public Map<String, OrientAutomaticIndex> autoIndices = new HashMap<String, OrientAutomaticIndex>();

    public OGraphDatabase rawGraph;
}
