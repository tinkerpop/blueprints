package com.tinkerpop.blueprints.util.generators;

import java.util.Random;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public class CopyDistribution extends Distribution {

    @Override
    Distribution initialize(int numNodes, int numEdges) {
        return this;
    }

    @Override
    int getDegree(Random random) {
        throw new UnsupportedOperationException();
    }

    @Override
    int getConditionalDegree(Random random, int degree) {
        return degree;
    }
}
