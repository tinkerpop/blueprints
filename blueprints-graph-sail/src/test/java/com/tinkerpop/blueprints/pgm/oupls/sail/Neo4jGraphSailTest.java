package com.tinkerpop.blueprints.pgm.oupls.sail;

import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class Neo4jGraphSailTest extends GraphSailTest {
    protected IndexableGraph createGraph() {
        String directory = this.getWorkingDirectory();

        Neo4jGraph g = new Neo4jGraph(directory);
        g.clear();
        g.setMaxBufferSize(0);
        return g;
    }

    private String getWorkingDirectory() {
        return System.getProperty("os.name").toUpperCase().contains("WINDOWS") ? "C:/temp/blueprints_test/graphsail/neo4jgraph" : "/tmp/blueprints_test/graphsail/neo4jgraph";
    }
}
