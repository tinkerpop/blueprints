package com.tinkerpop.blueprints.oupls.sail;

import org.openrdf.sail.Sail;
import org.openrdf.sail.memory.MemoryStore;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class MemoryStoreTest extends SailTest {
    @Override
    protected void before() throws Exception {
        // Nothing to do.
    }

    @Override
    protected void after() throws Exception {
        // Nothing to do.
    }

    @Override
    protected Sail createSail() throws Exception {
        return new MemoryStore();
    }
}
