package com.tinkerpop.blueprints.pgm.pipex.pgm;

import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.pipex.Channel;
import com.tinkerpop.blueprints.pgm.pipex.SerialProcess;

import java.util.Collection;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class PropertyFilterProcess<T> extends SerialProcess<Element, Element> {

    private final String key;
    private final Collection<T> values;
    private final boolean filter;

    public PropertyFilterProcess(final String key, final Collection<T> values, final boolean filter) {
        this(key, values, filter, null, null);
    }

    public PropertyFilterProcess(final String key, final Collection<T> values, final boolean filter, final Channel<Element> inChannel, final Channel<Element> outChannel) {
        super(inChannel, outChannel);
        this.key = key;
        this.values = values;
        this.filter = filter;
    }

    public boolean step() {
        Element element = inChannel.read();
        if (null != element) {
            if (filter) {
                if (!this.values.contains(element.getProperty(key))) {
                    this.outChannel.write(element);
                }
            } else {
                if (this.values.contains(element.getProperty(key))) {
                    this.outChannel.write(element);
                }
            }
            return true;
        } else {
            return false;
        }
    }

}
