package com.tinkerpop.blueprints.pgm.pipes.splits;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.pgm.pipes.PipeHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ReadySplitPipeTest extends BaseTest {

    public void testReadySplitPipe() {
        SplitPipe<String> pipe = new ReadySplitPipe<String>(3);
        List<String> starts = BaseTest.generateUUIDs(12);
        pipe.setStarts(starts.iterator());
        List<String> end1 = new ArrayList<String>();
        List<String> end2 = new ArrayList<String>();
        List<String> end3 = new ArrayList<String>();

        assertTrue(pipe.hasNext());
        assertTrue(pipe.getSplit(0).hasNext());
        //assertTrue(pipe.getSplit(1).hasNext());
        //assertTrue(pipe.getSplit(2).hasNext());
        PipeHelper.fillCollection(pipe.getSplit(0), end1);
        PipeHelper.fillCollection(pipe.getSplit(1), end2);
        PipeHelper.fillCollection(pipe.getSplit(2), end3);

        assertEquals(end1.size(), 12);
        assertEquals(end2.size(), 0);
        assertEquals(end3.size(), 0);

        for (int i = 0; i < starts.size(); i++) {
            assertEquals(end1.get(i), starts.get(i));
        }
    }
}
