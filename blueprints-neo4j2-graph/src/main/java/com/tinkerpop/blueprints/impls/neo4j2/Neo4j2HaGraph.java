package com.tinkerpop.blueprints.impls.neo4j2;

import org.apache.commons.configuration.Configuration;
import org.neo4j.graphdb.factory.HighlyAvailableGraphDatabaseFactory;
import org.neo4j.kernel.ha.HighlyAvailableGraphDatabase;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A Blueprints implementation of the graph database Neo4j (http://neo4j.org) with High Availability mode.
 *
 * @author Stephen Mallette
 */
public class Neo4j2HaGraph extends Neo4j2Graph {

    public Neo4j2HaGraph(final String directory) {
        super(new HighlyAvailableGraphDatabaseFactory().newHighlyAvailableDatabase(directory));
    }

    public Neo4j2HaGraph(final String directory, final Map<String, String> configuration) {
        super(new HighlyAvailableGraphDatabaseFactory().newHighlyAvailableDatabaseBuilder(directory).setConfig(configuration).newGraphDatabase());
    }

    public Neo4j2HaGraph(final HighlyAvailableGraphDatabase rawGraph) {
        super(rawGraph);
    }

    public Neo4j2HaGraph(final Configuration configuration) {
        this(configuration.getString("blueprints.neo4jha.directory", null),
                convertConfiguration(configuration.subset("blueprints.neo4jha.conf")));
    }

    private static Map<String,String> convertConfiguration(final Configuration configuration) {
        final Map<String,String> c = new HashMap<String,String>();
        final Iterator<String> keys = configuration.getKeys();
        while (keys.hasNext()) {
            final String k = keys.next();
            c.put(k, configuration.getString(k));
        }
        return c;
    }
}
