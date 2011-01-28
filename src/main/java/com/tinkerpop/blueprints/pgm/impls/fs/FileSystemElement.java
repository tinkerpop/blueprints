package com.tinkerpop.blueprints.pgm.impls.fs;

import com.tinkerpop.blueprints.pgm.Element;

import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class FileSystemElement implements Element {

    public Set<String> getPropertyKeys() {
        return null;
    }

    public Object removeProperty(String key) {
        return null;
    }

    public void setProperty(String key, Object value) {

    }

    public Object getProperty(String key) {
        return null;
    }

}
