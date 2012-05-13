package com.tinkerpop.blueprints.oupls.jung;

import com.tinkerpop.blueprints.Edge;
import org.apache.commons.collections15.Transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class JungHelper {

    public static Double totalWeight(final Iterable<Edge> edges, final String weightKey) {
        double total = 0.0d;
        for (Edge edge : edges) {
            Object weight = edge.getProperty(weightKey);
            if (null != weight && weight instanceof Number) {
                total = total + ((Number) weight).doubleValue();
            }
        }
        return total;
    }

    public static List<Edge> filterEdgeLabels(final Iterable<Edge> edges, final Set<String> labels, final boolean filter) {
        List<Edge> returnEdges = new ArrayList<Edge>();
        for (Edge edge : edges) {
            if (labels.contains(edge.getLabel())) {
                if (!filter) {
                    returnEdges.add(edge);
                }
            } else {
                if (filter) {
                    returnEdges.add(edge);
                }
            }
        }
        return returnEdges;
    }

    public static Transformer<Edge, Number> makeTransformer(final Set<String> labels, final Boolean filterLabels, final Number filterValue, final Boolean probability, final String weightKey, final Boolean normalizeWeights, final Boolean invertWeights) {
        if (labels != null && weightKey != null) {
            return new EdgeLabelWeightTransformer(labels, filterLabels, weightKey, normalizeWeights, invertWeights);
        } else if (labels != null) {
            return new EdgeLabelTransformer(labels, filterLabels, filterValue, probability);
        } else if (weightKey != null) {
            return new EdgeWeightTransformer(weightKey, normalizeWeights, invertWeights);
        } else {
            return null;
        }
    }

}
