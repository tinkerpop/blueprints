package com.tinkerpop.blueprints.pgm.pipes;

import com.tinkerpop.blueprints.BaseTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class TimingTest extends BaseTest {

    public void testForLoopVsIterator() {
        int numberOfStarts = 100000;
        //warmUp(numberOfStarts);
        List<String> uuids = new ArrayList<String>();
        for (int i = 0; i < numberOfStarts; i++) {
            uuids.add(UUID.randomUUID().toString());
        }

        // SINGLE FOR-LOOP MODEL
        this.stopWatch();
        List<String> tempUUIDs = new ArrayList<String>();
        for (String uuid : uuids) {
            tempUUIDs.add(uuid.toUpperCase().toLowerCase().toUpperCase().toLowerCase());
        }
        printPerformance("Pipes", numberOfStarts, "single for-loop 4 operations", this.stopWatch());
        assertEquals(tempUUIDs.size(), uuids.size());

        // MULTI FOR-LOOP MODEL
        this.stopWatch();
        tempUUIDs = new ArrayList<String>();
        for (String uuid : uuids) {
            tempUUIDs.add(uuid.toUpperCase());
        }
        List<String> tempUUIDs2 = new ArrayList<String>();
        for (String uuid : tempUUIDs) {
            tempUUIDs2.add(uuid.toLowerCase());
        }
        tempUUIDs.clear();
        for (String uuid : tempUUIDs2) {
            tempUUIDs.add(uuid.toUpperCase());
        }
        tempUUIDs2.clear();
        for (String uuid : tempUUIDs) {
            tempUUIDs2.add(uuid.toLowerCase());
        }
        printPerformance("Pipes", numberOfStarts, "multi for-loop 4 operations", this.stopWatch());
        assertEquals(tempUUIDs2.size(), uuids.size());


        // PIPE MODEL
        this.stopWatch();
        tempUUIDs = new ArrayList<String>();
        Pipe pipe1 = new UpperPipe();
        Pipe pipe2 = new LowerPipe();
        Pipe pipe3 = new UpperPipe();
        Pipe pipe4 = new LowerPipe();
        Pipeline<String, String> pipeline = new Pipeline<String, String>(Arrays.asList(pipe1, pipe2, pipe3, pipe4));
        pipeline.setStarts(uuids.iterator());
        int counter = 0;
        while (pipeline.hasNext()) {
            tempUUIDs.add(pipeline.next());
            counter++;
        }
        printPerformance("Pipes", numberOfStarts, "pipes 4 operations", this.stopWatch());
        assertEquals(counter, uuids.size());
    }


    private class UpperPipe extends AbstractPipe<String, String> {
        protected String processNextStart() {
            return this.starts.next().toUpperCase();

        }
    }

    private class LowerPipe extends AbstractPipe<String, String> {
        protected String processNextStart() {
            return this.starts.next().toLowerCase();
        }
    }
}
