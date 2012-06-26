package com.tinkerpop.blueprints.oupls.sail;

import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.impls.dex.DexGraph;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static junit.framework.Assert.assertTrue;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class DexGraphSailTest {//extends GraphSailTest {

    @Test
    public void testTrue() {
        assertTrue(true);
    }

    protected KeyIndexableGraph createGraph() throws IOException {
        /*
        boolean create = true;
        String db = this.computeTestDataRoot() + "/blueprints_test.dex";

        if (create) {
            File f = new File(db);
            if (f.exists()) f.delete();
        }
        return new DexGraph(db);
        */


        File dir = File.createTempFile("blueprints", "-dex-test");
        String path = dir.getPath();
        dir.delete();
        //dir.mkdir();

        DexGraph g = new DexGraph(path);
        //g.label.set("thing");
        //g.clear();
        return g;
        //*/
    }

    private String getWorkingDirectory() {
        return System.getProperty("os.name").toUpperCase().contains("WINDOWS") ? "C:/temp/blueprints_test/graphsail/dexgraph" : "/tmp/blueprints_test/graphsail/dexgraph";
    }
}
