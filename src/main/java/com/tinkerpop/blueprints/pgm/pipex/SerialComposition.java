package com.tinkerpop.blueprints.pgm.pipex;

import java.util.concurrent.ExecutorService;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class SerialComposition<S, E> extends SerialProcess<S, E> {

    SerialProcess[] processes;
    ExecutorService executor;

    public SerialComposition(ExecutorService executor, int capacity, Channel inputChannel, Channel outputChannel, SerialProcess... processes) {
        this.inChannel = inputChannel;
        this.outChannel = outputChannel;
        this.processes = processes;
        this.executor = executor;
        processes[0].setInChannel(this.inChannel);
        processes[processes.length - 1].setOutChannel(this.outChannel);
        for (int i = 1; i < processes.length; i++) {
            Channel channel = new BlockingChannel(capacity);
            processes[i - 1].setOutChannel(channel);
            processes[i].setInChannel(channel);
        }
    }

    public void run() {
        for (Process process : this.processes) {
            executor.execute(process);
        }
    }

}
