package com.tinkerpop.blueprints.pgm.pipes.merges;

import com.tinkerpop.blueprints.pgm.pipes.AbstractPipe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
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
    }

    protected S processNextStart() {
        if (!this.queue.isEmpty()) {
            return this.queue.remove();
        } else {

            boolean allDone = true;
            for (ThreadedPull thread : threads) {
                if (!thread.isDone()) {
                    allDone = false;

                }
            }
            if (allDone) {
                throw new NoSuchElementException();
            }
            else {
                try {
                    synchronized (monitor) {
                        monitor.wait(0, 500);
                    }
                } catch (InterruptedException e) {
                }
                return this.processNextStart();
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
                Thread.yield();
            }
            this.done = true;
        }

        public boolean isDone() {
            return this.done;
        }


    }

}
