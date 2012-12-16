package com.tinkerpop.blueprints.util.wrappers.batch;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.MockTransactionalGraph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
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
    

    private static final String filename = "target"+File.separator+"testfile.tmp";
    
    public void testIntegration() throws IOException {
        int numVertices = 1000, outdegree = 10;
        final Random random = new Random();
        TinkerGraph g = new TinkerGraph();
        for (int i=0;i<numVertices;i++) {
            Vertex v = g.addVertex(i);
            v.setProperty("name","Vertex"+i);
        }
        
        for (int i=0;i<numVertices;i++) {
            Vertex v = g.getVertex(i);
            for (int d=0;d<outdegree;d++) {
                int other = i;
                while (other==i) other=random.nextInt(numVertices);
                Edge e = g.addEdge(null,v,g.getVertex(other),"knows");
                e.setProperty("time",random.nextInt(10000)+1);
            }
        }
        int numEdges = outdegree*numVertices;
        
        assertEquals(numVertices, BaseTest.count(g.getVertices()));
        assertEquals(numEdges, BaseTest.count(g.getEdges()));


        try {
            FileOutputStream out = new FileOutputStream(filename);
            DelimitedTripleWriter.outputGraph(g,out);
            out.close();
            
            DelimitedTripleLoader triples = new DelimitedTripleLoader(filename);
            assertEquals(numVertices+numEdges,BaseTest.count(triples));

            TransactionalGraph graph = new MockTransactionalGraph(new TinkerTransactionalGraph());
            ParallelGraphLoader loader = new ParallelGraphLoader(graph,VertexIDType.STRING);
            loader.configure(1,50);
            loader.setVertexIdKey("uid");
            loader.load(triples);

            assertEquals(numVertices, BaseTest.count(graph.getVertices()));
            assertEquals(numEdges, BaseTest.count(graph.getEdges()));


            
        } finally {
            File f = new File(filename);
            if (f.exists() && f.isFile()) f.delete();
        }
        
    }
    
    
}
