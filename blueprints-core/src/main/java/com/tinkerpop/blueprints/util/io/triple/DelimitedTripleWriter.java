package com.tinkerpop.blueprints.util.io.triple;

import com.tinkerpop.blueprints.*;

import java.io.*;
import java.nio.charset.Charset;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public class DelimitedTripleWriter {

    private static final String NEWLINE = "\n";
    
    public static final String TOKEN_DELIMITER_DEFAULT = "\t";
    public static final String PROPERTY_DELIMITER_DEFAULT = "=";
    public static final String PROPERTY_KEY_PREFIX = "_";
    
    private final String tokenDelimiter = TOKEN_DELIMITER_DEFAULT;
    private final String propertyDelimiter = PROPERTY_DELIMITER_DEFAULT;
    
    private Graph graph;
    private String vertexIdKey = null;
    private boolean addEdgeId = false;
    
    public DelimitedTripleWriter(Graph graph) {
        if (graph==null) throw new IllegalArgumentException("Need to provide graph");
        this.graph=graph;
    }
    
    public static final void outputGraph(final Graph graph, final OutputStream out) throws IOException {
        DelimitedTripleWriter writer = new DelimitedTripleWriter(graph);
        writer.outputGraph(out);
    }

    private Object getID(Vertex v) {
        Object id = null;
        if (vertexIdKey!=null) id = v.getProperty(vertexIdKey);
        else id = v.getId();
        if (id==null) throw new IllegalArgumentException("Vertex id cannot be retrieved: " + v);
        return id;
    }
    
    private String getValue(Element e, String key) {
        Object value = e.getProperty(key);
        return value==null?"null":value.toString();
    }
    
    public void outputGraph(final OutputStream gMLOutputStream) throws IOException {
        final Writer writer = new BufferedWriter(new OutputStreamWriter(gMLOutputStream, Charset.forName("ISO-8859-1")));
        for (Vertex v : graph.getVertices()) {
            for (String key : v.getPropertyKeys()) {
                if (key.equals(vertexIdKey)) continue;
                writer.write(getID(v).toString());
                writer.write(tokenDelimiter);
                writer.write(PROPERTY_KEY_PREFIX); writer.write(key);
                writer.write(tokenDelimiter);
                writer.write(getValue(v,key));
                writer.write(NEWLINE);
            }
        }
        for (Edge e : graph.getEdges()) {
            writer.write(getID(e.getVertex(Direction.OUT)).toString());
            writer.write(tokenDelimiter);
            writer.write(e.getLabel());
            writer.write(tokenDelimiter);
            writer.write(getID(e.getVertex(Direction.IN)).toString());
            if (addEdgeId && e.getId()!=null) {
                writer.write(tokenDelimiter);
                writer.write("id"); 
                writer.write(propertyDelimiter);
                writer.write(e.getId().toString());
            }
            for (String key : e.getPropertyKeys()) {
                writer.write(tokenDelimiter);
                writer.write(key); 
                writer.write(propertyDelimiter);
                writer.write(getValue(e,key));
            }
            writer.write(NEWLINE);
        }


        writer.flush();
        writer.close();
    }



}
