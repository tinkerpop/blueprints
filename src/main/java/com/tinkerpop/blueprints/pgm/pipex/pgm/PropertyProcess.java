package com.tinkerpop.blueprints.pgm.pipex.pgm;

import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.pipex.Channel;
import com.tinkerpop.blueprints.pgm.pipex.SerialProcess;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class PropertyProcess<E> extends SerialProcess<Element, E> {

    private final String key;

    public PropertyProcess(final String key) {
        this(key, null, null);
    }

    public PropertyProcess(final String key, final Channel<Element> inChannel, final Channel<E> outChannel) {
        super(inChannel, outChannel);
        this.key = key;
    }

    public boolean step() {
        Element element = this.inChannel.read();
        if (null != element) {
            E value = (E) element.getProperty(this.key);
            if (null != value) {
                this.outChannel.write(value);
            }
            return true;
        } else {
            return false;
        }
    }

}
