package com.tinkerpop.blueprints.util.generators;

import java.util.Random;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public class ScaleFreeDistribution extends Distribution {

    private final double beta;
    private final double multiplier;

    public ScaleFreeDistribution(double beta) {
        this(beta,0.0);
    }

    private ScaleFreeDistribution(double beta, double multiplier) {
        if (beta<=2.0) throw new IllegalArgumentException("Beta must be bigger than 2: " + beta);
        if (multiplier<0) throw new IllegalArgumentException("Invalid multiplier value: " + multiplier);
        this.beta=beta;
        this.multiplier=multiplier;
    }

    @Override
    Distribution initialize(int numNodes, int numEdges) {
        double multiplier = numEdges/((beta-1)/(beta-2) * numNodes) * 2; //times two because we are generating stubs
        assert multiplier>0;
        return new ScaleFreeDistribution(beta,multiplier);
    }

    @Override
    int getDegree(Random random) {
        if (multiplier==0.0) throw new IllegalStateException("Distribution has not been initialized");
        return getValue(random,multiplier,beta);
    }

    @Override
    int getConditionalDegree(Random random, int degree) {
        return getDegree(random);
    }
    
    public static int getValue(Random random, double multiplier, double beta) {
        return (int)Math.round(multiplier*(Math.pow(1.0/(1.0-random.nextDouble()), 1.0/(beta-1.0))-1.0));
    }
}
