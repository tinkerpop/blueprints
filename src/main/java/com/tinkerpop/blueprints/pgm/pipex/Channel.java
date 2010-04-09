package com.tinkerpop.blueprints.pgm.pipex;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface Channel<T> {

    public T read();
    public boolean write(T t);
    public void close();
    public boolean isComplete();
}
