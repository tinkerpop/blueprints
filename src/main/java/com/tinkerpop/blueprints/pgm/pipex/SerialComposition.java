package com.tinkerpop.blueprints.pgm.pipex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class SerialComposition<S, E> extends SerialProcess<S, E> {

    private final List<SerialProcess> processes;
    private final ExecutorService executor;

    public SerialComposition(final ExecutorService executor, final int capacity, final Channel<S> inChannel, final Channel<E> outChannel, final List<SerialProcess> processes) {
        this.inChannel = inChannel;
        this.outChannel = outChannel;
        this.processes = processes;
        this.executor = executor;
        processes.get(0).setInChannel(this.inChannel);
        processes.get(processes.size() - 1).setOutChannel(this.outChannel);
        for (int i = 1; i < processes.size(); i++) {
            Channel channel = new BlockingChannel(capacity);
            processes.get(i - 1).setOutChannel(channel);
            processes.get(i).setInChannel(channel);
        }
    }

    public SerialComposition(final ExecutorService executor, final int capacity, final Channel<S> inChannel, final Channel<E> outChannel, final SerialProcess... processes) {
        this(executor, capacity, inChannel, outChannel, new ArrayList<SerialProcess>(Arrays.asList(processes)));    
    }

    public void run() {
        for (Process process : this.processes) {
            this.executor.execute(process);
        }
    }

}
