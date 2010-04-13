package com.tinkerpop.blueprints.pgm.pipex;

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

    public class ChannelReader implements Runnable {
        private final Channel channel;
        private int counter = 0;
        private boolean complete = false;
        private final Object monitor = new Object();

        public ChannelReader(Channel channel) {
            this.channel = channel;
        }

        public void run() {
            while (null != channel.read()) {
                this.counter++;
            }
            channel.close();

            synchronized (this.monitor) {
                this.complete = true;
                this.monitor.notifyAll();
            }
        }

        public int getCounter() {
            try {
                synchronized (this.monitor) {
                    if (!this.complete) {
                        this.monitor.wait();
                    }
                }
            } catch (InterruptedException e) {

            }
            return this.counter;
        }
    }

}
