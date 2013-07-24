package com.tinkerpop.blueprints;

/**
 * @author Matthias Broecheler (me@matthiasb.com)
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Luca Garulli (http://www.orientechnologies.com)
 * @author Daniel Kuppitz (daniel.kuppitz@shoproach.com)
 */

public interface GraphQuery extends Query {

    @Override
    public GraphQuery has(String key);

    @Override
    public GraphQuery hasNot(String key);

    @Override
    public GraphQuery has(String key, Object value);

    @Override
    public GraphQuery hasNot(String key, Object value);

    @Override
    public GraphQuery has(String key, Predicate predicate, Object value);

    @Override
    @Deprecated
    public <T extends Comparable<T>> GraphQuery has(String key, T value, Compare compare);

    @Override
    public <T extends Comparable<?>> GraphQuery interval(String key, T startValue, T endValue);

    @Override
    public GraphQuery limit(int limit);
}
