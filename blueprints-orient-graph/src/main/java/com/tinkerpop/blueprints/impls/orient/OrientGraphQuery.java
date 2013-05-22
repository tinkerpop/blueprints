package com.tinkerpop.blueprints.impls.orient;

import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Query;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.DefaultGraphQuery;

import java.util.Collections;

/**
 * OrientDB implementation for Graph query.
 * 
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientGraphQuery extends DefaultGraphQuery {

	private static final char SPACE = ' ';
	private static final String OPERATOR_DIFFERENT = "<>";
	private static final String OPERATOR_IS_NOT = "is not";
	private static final String OPERATOR_LET = "<=";
	private static final char OPERATOR_LT = '<';
	private static final String OPERATOR_GTE = ">=";
	private static final char OPERATOR_GT = '>';
	private static final String OPERATOR_EQUALS = "=";
	private static final String OPERATOR_IS = "is";

	private static final String QUERY_FILTER_AND = " and ";
	private static final char QUERY_STRING = '\'';
	private static final char QUERY_SEPARATOR = ',';
	private static final String QUERY_LABEL_BEGIN = " and label in [";
	private static final String QUERY_LABEL_END = "]";
	private static final String QUERY_WHERE = " where 1=1";
	private static final String QUERY_SELECT_FROM = "select from ";
    private static final String LIMIT = " LIMIT ";
    private static final String SKIP = " SKIP ";

	public OrientGraphQuery(final Graph iGraph) {
		super(iGraph);
	}

	public Query labels(final String... labels) {
		this.labels = labels;
		return this;
	}

	@Override
	public Iterable<Vertex> vertices() {
		if (maximum == 0)
			return Collections.emptyList();

		final StringBuilder text = new StringBuilder();

		// GO DIRECTLY AGAINST E CLASS AND SUB-CLASSES
		text.append(QUERY_SELECT_FROM);

		if (((OrientBaseGraph) graph).isUseClassForVertexLabel()
				&& labels != null && labels.length > 0) {
			// FILTER PER CLASS SAVING CHECKING OF LABEL PROPERTY
			if (labels.length == 1)
				// USE THE CLASS NAME
				text.append(OrientBaseGraph.encodeClassName(labels[0]));
			else {
				// MULTIPLE CLASSES NOT SUPPORTED DIRECTLY: CREATE A SUB-QUERY
				return super.vertices();
			}
		} else
			text.append(OrientVertex.CLASS_NAME);

		// APPEND ALWAYS WHERE 1=1 TO MAKE CONCATENATING EASIER
		text.append(QUERY_WHERE);
		manageFilters(text);
		if (!((OrientBaseGraph) graph).isUseClassForVertexLabel())
			manageLabels(text);

        if (maximum > 0 && maximum < Long.MAX_VALUE) {
            if( minimum > 0 && maximum < Long.MAX_VALUE ) {
                text.append(SKIP);
                text.append(minimum);
                text.append(LIMIT);
                text.append(maximum - minimum );
            } else {
                text.append(LIMIT);
                text.append(maximum);
            }
        }
        final OSQLSynchQuery<OIdentifiable> query = new OSQLSynchQuery<OIdentifiable>(
                text.toString());
		return new OrientElementIterable<Vertex>(((OrientBaseGraph) graph),
				((OrientBaseGraph) graph).getRawGraph().query(query));
	}

	@Override
	public Iterable<Edge> edges() {
		if (maximum == 0)
			return Collections.emptyList();

		if (((OrientBaseGraph) graph).isUseLightweightEdges())
			return super.edges();

		final StringBuilder text = new StringBuilder();

		// GO DIRECTLY AGAINST E CLASS AND SUB-CLASSES
		text.append(QUERY_SELECT_FROM);

		if (((OrientBaseGraph) graph).isUseClassForEdgeLabel()
				&& labels != null && labels.length > 0) {
			// FILTER PER CLASS SAVING CHECKING OF LABEL PROPERTY
			if (labels.length == 1)
				// USE THE CLASS NAME
				text.append(OrientBaseGraph.encodeClassName(labels[0]));
			else {
				// MULTIPLE CLASSES NOT SUPPORTED DIRECTLY: CREATE A SUB-QUERY
				return super.edges();
			}
		} else
			text.append(OrientEdge.CLASS_NAME);

		// APPEND ALWAYS WHERE 1=1 TO MAKE CONCATENATING EASIER
		text.append(QUERY_WHERE);

		manageFilters(text);
		if (!((OrientBaseGraph) graph).isUseClassForEdgeLabel())
			manageLabels(text);

		final OSQLSynchQuery<OIdentifiable> query = new OSQLSynchQuery<OIdentifiable>(
				text.toString());

		if (maximum > 0 && maximum < Long.MAX_VALUE)
			query.setLimit((int) maximum);

		return new OrientElementIterable<Edge>(((OrientBaseGraph) graph),
				((OrientBaseGraph) graph).getRawGraph().query(query));
	}

	private void manageLabels(final StringBuilder text) {
		if (labels != null && labels.length > 0) {
			// APPEND LABELS
			text.append(QUERY_LABEL_BEGIN);
			for (int i = 0; i < labels.length; ++i) {
				if (i > 0)
					text.append(QUERY_SEPARATOR);
				text.append(QUERY_STRING);
				text.append(labels[i]);
				text.append(QUERY_STRING);
			}
			text.append(QUERY_LABEL_END);
		}
	}

    // TODO: make this.values be the ORing of the values not just values[0]
	private void manageFilters(final StringBuilder text) {
		for (HasContainer has : hasContainers) {
			text.append(QUERY_FILTER_AND);

			text.append(has.key);
			text.append(SPACE);
			switch (has.compare) {
			case EQUAL:
				if (has.values[0] == null)
					text.append(OPERATOR_IS);
				else
					text.append(OPERATOR_EQUALS);
				break;
			case GREATER_THAN:
				text.append(OPERATOR_GT);
				break;
			case GREATER_THAN_EQUAL:
				text.append(OPERATOR_GTE);
				break;
			case LESS_THAN:
				text.append(OPERATOR_LT);
				break;
			case LESS_THAN_EQUAL:
				text.append(OPERATOR_LET);
				break;
			case NOT_EQUAL:
				if (has.values[0] == null)
					text.append(OPERATOR_IS_NOT);
				else
					text.append(OPERATOR_DIFFERENT);
				break;
			}
			text.append(SPACE);

			if (has.values[0] instanceof String)
				text.append(QUERY_STRING);
			text.append(has.values[0]);
			if (has.values[0] instanceof String)
				text.append(QUERY_STRING);
		}
	}
}