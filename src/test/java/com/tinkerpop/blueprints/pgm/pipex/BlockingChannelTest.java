package com.tinkerpop.blueprints.pgm.pipex;

import com.tinkerpop.blueprints.pgm.pipex.util.ChannelReader;
import com.tinkerpop.blueprints.pgm.pipex.util.IdempotentProcess;
import junit.framework.TestCase;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class BlockingChannelTest extends TestCase {

    public void testEmptyCapacity() {
        try {
            new BlockingChannel<String>(0);
            assertFalse(true);
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    public void testCapacitySweep() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        int maxCapacity = 25;
        int maxContent = 100;

        for (int i = 1; i < maxCapacity; i++) {
            for (int j = 1; j < maxCapacity; j++) {
                SerialProcess<String, String> process = new IdempotentProcess<String>(new BlockingChannel<String>(i), new BlockingChannel<String>(j));
                ChannelReader reader = new ChannelReader(process.getOutChannel());
                executor.execute(reader);
                executor.execute(process);
                for (int k = 0; k < maxContent; k++) {
                    process.getInChannel().write(UUID.randomUUID().toString());
                }
                process.getInChannel().close();
                assertEquals(reader.getCounter(), maxContent);
            }
        }
        executor.shutdown();
    }

    public void testThreadSweep() {
        int maxCapacity = 10;
        int maxContent = 50;
        int maxThreads = 25;
        for (int i = 1; i < maxCapacity; i++) {
            for (int j = 2; j < maxThreads; j++) {
                ExecutorService executor = Executors.newFixedThreadPool(j);
                SerialProcess<String, String> process = new IdempotentProcess<String>(new BlockingChannel<String>(i), new BlockingChannel<String>(i));
                ChannelReader reader = new ChannelReader(process.getOutChannel());
                executor.execute(reader);
                executor.execute(process);
                for (int k = 0; k < maxContent; k++) {
                    process.getInChannel().write(UUID.randomUUID().toString());
                }
                process.getInChannel().close();
                assertEquals(reader.getCounter(), maxContent);
                executor.shutdown();

            }
        }
    }

    public void testMultipleReaders() {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        SerialProcess<String, String> process = new IdempotentProcess<String>(new BlockingChannel<String>(10), new BlockingChannel<String>(10));
        ChannelReader<String> reader1 = new ChannelReader<String>(process.getOutChannel());
        ChannelReader<String> reader2 = new ChannelReader<String>(process.getOutChannel());
        executor.execute(reader1);
        executor.execute(reader2);
        executor.execute(process);
        for (int k = 0; k < 100; k++) {
            process.getInChannel().write(UUID.randomUUID().toString());
        }
        process.getInChannel().close();
        assertEquals(reader1.getCounter() + reader2.getCounter(), 100);
        executor.shutdown();

    }

    public void testMultipleWriters() {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        //Channel<String> in = new BlockingChannel<String>(10);
        Channel<String> out = new BlockingChannel<String>(10);
        SerialProcess<String, String> process1 = new IdempotentProcess<String>(new BlockingChannel<String>(10), out);
        SerialProcess<String, String> process2 = new IdempotentProcess<String>(new BlockingChannel<String>(10), out);
        ChannelReader<String> reader1 = new ChannelReader<String>(out);
        executor.execute(reader1);
        executor.execute(process1);
        executor.execute(process2);
        for (int k = 0; k < 100; k++) {
            process1.getInChannel().write(UUID.randomUUID().toString());
            process2.getInChannel().write(UUID.randomUUID().toString());
        }
        process1.getInChannel().close();
        process2.getInChannel().close();
        assertEquals(reader1.getCounter(), 200);
        executor.shutdown();

    }
}
