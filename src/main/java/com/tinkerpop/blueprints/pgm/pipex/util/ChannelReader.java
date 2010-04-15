package com.tinkerpop.blueprints.pgm.pipex.util;

import com.tinkerpop.blueprints.pgm.pipex.Channel;
import com.tinkerpop.blueprints.pgm.pipex.SerialProcess;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ChannelReader<S> extends SerialProcess<S, S> {
    private final Channel<S> channel;
    private int counter = 0;
    private boolean complete = false;
    private final Object monitor = new Object();

    public ChannelReader(Channel<S> channel) {
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