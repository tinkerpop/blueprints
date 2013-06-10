package com.tinkerpop.blueprints;

import junit.framework.TestCase;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ParameterTest extends TestCase {

    public void testEquality() {
        Parameter<String, Long> a = new Parameter<String, Long>("blah", 7l);
        Parameter<String, Long> b = new Parameter<String, Long>("blah", 7l);

        assertEquals(a, a);
        assertEquals(b, b);
        assertEquals(a, b);

        Parameter<String, Long> c = new Parameter<String, Long>("blah", 6l);

        assertNotSame(a, c);
        assertNotSame(b, c);

        Parameter<String, Long> d = new Parameter<String, Long>("boop", 7l);

        assertNotSame(a, d);
        assertNotSame(b, d);
        assertNotSame(c, d);

    }
}
