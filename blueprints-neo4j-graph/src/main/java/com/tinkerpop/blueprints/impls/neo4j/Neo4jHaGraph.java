package com.tinkerpop.blueprints.impls.neo4j;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationConverter;
import org.neo4j.graphdb.factory.HighlyAvailableGraphDatabaseFactory;
import org.neo4j.kernel.ha.HighlyAvailableGraphDatabase;

import java.util.Map;

/**
 * A Blueprints implementation of the graph database Neo4j (http://neo4j.org) with High Availability mode.
 *
 * @author Stephen Mallette
 */
public class Neo4jHaGraph extends Neo4jGraph {

    public Neo4jHaGraph(final String directory) {
        super(new HighlyAvailableGraphDatabaseFactory().newHighlyAvailableDatabase(directory));
    }

    public Neo4jHaGraph(final String directory, final Map<String, String> configuration) {
        super(new HighlyAvailableGraphDatabaseFactory().newHighlyAvailableDatabaseBuilder(directory).setConfig(configuration).newGraphDatabase());
    }

    public Neo4jHaGraph(final HighlyAvailableGraphDatabase rawGraph) {
        super(rawGraph);
    }

    public Neo4jHaGraph(final Configuration configuration) {
        this(configuration.getString("blueprints.neo4jha.directory", null),
                ConfigurationConverter.getMap(configuration.subset("blueprints.neo4jha.conf")));
    }
}
