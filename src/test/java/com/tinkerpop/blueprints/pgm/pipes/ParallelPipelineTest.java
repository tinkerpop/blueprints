package com.tinkerpop.blueprints.pgm.pipes;

import com.tinkerpop.blueprints.BaseTest;

import java.util.Arrays;
import java.util.List;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ParallelPipelineTest extends BaseTest {

    public void testParallelPipeLine() {
        int totalStarts = 200;
        int timeWaste = 10;
        List<String> starts1 = generateUUIDs(totalStarts);
        this.stopWatch();
        Pipe<String, String> pipe1 = new WasteTimePipe(timeWaste);
        Pipe<String, String> pipe2 = new WasteTimePipe(timeWaste);
        Pipe<String, String> pipe3 = new WasteTimePipe(timeWaste);
        ParallelPipeline<String,String> pipeline = new ParallelPipeline<String,String>(Arrays.asList(pipe1,pipe2,pipe3));
        pipeline.setStarts(starts1.iterator());
        int counter = 0;
        while(pipeline.hasNext()) {
            counter++;
            //System.out.println(pipeline.next());
            pipeline.next();
        }
        BaseTest.printPerformance("Parallel pipeline", totalStarts, "elements processed", this.stopWatch());
        assertEquals(counter, totalStarts);

        this.stopWatch();
        pipe1 = new WasteTimePipe(timeWaste);
        pipe1.setStarts(starts1.iterator());
        counter = 0;
        while(pipe1.hasNext()) {
            counter++;
            //System.out.println(pipe1.next());
            pipe1.next();
        }
        BaseTest.printPerformance("Serial pipeline", totalStarts, "elements processed", this.stopWatch());
        assertEquals(counter, totalStarts);
    }
}
