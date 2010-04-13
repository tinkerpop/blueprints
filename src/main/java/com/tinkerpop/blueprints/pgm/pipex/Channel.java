package com.tinkerpop.blueprints.pgm.pipex;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface Channel<T> {

    public T read();
    public void write(T t);
    public void close();
}
