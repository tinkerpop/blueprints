package com.tinkerpop.blueprints.impls.sail.impls;

import junit.framework.TestCase;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class LinkedDataSailGraphTest extends TestCase {

    public void testConstructLinkedDataSail() {
        new LinkedDataSailGraph(new MemoryStoreSailGraph());
    }
}
