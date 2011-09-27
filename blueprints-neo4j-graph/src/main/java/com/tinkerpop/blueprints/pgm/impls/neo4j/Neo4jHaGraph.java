package com.tinkerpop.blueprints.pgm.impls.neo4j;

import org.neo4j.kernel.HighlyAvailableGraphDatabase;

import java.util.Map;

/**
 * A Blueprints implementation of the graph database Neo4j (http://neo4j.org) with High Availability mode.
 *
 * @author Stephen Mallette
 */
public class Neo4jHaGraph extends Neo4jGraph {

    public Neo4jHaGraph(final String directory, final Map<String, String> configuration) {
        super(directory, configuration, true);
    }

    public Neo4jHaGraph(final HighlyAvailableGraphDatabase rawGraph) {
        super(rawGraph);
    }
}
