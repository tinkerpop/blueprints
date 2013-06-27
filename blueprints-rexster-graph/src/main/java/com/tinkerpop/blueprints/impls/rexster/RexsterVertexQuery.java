package com.tinkerpop.blueprints.impls.rexster;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.DefaultVertexQuery;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * RexsterQuery makes no requests until edges(), vertices(), count() or vertexIds() is called.
 *
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class RexsterVertexQuery extends DefaultVertexQuery {

    public final String baseUri;
    public final RexsterGraph graph;

    public RexsterVertexQuery(final String uri, final RexsterGraph graph) {
        super(null);
        this.baseUri = uri;
        this.graph = graph;
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
        return jsonObject.optLong(RexsterTokens.TOTAL_SIZE);
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
        sb.append(RexsterTokens._TAKE);
        sb.append(RexsterTokens.EQUALS);
        sb.append(this.limit);

        /*sb.append(RexsterTokens.AND);

        sb.append(RexsterTokens._SKIP);
        sb.append(RexsterTokens.EQUALS);
        sb.append(this.minimum);*/

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
                sb.append(hasContainer.predicate.toString());
                sb.append(RexsterTokens.COMMA);

                // for (Object v : hasContainer.values) {
                sb.append(RestHelper.uriCast(hasContainer.value)); // TODO BRO.
                sb.append(" ");
                //}

                sb.trimToSize();

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

}

