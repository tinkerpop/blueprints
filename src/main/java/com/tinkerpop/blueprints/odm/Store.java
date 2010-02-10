package com.tinkerpop.blueprints.odm;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface Store {

    public Document getDocument(String collection, String id);

    public void putDocument(Document document);

    public void removeDocument(Document document);

    public void shutdown();
}
