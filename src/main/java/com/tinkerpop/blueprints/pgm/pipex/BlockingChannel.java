package com.tinkerpop.blueprints.pgm.pipex;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class BlockingChannel<T> implements Channel<T> {

    private final LinkedBlockingQueue<T> queue;
    volatile private boolean open = true;
    private final Semaphore semaphore = new Semaphore(1, true);

    public BlockingChannel(final int capacity) {
        this.queue = new LinkedBlockingQueue<T>(capacity);
    }

    public T read() {
        try {
            semaphore.acquire();
            if (!this.queue.isEmpty()) {
                return this.queue.take();
            }
            else if (!this.open) {
                return null;
            }
            else {
                return this.read();
            }
        } catch (InterruptedException e) {
            this.close();
            e.printStackTrace();
            return null;
        }
    }

    public void write(final T t) {
        try {
            this.queue.put(t);
            this.semaphore.release();
        } catch (InterruptedException e) {
            this.close();
            e.printStackTrace();
        }
    }

    public void close() {
        this.open = false;
        this.semaphore.release();
    }
}
