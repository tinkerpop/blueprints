package com.tinkerpop.blueprints.impls.neo4j2.rexster;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Graph;
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2HaGraph;
import com.tinkerpop.rexster.Tokens;
import com.tinkerpop.rexster.config.GraphConfiguration;
import com.tinkerpop.rexster.config.GraphConfigurationContext;
import com.tinkerpop.rexster.config.GraphConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;

import java.util.HashMap;
import java.util.Iterator;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class Neo4j2GraphConfiguration implements GraphConfiguration {

    public Graph configureGraphInstance(final GraphConfigurationContext context) throws GraphConfigurationException {

        final String graphFile = context.getProperties().getString(Tokens.REXSTER_GRAPH_LOCATION);

        if (graphFile == null || graphFile.length() == 0) {
            throw new GraphConfigurationException("Check graph configuration. Missing or empty configuration element: " + Tokens.REXSTER_GRAPH_LOCATION);
        }

        final boolean highAvailabilityMode = context.getProperties().getBoolean(Tokens.REXSTER_GRAPH_HA, false);

        // get the <properties> section of the xml configuration
        final HierarchicalConfiguration graphSectionConfig = (HierarchicalConfiguration) context.getProperties();
        SubnodeConfiguration neo4jSpecificConfiguration;

        try {
            neo4jSpecificConfiguration = graphSectionConfig.configurationAt(Tokens.REXSTER_GRAPH_PROPERTIES);
        } catch (IllegalArgumentException iae) {
            throw new GraphConfigurationException("Check graph configuration. Missing or empty configuration element: " + Tokens.REXSTER_GRAPH_PROPERTIES);
        }

        try {

            // properties to initialize the neo4j instance.
            final HashMap<String, String> neo4jProperties = new HashMap<String, String>();

            // read the properties from the xml file and convert them to properties
            // to be injected into neo4j.
            final Iterator<String> neo4jSpecificConfigurationKeys = neo4jSpecificConfiguration.getKeys();
            while (neo4jSpecificConfigurationKeys.hasNext()) {
                String key = neo4jSpecificConfigurationKeys.next();

                // replace the ".." put in play by apache commons configuration.  that's expected behavior
                // due to parsing key names to xml.
                neo4jProperties.put(key.replace("..", "."), neo4jSpecificConfiguration.getString(key));
            }

            if (highAvailabilityMode) {
                if (!neo4jProperties.containsKey("ha.machine_id")) {
                    throw new GraphConfigurationException("Check graph configuration. Neo4j HA requires [ha.machine_id] in the <properties> of the configuration");
                }

                if (!neo4jProperties.containsKey("ha.server")) {
                    throw new GraphConfigurationException("Check graph configuration. Neo4j HA requires [ha.server] <properties> of the configuration");
                }

                if (!neo4jProperties.containsKey("ha.initial_hosts")) {
                    throw new GraphConfigurationException("Check graph configuration. Neo4j HA requires [ha.initial_hosts] <properties> of the configuration");
                }

                return new Neo4j2HaGraph(graphFile, neo4jProperties);

            } else {
                return new Neo4j2Graph(graphFile, neo4jProperties);
            }

        } catch (GraphConfigurationException gce) {
            throw gce;
        } catch (Exception ex) {
            throw new GraphConfigurationException(ex);
        }
    }

}
