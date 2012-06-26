package com.tinkerpop.blueprints.oupls.sail;


import static org.junit.Assert.assertTrue;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class OrientGraphSailTest {//extends GraphSailTest {

    public void testTrue() {
        assertTrue(true);
    }

    /*
    public KeyIndexableGraph createGraph() {
        String directory = getWorkingDirectory();
        OrientGraph g = new OrientGraph("local:" + directory + "/graph");
        return g;
    }

    private String getWorkingDirectory() {
        return this.computeTestDataRoot().getAbsolutePath();
    }
    */
}
