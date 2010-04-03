package com.tinkerpop.blueprints.pgm.pipes.merges;

import com.tinkerpop.blueprints.BaseTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class RobinMergePipeTest extends BaseTest {

    public void testRobinMergePipe() {
        RobinMergePipe<String> pipe = new RobinMergePipe<String>();
        List<String> marko = Arrays.asList("marko", "antonio", "rodriguez");
        List<String> peter = Arrays.asList("peter", "neubauer");
        List<String> josh = Arrays.asList("joshua", "shinavier");
        pipe.setStarts(Arrays.asList(marko.iterator(), peter.iterator(), josh.iterator()).iterator());
        assertTrue(pipe.hasNext());
        int counter = 0;
        List<String> ends = new ArrayList<String>();
        while (pipe.hasNext()) {
            counter++;
            String name = pipe.next();
            //System.out.println(name);
            assertTrue(marko.contains(name) || peter.contains(name) || josh.contains(name));
            ends.add(name);
        }
        assertEquals(counter, 7);
        assertEquals(counter, ends.size());
        assertEquals(ends.get(0), "marko");
        assertEquals(ends.get(1), "peter");
        assertEquals(ends.get(2), "joshua");
        assertEquals(ends.get(3), "antonio");
        assertEquals(ends.get(4), "neubauer");
        assertEquals(ends.get(5), "shinavier");
        assertEquals(ends.get(6), "rodriguez");
    }

    public void testRobinMergePipeSomeEmpty() {
        RobinMergePipe<String> pipe = new RobinMergePipe<String>();
        List<String> marko = Arrays.asList("marko");
        List<String> peter = Arrays.asList();
        List<String> josh = Arrays.asList("joshua", "shinavier");
        pipe.setStarts(Arrays.asList(marko.iterator(), peter.iterator(), josh.iterator()).iterator());
        assertTrue(pipe.hasNext());
        int counter = 0;
        List<String> ends = new ArrayList<String>();
        while (pipe.hasNext()) {
            counter++;
            String name = pipe.next();
            assertTrue(marko.contains(name) || peter.contains(name) || josh.contains(name));
            ends.add(name);
        }
        assertEquals(counter, 3);
        assertEquals(counter, ends.size());
        assertEquals(ends.get(0), "marko");
        assertEquals(ends.get(1), "joshua");
        assertEquals(ends.get(2), "shinavier");
    }

    public void testRobinMergePipeEmpty() {
        RobinMergePipe<String> pipe = new RobinMergePipe<String>();
        List<String> marko = Arrays.asList();
        List<String> peter = Arrays.asList();
        List<String> josh = Arrays.asList();
        pipe.setStarts(Arrays.asList(marko.iterator(), peter.iterator(), josh.iterator()).iterator());
        assertFalse(pipe.hasNext());
        int counter = 0;
        while (pipe.hasNext()) {
            counter++;
            pipe.next();
        }
        assertEquals(counter, 0);
    }

    public void testRobinMergePipeSingle() {
        RobinMergePipe<String> pipe = new RobinMergePipe<String>();
        List<String> marko = Arrays.asList("marko", "antonio", "rodriguez");
        pipe.setStarts(Arrays.asList(marko.iterator()).iterator());
        assertTrue(pipe.hasNext());
        int counter = 0;
        List<String> ends = new ArrayList<String>();
        while (pipe.hasNext()) {
            counter++;
            String name = pipe.next();
            assertTrue(marko.contains(name));
            ends.add(name);
        }
        assertEquals(counter, 3);
        assertEquals(counter, ends.size());
        assertEquals(ends.get(0), "marko");
        assertEquals(ends.get(1), "antonio");
        assertEquals(ends.get(2), "rodriguez");
    }

    public void testRobinMergePipeSingleEmpty() {
        RobinMergePipe<String> pipe = new RobinMergePipe<String>();
        List<String> marko = Arrays.asList();
        pipe.setStarts(Arrays.asList(marko.iterator()).iterator());
        assertFalse(pipe.hasNext());
        int counter = 0;
        while (pipe.hasNext()) {
            counter++;
            pipe.next();

        }
        assertEquals(counter, 0);
    }


}
