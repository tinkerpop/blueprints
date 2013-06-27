package com.tinkerpop.blueprints.util;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.Compare;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class CompareTest extends BaseTest {
    public void testQueryCompareOpposite() {
        assertEquals(Compare.EQUAL, Compare.NOT_EQUAL.opposite());
        assertEquals(Compare.NOT_EQUAL, Compare.EQUAL.opposite());
        assertEquals(Compare.LESS_THAN_EQUAL, Compare.GREATER_THAN.opposite());
        assertEquals(Compare.GREATER_THAN_EQUAL, Compare.LESS_THAN.opposite());
        assertEquals(Compare.LESS_THAN, Compare.GREATER_THAN_EQUAL.opposite());
        assertEquals(Compare.GREATER_THAN, Compare.LESS_THAN_EQUAL.opposite());
    }

    public void testQueryCompareAsString() {
        assertEquals("=", Compare.EQUAL.asString());
        assertEquals("<>", Compare.NOT_EQUAL.asString());
        assertEquals("<", Compare.LESS_THAN.asString());
        assertEquals(">=", Compare.GREATER_THAN_EQUAL.asString());
        assertEquals("<=", Compare.LESS_THAN_EQUAL.asString());
        assertEquals(">", Compare.GREATER_THAN.asString());
    }

    public void testQueryCompareFromString() {
        assertEquals(Compare.EQUAL, Compare.fromString("="));
        assertEquals(Compare.NOT_EQUAL, Compare.fromString("<>"));
        assertEquals(Compare.LESS_THAN, Compare.fromString("<"));
        assertEquals(Compare.GREATER_THAN_EQUAL, Compare.fromString(">="));
        assertEquals(Compare.LESS_THAN_EQUAL, Compare.fromString("<="));
        assertEquals(Compare.GREATER_THAN, Compare.fromString(">"));
    }
}
