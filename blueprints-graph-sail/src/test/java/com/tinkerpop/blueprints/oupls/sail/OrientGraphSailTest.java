package com.tinkerpop.blueprints.oupls.sail;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class OrientGraphSailTest extends GraphSailTest {

    /*
    The following code properly opens/closes the database.
    UNCOMMENT once the following is resolved: GraphSail is using a period in the key name of a property,
    specifically "default.namespace".  OrientDB does not allow a period in the key name.  If that can be changed
    the tests will pass.  For example, if changed to "default-namespace" all tests across all graphs pass.
    */
    public KeyIndexableGraph createGraph() {
        String directory = getWorkingDirectory();

        OGraphDatabase db = new OGraphDatabase("local:" + directory + "/graph");

        if (db.exists())
            db.open("admin", "admin").drop();

        return new OrientGraph("local:" + directory + "/graph");
    }

    private String getWorkingDirectory() {
        return this.computeTestDataRoot().getAbsolutePath();
    }
}
