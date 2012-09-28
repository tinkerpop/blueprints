package com.tinkerpop.blueprints.util.wrappers.batch;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.MockTransactionalGraph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.generators.DistributionGenerator;
import com.tinkerpop.blueprints.util.generators.EdgeAnnotator;
import com.tinkerpop.blueprints.util.generators.ScaleFreeDistribution;
import com.tinkerpop.blueprints.util.generators.SizableIterable;
import com.tinkerpop.blueprints.util.io.triple.DelimitedTripleWriter;
import com.tinkerpop.blueprints.util.wrappers.batch.loader.ParallelGraphLoader;
import com.tinkerpop.blueprints.util.wrappers.batch.loader.io.DelimitedTripleLoader;
import com.tinkerpop.blueprints.util.wrappers.event.TinkerTransactionalGraph;
import junit.framework.TestCase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public class ParallelGraphLoaderTest extends TestCase {
    
    private static final String filename = "./target/testfile.tmp";
    
    public void testIntegration() throws IOException {
        int numNodes = 1000;
        final Random random = new Random();
        TinkerGraph g = new TinkerGraph();
        for (int i=0;i<numNodes;i++) {
            Vertex v = g.addVertex("v"+(i+1));
            v.setProperty("name","Vertex"+(i+1));
        }

        
        DistributionGenerator generator = new DistributionGenerator("knows", new EdgeAnnotator() {
            @Override
            public void annotate(Edge edge) {
                edge.setProperty("time",random.nextInt(10000)+1);
            }
        });
        generator.setOutDistribution(new ScaleFreeDistribution(2.1));
        int numEdges = generator.generate(g,numNodes*10);
        
        assertEquals(numNodes, SizableIterable.sizeOf(g.getVertices()));
        assertEquals(numEdges, SizableIterable.sizeOf(g.getEdges()));

        try {
            FileOutputStream out = new FileOutputStream(filename);
            DelimitedTripleWriter.outputGraph(g,out);
            out.close();
            
            DelimitedTripleLoader triples = new DelimitedTripleLoader(filename);
            assertEquals(numNodes+numEdges,SizableIterable.sizeOf(triples));

            TransactionalGraph graph = new MockTransactionalGraph(new TinkerTransactionalGraph());
            ParallelGraphLoader loader = new ParallelGraphLoader(graph,VertexIDType.STRING);
            loader.configure(1,50);
            loader.setVertexIdKey("uid");
            loader.load(triples);

            assertEquals(numNodes, SizableIterable.sizeOf(graph.getVertices()));
            assertEquals(numEdges, SizableIterable.sizeOf(graph.getEdges()));

            
        } finally {
            File f = new File(filename);
            if (f.exists() && f.isFile()) f.delete();
        }
        
    }
    
    
}
