package com.tinkerpop.blueprints;

import junit.framework.TestCase;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class GraphFactoryTest extends TestCase {

    public void testOpenInMemoryTinkerGraph(){
        final Configuration conf = new BaseConfiguration();
        conf.setProperty("blueprints.graph", "com.tinkerpop.blueprints.impls.tg.TinkerGraph");
        final Graph g = GraphFactory.open(conf);

        assertNotNull(g);
    }
}
