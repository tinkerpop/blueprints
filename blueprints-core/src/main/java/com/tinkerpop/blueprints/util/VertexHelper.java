package com.tinkerpop.blueprints.util;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class VertexHelper {

    /**
     * Test whether the two vertices have equal properties and edge sets.
     *
     * @param a               the first vertex
     * @param b               the second vertex
     * @param checkIdEquality whether to check on vertex and edge ids
     * @return whether the two vertices are semantically the same
     */
    public static boolean haveEqualNeighborhood(final Vertex a, final Vertex b, final boolean checkIdEquality) {
        if (checkIdEquality && !ElementHelper.haveEqualIds(a, b))
            return false;

        return ElementHelper.haveEqualProperties(a, b) && haveEqualEdges(a, b, checkIdEquality);
    }

    /**
     * Test whether the two vertices have equal edge sets
     *
     * @param a               the first vertex
     * @param b               the second vertex
     * @param checkIdEquality whether to check on vertex and edge ids
     * @return whether the two vertices have the same edge sets
     */
    public static boolean haveEqualEdges(final Vertex a, final Vertex b, boolean checkIdEquality) {
        Set<Edge> aEdgeSet = new HashSet<Edge>();
        for (Edge edge : a.getEdges(Direction.OUT)) {
            aEdgeSet.add(edge);
        }
        Set<Edge> bEdgeSet = new HashSet<Edge>();
        for (Edge edge : b.getEdges(Direction.OUT)) {
            bEdgeSet.add(edge);
        }

        if (!hasEqualEdgeSets(aEdgeSet, bEdgeSet, checkIdEquality))
            return false;

        aEdgeSet.clear();
        bEdgeSet.clear();

        for (Edge edge : a.getEdges(Direction.IN)) {
            aEdgeSet.add(edge);
        }
        for (Edge edge : b.getEdges(Direction.IN)) {
            bEdgeSet.add(edge);
        }
        return hasEqualEdgeSets(aEdgeSet, bEdgeSet, checkIdEquality);

    }

    private static boolean hasEqualEdgeSets(final Set<Edge> aEdgeSet, final Set<Edge> bEdgeSet, final boolean checkIdEquality) {
        if (aEdgeSet.size() != bEdgeSet.size())
            return false;

        for (Edge aEdge : aEdgeSet) {
            Edge tempEdge = null;
            for (Edge bEdge : bEdgeSet) {
                if (bEdge.getLabel().equals(aEdge.getLabel())) {
                    if (checkIdEquality) {
                        if (ElementHelper.haveEqualIds(aEdge, bEdge) &&
                                ElementHelper.haveEqualIds(aEdge.getVertex(Direction.IN), bEdge.getVertex(Direction.IN)) &&
                                ElementHelper.haveEqualIds(aEdge.getVertex(Direction.OUT), bEdge.getVertex(Direction.OUT)) &&
                                ElementHelper.haveEqualProperties(aEdge, bEdge)) {
                            tempEdge = bEdge;
                            break;
                        }
                    } else if (ElementHelper.haveEqualProperties(aEdge, bEdge)) {
                        tempEdge = bEdge;
                        break;
                    }
                }
            }
            if (tempEdge == null)
                return false;
            else
                bEdgeSet.remove(tempEdge);
        }
        return bEdgeSet.size() == 0;
    }
}
