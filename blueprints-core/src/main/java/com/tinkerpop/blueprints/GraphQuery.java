package com.tinkerpop.blueprints;

/**
 * @author Matthias Broecheler (me@matthiasb.com)
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Luca Garulli (http://www.orientechnologies.com)
 * @author Daniel Kuppitz (daniel.kuppitz@shoproach.com)
 */

public interface GraphQuery extends Query {

    @Override
    public GraphQuery has(final String key);

    @Override
    public GraphQuery hasNot(final String key);

    @Override
    public GraphQuery has(final String key, final Object value);

    @Override
    public GraphQuery hasNot(final String key, final Object value);

    @Override
    public GraphQuery has(final String key, final CompareRelation compare, final Object value);

    @Override
    @Deprecated
    public <T extends Comparable<T>> GraphQuery has(final String key, final T value, final Compare compare);

    @Override
    public <T extends Comparable<T>> GraphQuery interval(final String key, final T startValue, final T endValue);

    @Override
    public GraphQuery limit(final int limit);
}
