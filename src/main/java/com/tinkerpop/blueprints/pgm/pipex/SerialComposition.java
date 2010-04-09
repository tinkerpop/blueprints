package com.tinkerpop.blueprints.pgm.pipex;

import java.util.concurrent.ExecutorService;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class SerialComposition<S, E> extends SerialProcess<S, E> {

    SerialProcess[] processes;
    ExecutorService executor;

    public SerialComposition(ExecutorService executor, int capacity, Channel inputChannel, Channel outputChannel, SerialProcess... processes) {
        this.inputChannel = inputChannel;
        this.outputChannel = outputChannel;
        this.processes = processes;
        this.executor = executor;
        processes[0].setInputChannel(this.inputChannel);
        processes[processes.length - 1].setOutputChannel(this.outputChannel);
        for (int i = 1; i < processes.length; i++) {
            Channel channel = new BlockingChannel(capacity);
            processes[i - 1].setOutputChannel(channel);
            processes[i].setInputChannel(channel);
        }
    }

    public void run() {
        for (Process process : this.processes) {
            executor.execute(process);
        }
    }

}
