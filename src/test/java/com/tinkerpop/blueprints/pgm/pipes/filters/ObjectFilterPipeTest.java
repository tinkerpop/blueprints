package com.tinkerpop.blueprints.pgm.pipes.filters;

import com.tinkerpop.blueprints.pgm.pipes.Pipe;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ObjectFilterPipeTest extends TestCase {

    public void testObjectFilter() {
        List<String> starts = Arrays.asList("marko", "josh", "peter");
        List<String> filters = Arrays.asList("marko");
        Pipe<String, String> ofp = new ObjectFilterPipe<String>(filters, true);
        ofp.setStarts(starts.iterator());
        assertTrue(ofp.hasNext());
        int counter = 0;
        while (ofp.hasNext()) {
            String next = ofp.next();
            assertTrue(next.equals("josh") || next.equals("peter"));
            counter++;
        }
        assertEquals(counter, 2);

        ofp = new ObjectFilterPipe<String>(filters, false);
        ofp.setStarts(starts.iterator());
        assertTrue(ofp.hasNext());
        counter = 0;
        while (ofp.hasNext()) {
            String next = ofp.next();
            assertTrue(next.equals("marko"));
            counter++;
        }
        assertEquals(counter, 1);
        try {
            ofp.next();
            assertTrue(false);
        } catch (NoSuchElementException e) {
            assertFalse(false);
        }
    }

}
