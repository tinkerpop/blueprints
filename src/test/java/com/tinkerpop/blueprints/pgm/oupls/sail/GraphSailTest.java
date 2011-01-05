package com.tinkerpop.blueprints.pgm.oupls.sail;

import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;
import org.openrdf.sail.Sail;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class GraphSailTest extends SailTest {
    public void testIndexPatterns() throws Exception {
        assertTriplePattern("spoc", true);
        assertTriplePattern("poc", true);
        assertTriplePattern("p", true);
        assertTriplePattern("", true);

        assertTriplePattern("xpoc", false);
        assertTriplePattern("sspo", false);
    }

    private void assertTriplePattern(final String pattern,
                                     final boolean isValid) {
        boolean m = GraphSail.INDEX_PATTERN.matcher(pattern).matches();
        assertTrue(isValid ? m : !m);
    }

    protected void before() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    protected void after() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    protected void clear() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    protected Sail createSail() throws Exception {
        TinkerGraph g = new TinkerGraph();
        return new GraphSail(g);
    }
}
