package com.tinkerpop.blueprints.pgm.pipex;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class BlockingChannel<T> implements Channel<T> {

    private final LinkedBlockingQueue<T> queue;
    private boolean open = true;
    private final Object monitor = new Object();

    public BlockingChannel(int capacity) {
        this.queue = new LinkedBlockingQueue<T>(capacity);
    }

    public T read() {
        try {
            synchronized (this.monitor) {
                if (!this.queue.isEmpty())
                    return this.queue.take();
                else if (!this.open)
                    return null;
                else {
                    this.monitor.wait();
                    return this.read();
                }
            }
        } catch (InterruptedException e) {
            this.close();
            e.printStackTrace();
            return null;
        }
    }

    public void write(T t) {
        try {
            this.queue.put(t);
            synchronized (this.monitor) {
                this.monitor.notifyAll();
            }
        } catch (InterruptedException e) {
            this.close();
            e.printStackTrace();
        }
    }

    public void close() {
        synchronized (this.monitor) {
            this.open = false;
            this.monitor.notifyAll();
        }
    }
}
