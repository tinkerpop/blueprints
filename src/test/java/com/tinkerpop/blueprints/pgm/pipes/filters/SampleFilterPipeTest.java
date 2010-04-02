package com.tinkerpop.blueprints.pgm.pipes.filters;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.pgm.pipes.Pipe;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class SampleFilterPipeTest extends BaseTest {

    public void testSampleFilterPipe() {
        Pipe<String,String> pipe = new SampleFilterPipe<String>(1.0d);
        pipe.setStarts(generateUUIDs(100).iterator());
        int counter = 0;
        while(pipe.hasNext()) {
            counter++;
            pipe.next();
        }
        assertEquals(counter, 100);

        pipe = new SampleFilterPipe<String>(0.0d);
        pipe.setStarts(generateUUIDs(100).iterator());
        counter = 0;
        while(pipe.hasNext()) {
            counter++;
            pipe.next();
        }
        assertEquals(counter, 0);
    }

    public void testSampleFilterPipe5050() {
        Pipe<String,String> pipe = new SampleFilterPipe<String>(0.5d);
        pipe.setStarts(generateUUIDs(1000).iterator());
        int counter = 0;
        while(pipe.hasNext()) {
            counter++;
            pipe.next();
        }
        //System.out.println(counter);
        assertTrue(counter > 400 && counter < 600);
    }
}
