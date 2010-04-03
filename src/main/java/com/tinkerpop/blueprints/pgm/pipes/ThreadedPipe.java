package com.tinkerpop.blueprints.pgm.pipes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ThreadedPipe<S> extends AbstractPipe<S, S> {

    private Buffer<S> buffer = new Buffer<S>();
    private List<PipeThread<S>> pipeThreads;

    public ThreadedPipe(int pipeThreads) {
        this.pipeThreads = new ArrayList<PipeThread<S>>(pipeThreads);
        for (int i = 0; i < pipeThreads; i++) {
            this.pipeThreads.add(new PipeThread<S>(this.buffer));
        }
    }

    public void setStarts(final Iterator<S> starts) {
        for (PipeThread<S> thread : this.pipeThreads) {
            thread.setStarts(starts);
            new Thread(thread).start();
        }
        this.setNext();
    }

    protected void setNext() {
        if (this.buffer.getDead() == this.pipeThreads.size() && !this.buffer.isAvailable())
            this.done = true;
        else
            this.nextEnd = buffer.get();
    }

    private class Buffer<S> {
        private S end;
        private boolean available = false;
        private int dead = 0;

        public synchronized S get() {
            while (!available) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
            available = false;
            notifyAll();
            return end;
        }

        public synchronized void put(S value) {
            while (available) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
            end = value;
            available = true;
            notifyAll();
        }

        public synchronized boolean isAvailable() {
            return available;
        }

        public synchronized void dead() {
            //System.out.println("THREAD DEAD");
            dead++;
        }

        public synchronized int getDead() {
            //System.out.println("TOTAL DEAD: " + dead);
            return dead;
        }
    }

    private class PipeThread<S> extends AbstractPipe<S, S> implements Runnable {

        private Buffer<S> buffer;

        public PipeThread(Buffer<S> buffer) {
            this.buffer = buffer;
        }

        public void run() {
            while (true) {
                //System.out.println(this.queue.peek() + "\t\t" + Thread.currentThread().getName());
                boolean haveNext = false;
                synchronized (this.starts) {
                    if (this.starts.hasNext()) {
                        this.nextEnd = this.starts.next();
                        haveNext = true;
                    }
                }
                if (haveNext) {
                    this.buffer.put(this.nextEnd);
                } else {
                    this.buffer.dead();
                    return;
                }
            }

            //System.out.println(this.queue);
        }

        public void setStarts(Iterator<S> starts) {
            this.starts = starts;
        }
    }

}
