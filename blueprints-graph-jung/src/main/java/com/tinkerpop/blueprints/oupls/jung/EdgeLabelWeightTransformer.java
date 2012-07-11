package com.tinkerpop.blueprints.oupls.jung;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import org.apache.commons.collections15.Transformer;

import java.util.Set;

public class EdgeLabelWeightTransformer implements Transformer<Edge, Number> {
    private Set<String> labels;
    private Boolean filterLabels;
    private String weightKey;
    private Boolean normalizeWeights;
    private Boolean invertWeights;

    public EdgeLabelWeightTransformer(final Set<String> labels, final Boolean filterLabels, final String weightKey, final Boolean normalizeWeights, final Boolean invertWeights) {
        this.labels = labels;
        if (null == filterLabels)
            this.filterLabels = false;
        else
            this.filterLabels = filterLabels;
        this.weightKey = weightKey;
        if (null == normalizeWeights)
            this.normalizeWeights = false;
        else
            this.normalizeWeights = normalizeWeights;
        if (null == invertWeights)
            this.invertWeights = false;
        else
            this.invertWeights = invertWeights;

    }

    public Double transform(final Edge edge) {
        if (labels.contains(edge.getLabel())) {
            if (filterLabels) {
                return 0.0d;
            } else {
                Object object = edge.getProperty(this.weightKey);
                if (object instanceof Number) {
                    Double weight = ((Number) object).doubleValue();
                    if (this.invertWeights)
                        weight = 1 / weight;
                    if (this.normalizeWeights) {
                        return weight / JungHelper.totalWeight(JungHelper.filterEdgeLabels(edge.getVertex(Direction.OUT).getEdges(Direction.OUT), this.labels, filterLabels), this.weightKey);
                    } else {
                        return weight;
                    }
                } else {
                    return 0.0d;
                }
            }
        } else {
            if (filterLabels) {
                Object object = edge.getProperty(this.weightKey);
                if (object instanceof Number) {
                    Double weight = ((Number) object).doubleValue();
                    if (this.invertWeights)
                        weight = 1 / weight;
                    if (this.normalizeWeights) {
                        return weight / JungHelper.totalWeight(JungHelper.filterEdgeLabels(edge.getVertex(Direction.OUT).getEdges(Direction.OUT), this.labels, filterLabels), this.weightKey);
                    } else {
                        return weight;
                    }
                } else {
                    return 0.0d;
                }
            } else {
                return 0.0d;
            }
        }
    }
}