package com.tinkerpop.blueprints.pgm.impls.neo4j;

import org.neo4j.kernel.HighlyAvailableGraphDatabase;

import java.util.Map;

/**
 * A Blueprints implementation of the graph database Neo4j (http://neo4j.org) with High Availability mode.
 *
 * @author Stephen Mallette
 */
public class Neo4jHaGraph extends Neo4jGraph {

    /**
     * Creates a new Neo4jHaGraph instance.
     * <p/>
     * The configuration parameter expects the standard neo4j configuration settings but also requires
     * some standard configuration elements for HA mode. These configuration keys are: ha.machine_id,
     * ha.server, ha.zoo_keeper_servers.
     * <p/>
     * These configuration elements are described in detail here:
     * <p/>
     * http://wiki.neo4j.org/content/High_Availability_Cluster
     */
    public Neo4jHaGraph(final String directory, final Map<String, String> configuration) {
        super(directory, configuration, true);
    }

    public Neo4jHaGraph(final HighlyAvailableGraphDatabase rawGraph) {
        super(rawGraph);
    }
}
