package com.tinkerpop.blueprints.util.generators;

import java.util.Random;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public class NormalDistribution extends Distribution {
    
    private final double stdDeviation;
    private final double mean;
    
    public NormalDistribution(double stdDeviation) {
        this(stdDeviation,0.0);
    }
    
    public NormalDistribution(double stdDeviation, double mean) {
        if (stdDeviation<0) throw new IllegalArgumentException("Standard deviation must be non-negative: " + stdDeviation);
        if (mean<0) throw new IllegalArgumentException("Mean must be positive: " + mean);
        this.stdDeviation=stdDeviation;
        this.mean=mean;
    }
    
    @Override
    Distribution initialize(int numNodes, int numEdges) {
        double mean = (numEdges*1.0)/numNodes; //TODO: account for truncated gaussian distribution
        return new NormalDistribution(stdDeviation,mean);
    }

    @Override
    int getDegree(Random random) {
        if (mean==0.0) throw new IllegalStateException("Distribution has not been initialized");
        return (int)Math.round(random.nextGaussian()*stdDeviation+mean);
    }

    @Override
    int getConditionalDegree(Random random, int degree) {
        return getDegree(random);
    }
}
