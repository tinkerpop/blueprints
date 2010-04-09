package com.tinkerpop.blueprints.pgm.pipex.pgm;

import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.pipex.SerialProcess;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class PropertyProcess<T> extends SerialProcess<Element, T> {

    private final String key;

    public PropertyProcess(final String key) {
        this.key = key;
    }

    public void step() {
        Element element = this.inputChannel.read();
        T value = (T) element.getProperty(this.key);
        if(null != value) {
            this.outputChannel.write(value);
        }
    }

}
