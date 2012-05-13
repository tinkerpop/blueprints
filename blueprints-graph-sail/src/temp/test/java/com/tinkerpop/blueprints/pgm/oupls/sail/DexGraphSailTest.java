package com.tinkerpop.blueprints.pgm.oupls.sail;

import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.impls.dex.DexGraph;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static junit.framework.Assert.assertTrue;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class DexGraphSailTest {//} extends GraphSailTest {

    @Test
    public void testTrue() {
        assertTrue(true);
    }

    protected IndexableGraph createGraph() throws IOException {
        /*
        File directory = new File(getWorkingDirectory());
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File f = new File(directory, "data");
        if (f.exists()) {
            f.delete();
        }*/

        File dir = File.createTempFile("blueprints", "-dex-test");
        String path = dir.getPath();
        dir.delete();
        //dir.mkdir();

        DexGraph g = new DexGraph(path);
        g.clear();
        return g;
    }

    private String getWorkingDirectory() {
        return System.getProperty("os.name").toUpperCase().contains("WINDOWS") ? "C:/temp/blueprints_test/graphsail/dexgraph" : "/tmp/blueprints_test/graphsail/dexgraph";
    }
}
