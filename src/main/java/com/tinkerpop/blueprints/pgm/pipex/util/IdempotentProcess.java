package com.tinkerpop.blueprints.pgm.pipex.util;

import com.tinkerpop.blueprints.pgm.pipex.Channel;
import com.tinkerpop.blueprints.pgm.pipex.SerialProcess;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class IdempotentProcess<S> extends SerialProcess<S, S> {

    public IdempotentProcess() {
        this(null, null);
    }

    public IdempotentProcess(final Channel<S> inChannel, final Channel<S> outChannel) {
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

