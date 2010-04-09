package com.tinkerpop.blueprints.pgm.pipex;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class BlockingChannel<T> implements Channel<T> {

    private final LinkedBlockingQueue<T> queue;
    private boolean open = true;

    public BlockingChannel(int capacity) {
        this.queue = new LinkedBlockingQueue<T>(capacity);
    }

    public T read() {
        try {
            return this.queue.poll(100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            this.close();
            e.printStackTrace();
            return null;
        }
    }

    public boolean write(T t) {
        try {
            return this.queue.offer(t, 100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            this.close();
            e.printStackTrace();
            return false;
        }
    }

    public void close() {
        this.open = false;
    }

    public boolean isComplete() {
        return !this.open && this.queue.isEmpty();
    }

}
