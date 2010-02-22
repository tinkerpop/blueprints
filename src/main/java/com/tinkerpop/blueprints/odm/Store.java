package com.tinkerpop.blueprints.odm;

import java.util.Map;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface Store<T extends Document> {

    public T makeDocument(Map<String, Object> map);

    public T retrieve(String id);

    public Iterable<T> retrieve(T document);

    public void save(T document);

    public void delete(T document);

    public void shutdown();
}
