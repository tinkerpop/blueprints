package com.tinkerpop.blueprints.pgm.jung.util;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.jung.JungHelper;
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

    public Number transform(final Edge edge) {
        if (labels.contains(edge.getLabel())) {
            if (filter) {
                return this.filterValue;
            } else {
                if (this.probability) {
                    List<Edge> allowedEdges = JungHelper.filterEdgeLabels(edge.getOutVertex().getOutEdges(), this.labels, filter);
                    return 1.0d / allowedEdges.size();
                } else {
                    return 1.0d;
                }
            }
        } else {
            if (filter) {
                if (this.probability) {
                    List<Edge> allowedEdges = JungHelper.filterEdgeLabels(edge.getOutVertex().getOutEdges(), this.labels, filter);
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
