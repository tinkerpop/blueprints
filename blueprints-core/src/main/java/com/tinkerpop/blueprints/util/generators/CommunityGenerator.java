package com.tinkerpop.blueprints.util.generators;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

import java.util.*;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public class CommunityGenerator extends AbstractGenerator {
    
    private Distribution communitySize=null;
    private Distribution edgeDegree=null;
    private double crossCommunityPercentage = 0.1;
    
    private final Random random = new Random();
    
    public CommunityGenerator(String label, EdgeAnnotator annotator) {
        super(label, annotator);
    }

    public CommunityGenerator(String label) {
        super(label);
    }
    
    public void setCommunityDistribution(Distribution community) {
        this.communitySize=community;
    }
    
    public void setDegreeDistribution(Distribution degree) {
        this.edgeDegree=degree;
    }

    public int generate(Graph graph, int expectedNumCommunities, int expectedNumEdges) {
        return generate(graph,graph.getVertices(),expectedNumCommunities,expectedNumEdges);
    }

    public void setCrossCommunityPercentage(double percentage) {
        if (percentage<0.0 || percentage>1.0) throw new IllegalArgumentException("Percentage must be between 0 and 1");
        this.crossCommunityPercentage=percentage;
    }

    public double getCrossCommunityPercentage() {
        return crossCommunityPercentage;
    }


    public int generate(Graph graph, Iterable<Vertex> vertices, int expectedNumCommunities, int expectedNumEdges) {
        if (communitySize==null) throw new IllegalStateException("Need to initialize community size distribution");
        if (edgeDegree==null) throw new IllegalStateException("Need to initialize degree distribution");
        int numVertices = SizableIterable.sizeOf(vertices);
        Iterator<Vertex> iter = vertices.iterator();
        ArrayList<ArrayList<Vertex>> communities = new ArrayList<ArrayList<Vertex>>(expectedNumCommunities);
        Distribution communityDist = communitySize.initialize(expectedNumCommunities,numVertices);
        while (iter.hasNext()) {
            int nextSize = communityDist.getDegree(random);
            ArrayList<Vertex> community = new ArrayList<Vertex>(nextSize);
            for (int i=0;i<nextSize && iter.hasNext();i++) {
                community.add(iter.next());
            }
            if (!community.isEmpty()) communities.add(community);
        }

        double inCommunityPercentage = 1.0-crossCommunityPercentage;
        Distribution degreeDist = edgeDegree.initialize(numVertices,expectedNumEdges);
        if (crossCommunityPercentage>0 && communities.size()<2) throw new IllegalArgumentException("Cannot have cross links with only one community");
        int addedEdges = 0;
        
        //System.out.println("Generating links on communities: "+communities.size());

        for (ArrayList<Vertex> community : communities) {
            for (Vertex v : community) {
                int degree = degreeDist.getDegree(random);
                degree = Math.min(degree,(int)Math.ceil((community.size() - 1) / inCommunityPercentage)-1);
                Set<Vertex> inlinks = new HashSet<Vertex>();
                for (int i=0;i<degree;i++) {
                    Vertex selected = null;
                    if (random.nextDouble()<crossCommunityPercentage || (community.size()-1<=inlinks.size()) ) {
                        //Cross community
                        ArrayList<Vertex> othercomm = null;
                        while (othercomm==null) {
                            othercomm = communities.get(random.nextInt(communities.size()));
                            if (othercomm.equals(community)) othercomm=null;
                        }
                        selected=othercomm.get(random.nextInt(othercomm.size()));
                    } else {
                        //In community
                        while (selected==null) {
                            selected=community.get(random.nextInt(community.size()));
                            if (v.equals(selected) || inlinks.contains(selected)) selected=null;
                        }
                        inlinks.add(selected);
                    }
                    addEdge(graph,v,selected);
                    addedEdges++;
                }
            }
        }
        return addedEdges;
    }
    
}
