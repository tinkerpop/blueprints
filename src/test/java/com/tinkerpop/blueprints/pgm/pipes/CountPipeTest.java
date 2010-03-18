package com.tinkerpop.blueprints.pgm.pipes;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class CountPipeTest extends TestCase {

    public void testCountPipe() {
        List<String> list = Arrays.asList("marko", "antonio", "rodriguez", "was", "here", ".");
        CountPipe<String> pipe1 = new CountPipe<String>();
        pipe1.setStarts(list.iterator());
        assertTrue(pipe1.hasNext());
        assertTrue(pipe1.hasNext());
        assertEquals(pipe1.next(), new Integer(6));
        assertFalse(pipe1.hasNext());
        assertNull(pipe1.next());
    }


    public void testCountPipeEmpty() {
        List<String> list = Arrays.asList();
        CountPipe<String> pipe1 = new CountPipe<String>();
        pipe1.setStarts(list.iterator());
        assertTrue(pipe1.hasNext());
        assertTrue(pipe1.hasNext());
        assertEquals(pipe1.next(), new Integer(0));
        assertFalse(pipe1.hasNext());
        assertNull(pipe1.next());
    }
}
