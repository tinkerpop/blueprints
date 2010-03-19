package com.tinkerpop.blueprints.pgm.pipes;

import com.tinkerpop.blueprints.BaseTest;
import junit.framework.TestCase;

import java.util.*;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class TimingTest extends BaseTest {

    public void testForLoopVsIterator() {
        int numberOfStarts = 200000;
        warmUp(numberOfStarts);
        List<String> uuids = new ArrayList<String>();
        for (int i = 0; i < numberOfStarts; i++) {
            uuids.add(UUID.randomUUID().toString());
        }

        this.stopWatch();
        List<String> tempUUIDs = new ArrayList<String>();
        for (String uuid : uuids) {
            tempUUIDs.add(uuid.toUpperCase());
        }
        List<String> tempUUIDs2 = new ArrayList<String>();
        for (String uuid : tempUUIDs) {
            tempUUIDs2.add(uuid.toUpperCase());
        }
        tempUUIDs.clear();
        for (String uuid : tempUUIDs2) {
            tempUUIDs.add(uuid.toUpperCase());
        }
        tempUUIDs2.clear();
        for (String uuid : tempUUIDs) {
            tempUUIDs2.add(uuid.toUpperCase());
        }
        printPerformance("Pipes", numberOfStarts, "strings for-looped 4 times", this.stopWatch());
        assertEquals(tempUUIDs2.size(), uuids.size());

        this.stopWatch();
        Pipe pipe1 = new CapitalizePipe();
        Pipe pipe2 = new CapitalizePipe();
        Pipe pipe3 = new CapitalizePipe();
        Pipe pipe4 = new CapitalizePipe();
        Pipeline<String, String> pipeline = new Pipeline<String, String>(Arrays.asList(pipe1, pipe2, pipe3, pipe4));
        pipeline.setStarts(uuids.iterator());
        int counter = 0;
        while (pipeline.hasNext()) {
            pipeline.next();
            counter++;
        }
        printPerformance("Pipes", numberOfStarts, "strings pipes 4 times", this.stopWatch());
        assertEquals(counter, uuids.size());
    }


    private class CapitalizePipe extends AbstractPipe<String, String> {
        protected void setNext() {
            if (this.starts.hasNext()) {
                this.nextEnd = this.starts.next().toUpperCase();
            } else {
                this.nextEnd = null;
            }
        }
    }
}
