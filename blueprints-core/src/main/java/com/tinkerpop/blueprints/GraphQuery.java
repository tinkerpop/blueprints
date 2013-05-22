package com.tinkerpop.blueprints;

/**
 * @author Matthias Broecheler (me@matthiasb.com)
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */

public interface GraphQuery extends Query {

    @Override
    public GraphQuery has(final String key);

    @Override
    public GraphQuery hasNot(final String key);

    @Override
    public GraphQuery has(final String key, final Object... values);

    @Override
    @Deprecated
    public <T extends Comparable<T>> GraphQuery has(final String key, final T value, final Compare compare);

    @Override
    public <T extends Comparable<T>> GraphQuery has(final String key, final Compare compare, final T... values);

    @Override
    public <T extends Comparable<T>> GraphQuery interval(final String key, final T startValue, final T endValue);

    @Override
    public GraphQuery limit(final long total);

    @Override
    public GraphQuery limit(final long skip, final long total);

}
