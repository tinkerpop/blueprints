package com.tinkerpop.blueprints.pgm.pipex;


/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class SerialProcess<S, E> extends AbstractProcess {

    protected Channel<S> inChannel;
    protected Channel<E> outChannel;

    public SerialProcess() {

    }

    public SerialProcess(final Channel<S> inChannel, final Channel<E> outChannel) {
        this.inChannel = inChannel;
        this.outChannel = outChannel;
    }

    public void setInChannel(Channel<S> inChannel) {
        this.inChannel = inChannel;
    }

    public void setOutChannel(Channel<E> outChannel) {
        this.outChannel = outChannel;
    }

    public Channel<S> getInChannel() {
        return this.inChannel;
    }

    public Channel<E> getOutChannel() {
        return this.outChannel;
    }

    public void run() {
        this.onStart();
        while (this.step()) {
            Thread.yield();
        }
        this.outChannel.close();
        this.onStop();
    }


    public boolean step() {
        throw new UnsupportedOperationException("Implement in all non-abstract extending classes");
    }

}
