package com.tinkerpop.blueprints.pgm.pipex.pgm;

import com.tinkerpop.blueprints.pgm.Element;
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
        this.key = key;
        this.values = values;
        this.filter = filter;
    }

    public void step() {
        Element element = inputChannel.read();
        if (filter) {
            if (!this.values.contains(element.getProperty(key))) {
                this.outputChannel.write(element);
            }
        } else {
            if (this.values.contains(element.getProperty(key))) {
                this.outputChannel.write(element);
            }
        }
    }

}
