package com.tinkerpop.blueprints.pgm.pipes;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.pgm.pipes.AbstractPipe;
import com.tinkerpop.blueprints.pgm.pipes.Pipeline;

import java.util.*;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ThreadedPipeTest extends BaseTest {

    public void testThreadedPipe() {
        List<String> starts = new ArrayList<String>();
        for (int i = 0; i < 5000; i++) {
            starts.add(i + "-" + UUID.randomUUID().toString());
        }
        Pipe pipe1 = new UpperPipe();
        Pipe pipe2 = new LowerPipe();
        Pipe pipe3 = new UpperPipe();
        Pipe pipe4 = new LowerPipe();
        Pipeline<String, String> pipeline = new Pipeline<String, String>(Arrays.asList(pipe1, pipe2, pipe3, pipe4));
        pipeline.setStarts(starts.iterator());
        /*List<String> ends = new ArrayList<String>();
        this.stopWatch();
        ThreadedPipe<String> threadedPipe = new ThreadedPipe<String>(5);
        threadedPipe.setStarts(pipeline);
        while(threadedPipe.hasNext()) {
            String uuid = threadedPipe.next();
            //System.out.print(".");
            System.out.println(uuid);
            ends.add(uuid);
        }
        while (pipeline.hasNext()) {
            String uuid = pipeline.next();
            System.out.print(".");
            //System.out.println(uuid);
            ends.add(uuid);
        }
        System.out.println("\n" + this.stopWatch() + "ms");
        assertTrue(!ends.contains(null));
        assertEquals(starts.size(), new HashSet(ends).size());
        for (String uuid : ends) {
            assertTrue(starts.contains(uuid));
        }*/

    }

    private class UpperPipe extends AbstractPipe<String, String> {
        protected void setNext() {
            if (this.starts.hasNext()) {
                this.nextEnd = this.starts.next().toUpperCase();
                int counter = 0;
                while (counter < 5000) {
                    counter++;
                    new String("marko").toUpperCase();
                }
            } else {
                this.done = true;
            }
        }
    }

    private class LowerPipe extends AbstractPipe<String, String> {
        protected void setNext() {
            if (this.starts.hasNext()) {
                this.nextEnd = this.starts.next().toLowerCase();
                int counter = 0;
                while (counter < 5000) {
                    counter++;
                    new String("marko").toUpperCase();
                }
            } else {
                this.done = true;
            }
        }
    }
}
