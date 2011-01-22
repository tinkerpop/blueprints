package com.tinkerpop.blueprints.pgm.oupls.sail;

import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.impls.orientdb.OrientGraph;
import junit.framework.TestCase;

/**
 * User: josh
 * Date: 1/18/11
 * Time: 10:54 AM
 */
public class OrientGraphSailTest extends TestCase {

    public void testTrue() {
        assertTrue(true);
    }

    protected IndexableGraph createGraph() {

        String directory = getWorkingDirectory();
        OrientGraph g = new OrientGraph("local:" + directory + "/graph");
        g.setTransactionMode(TransactionalGraph.Mode.MANUAL);
        return g;
    }

    private String getWorkingDirectory() {
        return System.getProperty("os.name").toUpperCase().contains("WINDOWS") ? "C:/temp/blueprints_test/graphsail/orientgraph" : "/tmp/blueprints_test/graphsail/orientgraph";
    }

    /*
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
                rc.add(SailTest.class.getResource("sailTest.trig"), "", RDFFormat.TRIG);
            } finally {
                rc.close();
            }

            sail.shutDown();
            //g.shutdown();
        }
    }
    */
}
