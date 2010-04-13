package com.tinkerpop.blueprints.pgm.pipex;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class IdempotentProcess<S> extends SerialProcess<S, S> {

    public IdempotentProcess(Channel<S> inChannel, Channel<S> outChannel) {
        super(inChannel, outChannel);
    }

    public boolean step() {
        S s = this.inChannel.read();
        if (null != s) {
            this.outChannel.write(s);
            return true;
        } else {
            return false;
        }
    }
}

