package com.tinkerpop.blueprints.util;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class DefaultQuery implements Query {

    private static final String[] EMPTY_LABELS = new String[]{};

    public Direction direction = Direction.BOTH;
    public String[] labels = EMPTY_LABELS;
    public long limit = Long.MAX_VALUE;
    public List<HasContainer> hasContainers = new ArrayList<HasContainer>();

    protected class HasContainer {
        public String key;
        public Object value;
        public Compare compare;

        public HasContainer(final String key, final Object value, final Compare compare) {
            this.key = key;
            this.value = value;
            this.compare = compare;
        }

        public boolean isLegal(final Element element) {
            final Object elementValue = element.getProperty(key);
            switch (compare) {
                case EQUAL:
                    if (null == elementValue)
                        return value == null;
                    return elementValue.equals(value);
                case NOT_EQUAL:
                    if (null == elementValue)
                        return value != null;
                    return !elementValue.equals(value);
                case GREATER_THAN:
                    if (null == elementValue || value == null)
                        return false;
                    return ((Comparable) elementValue).compareTo(value) >= 1;
                case LESS_THAN:
                    if (null == elementValue || value == null)
                        return false;
                    return ((Comparable) elementValue).compareTo(value) <= -1;
                case GREATER_THAN_EQUAL:
                    if (null == elementValue || value == null)
                        return false;
                    return ((Comparable) elementValue).compareTo(value) >= 0;
                case LESS_THAN_EQUAL:
                    if (null == elementValue || value == null)
                        return false;
                    return ((Comparable) elementValue).compareTo(value) <= 0;
                default:
                    throw new IllegalArgumentException("Invalid state as no valid filter was provided");
            }
        }
    }
}
