package com.tinkerpop.blueprints;

import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import junit.framework.TestCase;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class GraphFactoryTest extends TestCase {

    public void testOpenInMemoryTinkerGraphViaApacheConfig(){
        final Configuration conf = new BaseConfiguration();
        conf.setProperty("blueprints.graph", "com.tinkerpop.blueprints.impls.tg.TinkerGraph");
        final Graph g = GraphFactory.open(conf);

        assertNotNull(g);
        assertTrue(g instanceof TinkerGraph);
    }

    public void testOpenInMemoryTinkerGraphViaMap(){
        final Map<String,Object> conf = new HashMap<String,Object>();
        conf.put("blueprints.graph", "com.tinkerpop.blueprints.impls.tg.TinkerGraph");
        final Graph g = GraphFactory.open(conf);

        assertNotNull(g);
        assertTrue(g instanceof TinkerGraph);
    }
}
