package com.tinkerpop.blueprints.pgm;

/**
 * @author Matthias Brocheler (http://matthiasb.com)
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface Query {

    public enum Compare {EQUAL, NOT_EQUAL, GREATER_THAN, GREATER_THAN_EQUAL, LESS_THAN, LESS_THAN_EQUAL}

    public enum Direction {OUT, IN, BOTH}

    public Query has(final String key, final Object value);

    public Query has(final String key, final Object value, final Compare compare);

    public Query interval(final String key, final Object startValue, final Object endValue);

    public Query direction(final Direction direction);

    public Query labels(final String... labels);

    public Query limit(final long max);

    public Iterable<Edge> edges();

    public Iterable<Vertex> vertices();

    public long count();

    public Object vertexIds();

}
