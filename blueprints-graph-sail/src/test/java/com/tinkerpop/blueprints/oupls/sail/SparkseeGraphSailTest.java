package com.tinkerpop.blueprints.oupls.sail;

import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.impls.sparksee.SparkseeGraph;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static junit.framework.Assert.assertTrue;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class SparkseeGraphSailTest {//extends GraphSailTest {

    @Test
    public void testTrue() {
        assertTrue(true);
    }

    protected KeyIndexableGraph createGraph() throws IOException {
        /*
        boolean create = true;
        String db = this.computeTestDataRoot() + "/blueprints_test.sparksee";

        if (create) {
            File f = new File(db);
            if (f.exists()) f.delete();
        }
        return new SparkseeGraph(db);
        */


        File dir = File.createTempFile("blueprints", "-sparksee-test");
        String path = dir.getPath();
        dir.delete();
        //dir.mkdir();

        SparkseeGraph g = new SparkseeGraph(path);
        //g.label.set("thing");
        //g.clear();
        return g;
        //*/
    }

    private String getWorkingDirectory() {
        return System.getProperty("os.name").toUpperCase().contains("WINDOWS") ? "C:/temp/blueprints_test/graphsail/sparkseegraph" : "/tmp/blueprints_test/graphsail/sparkseegraph";
    }
}
