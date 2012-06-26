package com.tinkerpop.blueprints.oupls.sail;


import junit.framework.TestCase;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class OrientGraphSailTest extends TestCase {//extends GraphSailTest {

    public void testTrue() {
        assertTrue(true);
    }

    /*
    protected KeyIndexableGraph createGraph() {

        String directory = getWorkingDirectory();

        new ODatabaseGraphTx("local:" + directory + "/graph").delete();


        OrientGraph g = new OrientGraph("local:" + directory + "/graph");
        g.setMaxBufferSize(0);
        return g;
    }

    private String getWorkingDirectory() {
        return System.getProperty("os.name").toUpperCase().contains("WINDOWS") ? "C:/temp/blueprints_test/graphsail/orientgraph" : "/tmp/blueprints_test/graphsail/orientgraph";
    }

    public static void main(final String[] args) throws Exception {
        new OrientGraphSailTest().doTest();
    }

    private void doTest() throws Exception {
        for (int i = 0; i < 2; i++) {
            System.out.println("i = " + i);
            IndexableGraph g = createGraph();
            Sail sail = new GraphSail(g);
            sail.initialize();

            SailConnection c = sail.getConnection();
            c.commit();
            try {
                c.addStatement(RDFS.LABEL, RDFS.LABEL, new LiteralImpl("label"));
                c.commit();
            } finally {
                c.close();
            }

            Repository repo = new SailRepository(sail);
            RepositoryConnection rc = repo.getConnection();
            rc.setAutoCommit(false);
            try {
                rc.add(SailTest.class.getResource("graph-example-sail-test.trig"), "", RDFFormat.TRIG);
            } finally {
                rc.close();
            }

            sail.shutDown();
            //g.shutdown();
        }
    }
    */
}
