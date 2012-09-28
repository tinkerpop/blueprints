package com.tinkerpop.blueprints.util.generators;

import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import junit.framework.TestCase;

import java.util.Random;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public class DistributionGeneratorTest extends TestCase {
    
    public void testRandom() {
        long seed = System.currentTimeMillis();
        Random r1 = new Random(seed);
        Random r2 = new Random(seed);
        for (int i=0;i<1000;i++) {
            assertEquals(r1.nextGaussian(),r2.nextGaussian());
            assertEquals(r1.nextDouble(),r2.nextDouble());
            assertEquals(r1.nextInt(100),r2.nextInt(100));
        }
    }

    public void testDistributions() {
        int numNodes = 100;
        int numEdges = 1000;
        long seed = System.currentTimeMillis();
        Random random = new Random(seed);
        //normal
        Distribution n = new NormalDistribution(2);
        n = n.initialize(numNodes,numEdges);
        int degreeSum = 0;
        for (int i=0;i<numNodes;i++) {
            int degree=n.getDegree(random);
            degreeSum+=degree;
        }
        System.out.println(degreeSum);

        random = new Random(seed);
        n = new NormalDistribution(2);
        n = n.initialize(numNodes,numEdges);
        for (int i=0;i<numNodes;i++) {
            degreeSum-=n.getDegree(random);
        }
        assertEquals(0,degreeSum);

        //scale free
        n = new ScaleFreeDistribution(2.9);
        n = n.initialize(numNodes,numEdges);
        degreeSum = 0;
        for (int i=0;i<numNodes;i++) {
            int degree=n.getDegree(random);
            //System.out.println(degree);
            degreeSum+=degree;
        }
        System.out.println(degreeSum);

    }
    
    public void testGeneratorNormal1() {
        distributionGeneratorTest(new NormalDistribution(2), null);
    }


    public void testGeneratorNormal2() {
        distributionGeneratorTest(new NormalDistribution(2), new NormalDistribution(5));
    }

    public void testGeneratorScaleFree1() {
        distributionGeneratorTest(new ScaleFreeDistribution(2.9), null);
    }

    public void testGeneratorScaleFree2() {
        distributionGeneratorTest(new ScaleFreeDistribution(2.1), null);
    }

    public void testGeneratorScaleFree3() {
        distributionGeneratorTest(new ScaleFreeDistribution(3.9), null);
    }

    public void testGeneratorScaleFree4() {
        distributionGeneratorTest(new ScaleFreeDistribution(2.3), new ScaleFreeDistribution(2.8));
    }

    private void distributionGeneratorTest(Distribution indist, Distribution outdist) {
        int numNodes = 100;
        TinkerGraph graph = new TinkerGraph();
        for (int i=0;i<numNodes;i++) graph.addVertex(i);

        DistributionGenerator generator = new DistributionGenerator("knows");
        generator.setOutDistributions(indist);
        if (outdist!=null) generator.setOutDistributions(outdist);
        int numEdges = generator.generate(graph,numNodes*10);
        assertEquals(numEdges, SizableIterable.sizeOf(graph.getEdges()));
        System.out.println(graph);
//        for (Vertex v : graph.getVertices()) System.out.print(SizableIterable.sizeOf(v.getEdges(Direction.BOTH,"knows"))+",");
//        System.out.println();
    }

    public void testCommunityGenerator1() {
        communityGeneratorTest(new NormalDistribution(2),new ScaleFreeDistribution(2.4),0.1);
    }

    public void testCommunityGenerator2() {
        communityGeneratorTest(new NormalDistribution(2),new ScaleFreeDistribution(2.4),0.5);
    }

    public void testCommunityGenerator3() {
        communityGeneratorTest(new NormalDistribution(2),new NormalDistribution(4),0.5);
    }

    public void testCommunityGenerator4() {
        communityGeneratorTest(new NormalDistribution(2),new NormalDistribution(4),0.1);
    }

    public void testCommunityGenerator5() {
        communityGeneratorTest(new ScaleFreeDistribution(2.3),new ScaleFreeDistribution(2.4),0.2);
    }

    public void testCommunityGenerator6() {
        communityGeneratorTest(new ScaleFreeDistribution(2.3),new NormalDistribution(4),0.2);
    }



    private void communityGeneratorTest(Distribution community, Distribution degree, double crossPercentage) {
        int numNodes = 100;
        TinkerGraph graph = new TinkerGraph();
        for (int i=0;i<numNodes;i++) graph.addVertex(i);

        CommunityGenerator generator = new CommunityGenerator("knows");
        generator.setCommunityDistribution(community);
        generator.setDegreeDistribution(degree);
        generator.setCrossCommunityPercentage(crossPercentage);
        int numEdges = generator.generate(graph,numNodes/10,numNodes*10);
        assertEquals(numEdges, SizableIterable.sizeOf(graph.getEdges()));
        System.out.println(graph);
        //        for (Vertex v : graph.getVertices()) System.out.print(SizableIterable.sizeOf(v.getEdges(Direction.BOTH,"knows"))+",");
//        System.out.println();
    }
    
}
