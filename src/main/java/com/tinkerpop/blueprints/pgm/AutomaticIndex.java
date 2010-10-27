package com.tinkerpop.blueprints.pgm;

import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface AutomaticIndex<T extends Element> extends Index<T> {

    public void addAutoIndexKey(String key);

    public void removeAutoIndexKey(String key);

    public Set<String> getAutoIndexKeys();

    public boolean doAutoIndex(String key, Class classToIndex);
}
