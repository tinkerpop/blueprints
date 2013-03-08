package com.tinkerpop.blueprints;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public interface GraphQuery extends Query {

    @Override
    public GraphQuery has(final String key, final Object value);

    @Override
    public <T extends Comparable<T>> GraphQuery has(final String key, final T value, final Compare compare);

    @Override
    public <T extends Comparable<T>> GraphQuery interval(final String key, final T startValue, final T endValue);

    @Override
    public GraphQuery limit(final long max);

}
