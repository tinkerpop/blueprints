package com.tinkerpop.blueprints.util.generators;

import java.util.Random;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public abstract class Distribution {

    
    abstract Distribution initialize(int numNodes, int numEdges);
    
    abstract int getDegree(Random random);
    
    abstract int getConditionalDegree(Random random, int degree);

}
