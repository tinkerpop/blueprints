package com.tinkerpop.blueprints.pgm.pipex;


/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class SerialProcess<S, E> extends AbstractProcess {

    protected Channel<S> inputChannel;
    protected Channel<E> outputChannel;

    public void setInputChannel(Channel<S> inputChannel) {
        this.inputChannel = inputChannel;
    }

    public void setOutputChannel(Channel<E> outputChannel) {
        this.outputChannel = outputChannel;
    }

    public Channel<S> getInputChannel() {
        return this.inputChannel;
    }

    public Channel<E> getOutputChannel() {
        return this.outputChannel;
    }

    public void run() {
        this.onStart();
        while (!this.inputChannel.isComplete()) {
            this.step();
            Thread.yield();
        }
        this.outputChannel.close();
        this.onStop();
    }


    public void step() {
        throw new UnsupportedOperationException();
    }

}
