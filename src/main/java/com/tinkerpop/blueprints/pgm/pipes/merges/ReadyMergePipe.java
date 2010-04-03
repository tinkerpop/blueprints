package com.tinkerpop.blueprints.pgm.pipes.merges;

import com.tinkerpop.blueprints.pgm.pipes.AbstractPipe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ReadyMergePipe<S> extends AbstractPipe<Iterator<S>, S> {

    private final ConcurrentLinkedQueue<S> queue = new ConcurrentLinkedQueue<S>();
    private final List<ThreadedPull<S>> threads = new ArrayList<ThreadedPull<S>>();
    private final Object monitor = new Object();

    public void setStarts(final Iterator<Iterator<S>> starts) {
        this.starts = starts;
        while (this.starts.hasNext()) {
            ThreadedPull<S> thread = new ThreadedPull<S>(this.starts.next(), this.queue);
            this.threads.add(thread);
            new Thread(thread).start();
        }
        this.setNext();
    }

    public void setNext() {
        if (!this.queue.isEmpty()) {
            this.nextEnd = this.queue.remove();
        } else {
            this.done = true;
            for (ThreadedPull thread : threads) {
                if (!thread.isDone()) {
                    this.done = false;
                    break;
                }
            }
            if (!this.done) {
                try {
                    synchronized (monitor) {
                        monitor.wait(0,500);
                    }
                } catch (InterruptedException e) {
                }
                this.setNext();
            }
        }
    }

    private class ThreadedPull<S> implements Runnable {

        private final ConcurrentLinkedQueue<S> pushToQueue;
        private final Iterator<S> pullFromIterator;
        private boolean done = false;

        public ThreadedPull(final Iterator<S> pullFromIterator, final ConcurrentLinkedQueue<S> pushToQueue) {
            this.pushToQueue = pushToQueue;
            this.pullFromIterator = pullFromIterator;
        }

        public void run() {
            while (this.pullFromIterator.hasNext()) {
                this.pushToQueue.add(this.pullFromIterator.next());
                synchronized (monitor) {
                    monitor.notify();
                }
            }
            this.done = true;
        }

        public boolean isDone() {
            return this.done;
        }


    }

}
