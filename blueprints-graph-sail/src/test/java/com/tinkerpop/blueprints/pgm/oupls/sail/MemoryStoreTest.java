package com.tinkerpop.blueprints.pgm.oupls.sail;

import org.openrdf.sail.Sail;
import org.openrdf.sail.memory.MemoryStore;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class MemoryStoreTest extends SailTest {
    @Override
    protected void before() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void after() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected Sail createSail() throws Exception {
        return new MemoryStore();
    }
}
