package com.tinkerpop.blueprints.oupls.jung;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import org.apache.commons.collections15.Transformer;

import java.util.List;
import java.util.Set;

public class EdgeLabelTransformer implements Transformer<Edge, Number> {
    private Set<String> labels;
    private Boolean filter;
    private Number filterValue;
    private Boolean probability;

    public EdgeLabelTransformer(final Set<String> labels, final Boolean filterLabels, final Number filterValue, final Boolean probability) {
        this.labels = labels;
        if (null == filterLabels)
            this.filter = false;
        else
            this.filter = filterLabels;
        this.filterValue = filterValue;
        this.probability = probability;
    }

    public EdgeLabelTransformer(final Set<String> labels, final Boolean filterLabels) {
        this(labels, filterLabels, 0, false);
    }

    public Number transform(final Edge edge) {
        if (labels.contains(edge.getLabel())) {
            if (filter) {
                return this.filterValue;
            } else {
                if (this.probability) {
                    List<Edge> allowedEdges = JungHelper.filterEdgeLabels(edge.getVertex(Direction.OUT).getEdges(Direction.OUT), this.labels, filter);
                    return 1.0d / allowedEdges.size();
                } else {
                    return 1.0d;
                }
            }
        } else {
            if (filter) {
                if (this.probability) {
                    List<Edge> allowedEdges = JungHelper.filterEdgeLabels(edge.getVertex(Direction.OUT).getEdges(Direction.OUT), this.labels, filter);
                    return 1.0d / allowedEdges.size();
                } else {
                    return 1.0d;
                }
            } else {
                return this.filterValue;
            }
        }
    }

}
