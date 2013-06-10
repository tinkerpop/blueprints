package com.tinkerpop.blueprints.util;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.Query;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class QueryTest extends BaseTest {
    public void testQueryCompareOpposite() {
        assertEquals(Query.Compare.EQUAL, Query.Compare.NOT_EQUAL.opposite());
        assertEquals(Query.Compare.NOT_EQUAL, Query.Compare.EQUAL.opposite());
        assertEquals(Query.Compare.LESS_THAN_EQUAL, Query.Compare.GREATER_THAN.opposite());
        assertEquals(Query.Compare.GREATER_THAN_EQUAL, Query.Compare.LESS_THAN.opposite());
        assertEquals(Query.Compare.LESS_THAN, Query.Compare.GREATER_THAN_EQUAL.opposite());
        assertEquals(Query.Compare.GREATER_THAN, Query.Compare.LESS_THAN_EQUAL.opposite());
    }

    public void testQueryCompareAsString() {
        assertEquals("=", Query.Compare.EQUAL.asString());
        assertEquals("<>", Query.Compare.NOT_EQUAL.asString());
        assertEquals("<", Query.Compare.LESS_THAN.asString());
        assertEquals(">=", Query.Compare.GREATER_THAN_EQUAL.asString());
        assertEquals("<=", Query.Compare.LESS_THAN_EQUAL.asString());
        assertEquals(">", Query.Compare.GREATER_THAN.asString());
    }

    public void testQueryCompareFromString() {
        assertEquals(Query.Compare.EQUAL, Query.Compare.fromString("="));
        assertEquals(Query.Compare.NOT_EQUAL, Query.Compare.fromString("<>"));
        assertEquals(Query.Compare.LESS_THAN, Query.Compare.fromString("<"));
        assertEquals(Query.Compare.GREATER_THAN_EQUAL, Query.Compare.fromString(">="));
        assertEquals(Query.Compare.LESS_THAN_EQUAL, Query.Compare.fromString("<="));
        assertEquals(Query.Compare.GREATER_THAN, Query.Compare.fromString(">"));
    }
}
