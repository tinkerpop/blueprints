package com.tinkerpop.blueprints.pgm.pipes;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class AggregatorPipeTest extends TestCase {

    public void testAggregatorPipe() {
        List<String> list = Arrays.asList("marko", "antonio", "rodriguez", "was", "here", ".");
        AggregatorPipe<String> pipe1 = new AggregatorPipe<String>(new ArrayList<String>());
        pipe1.setStarts(list.iterator());
        assertTrue(pipe1.hasNext());
        int counter = 0;
        while (pipe1.hasNext()) {
            assertEquals(pipe1.next(), list.get(counter));
            counter++;
        }
        assertEquals(counter, 6);
        assertEquals(pipe1.getSideEffect().size(), counter);
        assertEquals(list.size(), counter);
        for (int i = 0; i < counter; i++) {
            assertEquals(list.get(i), pipe1.getSideEffect().toArray()[i]);
        }
    }

    public void testSelfFilter() {
        List<String> list = Arrays.asList("marko", "antonio", "rodriguez", "was", "here", ".");
        AggregatorPipe<String> pipe1 = new AggregatorPipe<String>(new ArrayList<String>());
        Pipe pipe2 = new ObjectFilterPipe<String>(pipe1.getSideEffect(), false);
        Pipeline<String,String> pipeline = new Pipeline<String,String>(Arrays.asList(pipe1,pipe2));
        pipeline.setStarts(list.iterator());
        int counter = 0;
        assertTrue(pipeline.hasNext());
        while(pipeline.hasNext()) {
            pipeline.next();
            counter++;
        }
        assertEquals(counter,6);

        pipe1 = new AggregatorPipe<String>(new ArrayList<String>());
        pipe2 = new ObjectFilterPipe<String>(pipe1.getSideEffect(), true);
        pipeline = new Pipeline<String,String>(Arrays.asList(pipe1,pipe2));
        pipeline.setStarts(list.iterator());
        counter = 0;
        assertFalse(pipeline.hasNext());
        while(pipeline.hasNext()) {
            pipeline.next();
            counter++;
        }
        assertEquals(counter,0);


    }
}
