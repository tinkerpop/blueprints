package com.tinkerpop.blueprints.oupls.jung;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import org.apache.commons.collections15.Transformer;

public class EdgeWeightTransformer implements Transformer<Edge, Number> {
    private String weightKey;
    private Boolean normalize;
    private Boolean invert;

    public EdgeWeightTransformer(final String weightKey, final Boolean normalizeWeights, final Boolean invertWeights) {
        this.weightKey = weightKey;
        if (null == normalizeWeights)
            this.normalize = false;
        else
            this.normalize = normalizeWeights;
        if (null == invertWeights)
            this.invert = false;
        else
            this.invert = invertWeights;
    }

    public Double transform(final Edge edge) {
        Object object = edge.getProperty(this.weightKey);
        if (object instanceof Number) {
            Double weight = ((Number) object).doubleValue();
            if (this.invert)
                weight = 1.0d / weight;
            if (this.normalize) {
                return weight / JungHelper.totalWeight(edge.getVertex(Direction.OUT).getEdges(Direction.OUT), this.weightKey);
            } else {
                return weight;
            }
        } else {
            return 0.0d;
        }

    }
}
