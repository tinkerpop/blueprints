package com.tinkerpop.blueprints;

/**
 * MetaGraph can be implemented as a way to access the underlying native graph engine.
 * This is useful for those Graph implementations that are not native Blueprints implementations.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface MetaGraph<T> extends Graph {

    /**
     * Get the raw underlying graph engine that exposes the Blueprints API.
     *
     * @return the raw underlying graph engine
     */
    public T getRawGraph();
}
