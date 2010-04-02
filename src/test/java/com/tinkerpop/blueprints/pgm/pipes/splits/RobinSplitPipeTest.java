package com.tinkerpop.blueprints.pgm.pipes.splits;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.pgm.pipes.PipeHelper;
import com.tinkerpop.blueprints.pgm.pipes.splits.RobinSplitPipe;
import com.tinkerpop.blueprints.pgm.pipes.splits.SplitPipe;

import java.util.ArrayList;
import java.util.List;


/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class RobinSplitPipeTest extends BaseTest {

    public void testRobinSplitPipeOrdering() {
        SplitPipe<String> pipe = new RobinSplitPipe<String>(3);
        List<String> starts = BaseTest.generateUUIDs(12);
        pipe.setStarts(starts.iterator());
        List<String> end1 = new ArrayList<String>();
        List<String> end2 = new ArrayList<String>();
        List<String> end3 = new ArrayList<String>();

        PipeHelper.fillCollection(pipe.getSplit(0), end1);
        PipeHelper.fillCollection(pipe.getSplit(1), end2);
        PipeHelper.fillCollection(pipe.getSplit(2), end3);

        assertEquals(end1.size(), 4);
        assertEquals(end2.size(), 4);
        assertEquals(end3.size(), 4);

        assertEquals(end1.get(0), starts.get(0));
        assertEquals(end1.get(1), starts.get(3));
        assertEquals(end1.get(2), starts.get(6));
        assertEquals(end1.get(3), starts.get(9));
        assertEquals(end2.get(0), starts.get(1));
        assertEquals(end2.get(1), starts.get(4));
        assertEquals(end2.get(2), starts.get(7));
        assertEquals(end2.get(3), starts.get(10));
        assertEquals(end3.get(0), starts.get(2));
        assertEquals(end3.get(1), starts.get(5));
        assertEquals(end3.get(2), starts.get(8));
        assertEquals(end3.get(3), starts.get(11));
    }

    public void testRobinSplitPipeSize() {
        SplitPipe<String> pipe = new RobinSplitPipe<String>(5);
        List<String> starts = BaseTest.generateUUIDs(101);
        pipe.setStarts(starts.iterator());
        List<String> end1 = new ArrayList<String>();
        List<String> end2 = new ArrayList<String>();
        List<String> end3 = new ArrayList<String>();
        List<String> end4 = new ArrayList<String>();
        List<String> end5 = new ArrayList<String>();

        PipeHelper.fillCollection(pipe.getSplit(0), end1);
        PipeHelper.fillCollection(pipe.getSplit(1), end2);
        PipeHelper.fillCollection(pipe.getSplit(2), end3);
        PipeHelper.fillCollection(pipe.getSplit(3), end4);
        PipeHelper.fillCollection(pipe.getSplit(4), end5);

        assertEquals(end1.size(), 21);
        assertEquals(end2.size(), 20);
        assertEquals(end3.size(), 20);
        assertEquals(end4.size(), 20);
        assertEquals(end5.size(), 20);
    }

    public void testRobinSplitPipeSmall() {
        SplitPipe<String> pipe = new RobinSplitPipe<String>(3);
        List<String> starts = BaseTest.generateUUIDs(2);
        pipe.setStarts(starts.iterator());
        List<String> end1 = new ArrayList<String>();
        List<String> end2 = new ArrayList<String>();
        List<String> end3 = new ArrayList<String>();

        assertTrue(pipe.getSplit(0).hasNext());
        assertTrue(pipe.getSplit(1).hasNext());
        assertFalse(pipe.getSplit(2).hasNext());

        PipeHelper.fillCollection(pipe.getSplit(0), end1);
        PipeHelper.fillCollection(pipe.getSplit(1), end2);
        PipeHelper.fillCollection(pipe.getSplit(2), end3);

        assertEquals(end1.size(), 1);
        assertEquals(end2.size(), 1);
        assertEquals(end3.size(), 0);
    }

    public void testRobinSplitPipeEmpty() {
        SplitPipe<String> pipe = new RobinSplitPipe<String>(3);
        List<String> starts = BaseTest.generateUUIDs(0);
        pipe.setStarts(starts.iterator());
        List<String> end1 = new ArrayList<String>();
        List<String> end2 = new ArrayList<String>();
        List<String> end3 = new ArrayList<String>();

        assertFalse(pipe.getSplit(0).hasNext());
        assertFalse(pipe.getSplit(1).hasNext());
        assertFalse(pipe.getSplit(2).hasNext());

        PipeHelper.fillCollection(pipe.getSplit(0), end1);
        PipeHelper.fillCollection(pipe.getSplit(1), end2);
        PipeHelper.fillCollection(pipe.getSplit(2), end3);

        assertEquals(end1.size(), 0);
        assertEquals(end2.size(), 0);
        assertEquals(end3.size(), 0);

    }
}
