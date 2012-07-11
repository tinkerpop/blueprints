package com.tinkerpop.blueprints.impls.rexster;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Query;
import com.tinkerpop.blueprints.Vertex;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;


/**
 * RexsterQuery makes no requests until edges(), vertices(), count() or vertexIds() is called.
 *
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class RexsterQuery implements Query {

    private static final String[] EMPTY_LABELS = new String[]{};

    public Direction direction = Direction.BOTH;
    public String[] labels = EMPTY_LABELS;
    public long limit = Long.MAX_VALUE;
    public List<HasContainer> hasContainers = new ArrayList<HasContainer>();
    public final String baseUri;
    public final RexsterGraph graph;

    public RexsterQuery(final String uri, final RexsterGraph graph) {
        this.baseUri = uri;
        this.graph = graph;
    }

    public Query has(final String key, final Object value) {
        this.hasContainers.add(new HasContainer(key, value, Compare.EQUAL));
        return this;
    }

    public <T extends Comparable<T>> Query has(final String key, final T value, final Compare compare) {
        this.hasContainers.add(new HasContainer(key, value, compare));
        return this;
    }

    public <T extends Comparable<T>> Query interval(final String key, final T startValue, final T endValue) {
        this.hasContainers.add(new HasContainer(key, startValue, Compare.GREATER_THAN_EQUAL));
        this.hasContainers.add(new HasContainer(key, endValue, Compare.LESS_THAN));
        return this;
    }

    public Query direction(final Direction direction) {
        this.direction = direction;
        return this;
    }

    public Query labels(final String... labels) {
        this.labels = labels;
        return this;
    }

    public Query limit(final long max) {
        this.limit = max;
        return this;
    }

    public Iterable<Edge> edges() {
        final String directionReturnToken;
        if (this.direction == Direction.IN) {
            directionReturnToken = RexsterTokens.SLASH_INE;
        } else if (this.direction == Direction.OUT) {
            directionReturnToken = RexsterTokens.SLASH_OUTE;
        } else {
            directionReturnToken = RexsterTokens.SLASH_BOTHE;
        }

        return new RexsterEdgeIterable(buildUri(directionReturnToken), graph);
    }

    public Iterable<Vertex> vertices() {
        final String directionReturnToken;
        if (this.direction == Direction.IN) {
            directionReturnToken = RexsterTokens.SLASH_IN;
        } else if (this.direction == Direction.OUT) {
            directionReturnToken = RexsterTokens.SLASH_OUT;
        } else {
            directionReturnToken = RexsterTokens.SLASH_BOTH;
        }

        return new RexsterVertexIterable(buildUri(directionReturnToken), graph);
    }

    public long count() {
        final String directionReturnToken;
        if (this.direction == Direction.IN) {
            directionReturnToken = RexsterTokens.SLASH_INCOUNT;
        } else if (this.direction == Direction.OUT) {
            directionReturnToken = RexsterTokens.SLASH_OUTCOUNT;
        } else {
            directionReturnToken = RexsterTokens.SLASH_BOTHCOUNT;
        }

        final JSONObject jsonObject = RestHelper.get(buildUri(directionReturnToken));
        final long count  = jsonObject.optLong(RexsterTokens.TOTAL_SIZE);

        return count;
    }

    public Object vertexIds() {
        final String directionReturnToken;
        if (this.direction == Direction.IN) {
            directionReturnToken = RexsterTokens.SLASH_INIDS;
        } else if (this.direction == Direction.OUT) {
            directionReturnToken = RexsterTokens.SLASH_OUTIDS;
        } else {
            directionReturnToken = RexsterTokens.SLASH_BOTHIDS;
        }

        final JSONArray jsonArray = RestHelper.getResultArray(buildUri(directionReturnToken));

        final List<Object> list = new ArrayList<Object>();
        for (int ix = 0; ix < jsonArray.length(); ix++) {
            list.add(jsonArray.opt(ix));
        }

        return list;
    }

    private String buildUri(final String directionReturnToken) {
        final StringBuilder sb = new StringBuilder(this.baseUri + directionReturnToken + RexsterTokens.QUESTION);
        sb.append(RexsterTokens._LIMIT);
        sb.append(RexsterTokens.EQUALS);
        sb.append(this.limit);

        if (this.labels != null && this.labels.length > 0) {
            sb.append(RexsterTokens.AND);
            sb.append(RexsterTokens._LABEL_EQUALS);

            sb.append(RexsterTokens.LEFT_SQUARE_BRACKET);
            for (String label : labels) {
                sb.append(label);
                sb.append(RexsterTokens.COMMA);
            }

            sb.deleteCharAt(sb.length() - 1);
            sb.append(RexsterTokens.RIGHT_SQUARE_BRACKET);
        }

        if (this.hasContainers.size() > 0) {
            sb.append(RexsterTokens.AND);
            sb.append(RexsterTokens._PROPERTIES);
            sb.append(RexsterTokens.EQUALS);
            sb.append(RexsterTokens.LEFT_SQUARE_BRACKET);

            for (HasContainer hasContainer : this.hasContainers) {
                sb.append(RexsterTokens.LEFT_SQUARE_BRACKET);
                sb.append(hasContainer.key);

                sb.append(RexsterTokens.COMMA);
                sb.append(getCompareString(hasContainer.compare));
                sb.append(RexsterTokens.COMMA);

                sb.append(RestHelper.uriCast(hasContainer.value));

                sb.append(RexsterTokens.RIGHT_SQUARE_BRACKET);

                sb.append(RexsterTokens.COMMA);
            }

            sb.deleteCharAt(sb.length() - 1);

            if (this.hasContainers.size() > 0) {
                sb.append(RexsterTokens.RIGHT_SQUARE_BRACKET);
            }
        }

        return sb.toString();
    }

    private static String getCompareString(Compare compare) {
        if (compare == Compare.EQUAL) {
            return "=";
        } else if (compare == Compare.GREATER_THAN){
            return ">";
        } else if (compare == Compare.GREATER_THAN_EQUAL) {
            return ">=";
        } else if (compare == Compare.LESS_THAN_EQUAL) {
            return "<=";
        } else if (compare == Compare.LESS_THAN) {
            return "<";
        } else if (compare == Compare.NOT_EQUAL) {
            return "<>";
        }

        throw new RuntimeException("Invalid comparator");
    }

    private class HasContainer {
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

