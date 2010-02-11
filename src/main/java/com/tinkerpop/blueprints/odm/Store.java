package com.tinkerpop.blueprints.odm;

import java.util.Map;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface Store {

    public Map get(Map document);

    public Map get(String id);

    public Iterable<Map> getAll(Map document);

    public void put(Map document);

    public void remove(Map document);

    public void shutdown();
}
