package com.tinkerpop.blueprints;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public enum Direction {

    OUT, IN, BOTH;

    public Direction opposite(final Direction direction) {
        if (direction.equals(OUT))
            return IN;
        else if (direction.equals(IN))
            return OUT;
        else
            return BOTH;
    }
}
