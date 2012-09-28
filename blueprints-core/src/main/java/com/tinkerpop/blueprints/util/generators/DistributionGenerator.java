package com.tinkerpop.blueprints.util.generators;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public class DistributionGenerator extends AbstractGenerator {

    private Distribution outDistribution;
    private Distribution inDistribution;

    private boolean allowLoops = true;
    
    public DistributionGenerator(String label, EdgeAnnotator annotator) {
        super(label,annotator);
    }
    
    public DistributionGenerator(String label) {
        super(label);
    }
    
    public void setOutDistributions(Distribution distribution) {
        if (distribution==null) throw new NullPointerException();
        this.outDistribution=distribution;
    }

    public void setInDistributions(Distribution distribution) {
        if (distribution==null) throw new NullPointerException();
        this.inDistribution=distribution;
    }
    
    public void clearInDistribution() {
        this.inDistribution=null;
    }

    public boolean hasAllowLoops() {
        return allowLoops;
    }

    public void setAllowLoops(boolean allowLoops) {
        this.allowLoops=allowLoops;
    }

    
    public int generate(Graph graph, int expectedNumEdges) {
        return generate(graph,graph.getVertices(),expectedNumEdges);
    }
    
    public int generate(Graph graph, Iterable<Vertex> out, int expectedNumEdges) {
        return generate(graph,out,out,expectedNumEdges);
    }    
    
    public int generate(Graph graph, Iterable<Vertex> out, Iterable<Vertex> in, int expectedNumEdges) {
        if (outDistribution==null) throw new IllegalStateException("Must set out-distribution before generating edges");
        
        Distribution outDist = outDistribution.initialize(SizableIterable.sizeOf(out),expectedNumEdges);
        Distribution inDist = null;
        if (inDistribution==null) {
            if (out!=in) throw new IllegalArgumentException("Need to specify in-distribution");
            inDist = new CopyDistribution();
        } else {
            inDist = inDistribution.initialize(SizableIterable.sizeOf(in),expectedNumEdges);
        }

        long seed = System.currentTimeMillis()*177;
        Random outRandom = new Random(seed);
        ArrayList<Vertex> outStubs = new ArrayList<Vertex>(expectedNumEdges);
        for (Vertex v : out) {
            int degree = outDist.getDegree(outRandom);
            for (int i=0;i<degree;i++) {
                outStubs.add(v);
            }
        }
        
        Collections.shuffle(outStubs);
        
        outRandom = new Random(seed);
        Random inRandom = new Random(System.currentTimeMillis()*14421);
        int addedEdges = 0;
        int position = 0;
        for (Vertex v : in) {
            int degree = inDist.getConditionalDegree(inRandom, outDist.getDegree(outRandom));
            for (int i=0;i<degree;i++) {
                Vertex other = null;
                while (other==null) {
                    if (position>=outStubs.size()) return addedEdges; //No more edges to connect
                    other = outStubs.get(position);
                    position++;
                    if (!allowLoops && v.equals(other)) other=null;
                }
                //Connect edge
                addEdge(graph,other,v);
                addedEdges++;
            }
        }
        return addedEdges;
    }


}
