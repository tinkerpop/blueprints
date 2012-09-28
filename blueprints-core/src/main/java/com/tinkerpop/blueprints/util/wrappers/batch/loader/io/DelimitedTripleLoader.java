package com.tinkerpop.blueprints.util.wrappers.batch.loader.io;

import com.tinkerpop.blueprints.util.io.triple.DelimitedTripleWriter;
import com.tinkerpop.blueprints.util.wrappers.batch.loader.Debugger;
import com.tinkerpop.blueprints.util.wrappers.batch.loader.StandardEdge;
import com.tinkerpop.blueprints.util.wrappers.batch.loader.StandardProperty;
import com.tinkerpop.blueprints.util.wrappers.batch.loader.Triple;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public class DelimitedTripleLoader extends Debugger implements Iterable<Triple> {

    private final String filename;
    private final String tokenDelimiter;
    private final String propertyDelimiter;

    public DelimitedTripleLoader(String filename) {
        this(filename, DelimitedTripleWriter.TOKEN_DELIMITER_DEFAULT,DelimitedTripleWriter.PROPERTY_DELIMITER_DEFAULT);
    }
    
    public DelimitedTripleLoader(String filename, String tokenDelimiter, String propertyDelimiter) {
        if (filename==null || filename.isEmpty() || !(new File(filename).isFile()))
            throw new IllegalArgumentException("File does not exist: " + filename);
        if (tokenDelimiter==null || tokenDelimiter.isEmpty())
            throw new IllegalArgumentException("Invalid token delimiter: " + tokenDelimiter);
        if (propertyDelimiter==null || propertyDelimiter.isEmpty()) {
            throw new IllegalArgumentException("Invalid property delimiter: " + propertyDelimiter);
        }
        this.filename=filename;
        if (tokenDelimiter.equals("\t")) tokenDelimiter="\\t";
        this.tokenDelimiter=tokenDelimiter;
        this.propertyDelimiter=propertyDelimiter;
    }
    
    
    @Override
    public Iterator<Triple> iterator() {
        try {
        return new Iterator<Triple>() {

            private final Scanner in = new Scanner(new File(filename));
            private Triple triple = findNext();

            private Triple findNext() {
                while (in.hasNext()) {
                    String[] line = in.nextLine().split(tokenDelimiter);
                    if (line.length<3) {
                        debug("Ignored line: ",line);
                        continue;
                    }
                    String type = line[1];
                    if (type.startsWith(DelimitedTripleWriter.PROPERTY_KEY_PREFIX)) {
                        type = type.substring(DelimitedTripleWriter.PROPERTY_KEY_PREFIX.length());
                        if (line.length>3) debug("Ignored additional tokens in line: ",line);
                        Object vertexid = getVertexID(line[0]);
                        return new StandardProperty(vertexid,type,parseValue(line[2]));
                    } else {
                        Object outVertexId = getVertexID(line[0]);
                        Object inVertexId = getVertexID(line[2]);
                        if (line.length>3) {
                            //Parse additional properties
                            int offset = 3;
                            String[] keys = new String[line.length-offset];
                            Object[] values = new Object[line.length-offset];
                            boolean failed = false;
                            for (int i=offset;i<line.length;i++) {
                                String[] property = line[i].split(propertyDelimiter);
                                if (property.length!=2) {
                                    failed=true; break;
                                }
                                keys[i-offset]=property[0];
                                values[i-offset]=parseValue(property[1]);
                            }
    
                            if (failed) debug("Ignored line due to failure in parsing properties: ",line);
                            else return new StandardEdge(outVertexId,type,inVertexId,keys,values);
                        } else {
                            return new StandardEdge(outVertexId,type,inVertexId);
                        }
                    }

                }
                in.close();
                return null;
            }
            
            private Object parseValue(String value) {
                try {
                    return Long.parseLong(value);
                } catch (NumberFormatException e) {}
                try {
                    return Double.parseDouble(value);
                } catch (NumberFormatException e) {}
                return value;
            }
            
            private Object getVertexID(String id) {
                try {
                    return Long.parseLong(id);
                } catch (NumberFormatException e) {
                    return id;
                }
            }
            
            @Override
            public boolean hasNext() {
                return triple!=null;
            }

            @Override
            public Triple next() {
                if (!hasNext()) throw new NoSuchElementException();
                Triple returnTriple = triple;
                triple = findNext();
                return returnTriple;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        
        };
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not open file " + filename,e);
        }
    }

}
