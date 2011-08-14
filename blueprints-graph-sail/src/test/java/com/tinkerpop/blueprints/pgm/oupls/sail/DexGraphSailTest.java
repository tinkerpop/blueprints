package com.tinkerpop.blueprints.pgm.oupls.sail;

import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.impls.dex.DexGraph;
import junit.framework.TestCase;

import java.io.File;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class DexGraphSailTest extends TestCase {//extends GraphSailTest {

    public void testTrue() {
        assertTrue(true);
    }

    protected IndexableGraph createGraph() {
        File directory = new File(getWorkingDirectory());
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File f = new File(directory, "data");
        if (f.exists()) {
            f.delete();
        }

        DexGraph g = new DexGraph(f.getAbsolutePath());
        g.clear();
        return g;
    }

    private String getWorkingDirectory() {
        return System.getProperty("os.name").toUpperCase().contains("WINDOWS") ? "C:/temp/blueprints_test/graphsail/dexgraph" : "/tmp/blueprints_test/graphsail/dexgraph";
    }
}
