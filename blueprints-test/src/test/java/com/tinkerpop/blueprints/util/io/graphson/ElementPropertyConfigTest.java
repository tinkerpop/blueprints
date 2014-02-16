package com.tinkerpop.blueprints.util.io.graphson;

import junit.framework.Assert;
import org.junit.Test;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class ElementPropertyConfigTest {
    @Test
    public void shouldExcludeBoth() {
        final ElementPropertyConfig config = ElementPropertyConfig.excludeProperties(null, null);
        Assert.assertEquals(ElementPropertyConfig.ElementPropertiesRule.EXCLUDE, config.getVertexPropertiesRule());
        Assert.assertEquals(ElementPropertyConfig.ElementPropertiesRule.EXCLUDE, config.getEdgePropertiesRule());
    }

    @Test
    public void shouldIncludeBoth() {
        final ElementPropertyConfig config = ElementPropertyConfig.includeProperties(null, null);
        Assert.assertEquals(ElementPropertyConfig.ElementPropertiesRule.INCLUDE, config.getVertexPropertiesRule());
        Assert.assertEquals(ElementPropertyConfig.ElementPropertiesRule.INCLUDE, config.getEdgePropertiesRule());
    }
}
