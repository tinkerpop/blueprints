package com.tinkerpop.blueprints.oupls.sail;

import org.junit.Test;

import static junit.framework.Assert.assertTrue;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class DexGraphSailTest {//} extends GraphSailTest {

    @Test
    public void testTrue() {
        assertTrue(true);
    }

    /*
    protected KeyIndexableGraph createGraph() throws IOException {
        File dir = File.createTempFile("blueprints", "-dex-test");
        String path = dir.getPath();
        dir.delete();
        //dir.mkdir();

        DexGraph g = new DexGraph(path);
        //g.clear();
        return g;
    }

    private String getWorkingDirectory() {
        return System.getProperty("os.name").toUpperCase().contains("WINDOWS") ? "C:/temp/blueprints_test/graphsail/dexgraph" : "/tmp/blueprints_test/graphsail/dexgraph";
    }
    */
}
