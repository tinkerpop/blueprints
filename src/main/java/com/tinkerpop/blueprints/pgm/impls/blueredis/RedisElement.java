/******************************************************************************
 * Copyright (c) 2010-2011. Dmitrii Dimandt <dmitrii@dmitriid.com>            *
 *                                                                            *
 *   Licensed under the Apache License, Version 2.0 (the "License");          *
 *   you may not use this file except in compliance with the License.         *
 *   You may obtain a copy of the License at                                  *
 *                                                                            *
 *       http://www.apache.org/licenses/LICENSE-2.0                           *
 *                                                                            *
 *   Unless required by applicable law or agreed to in writing, software      *
 *   distributed under the License is distributed on an "AS IS" BASIS,        *
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
 *   See the License for the specific language governing permissions and      *
 *   limitations under the License.                                           *
 ******************************************************************************/

package com.tinkerpop.blueprints.pgm.impls.blueredis;

import com.tinkerpop.blueprints.pgm.impls.blueredis.index.RedisAutomaticIndex;
import com.tinkerpop.blueprints.pgm.impls.blueredis.utils.Base64Coder;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import org.jredis.RedisException;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class RedisElement implements Element {
    protected Long id = null;
    protected RedisGraph graph = null;

    RedisElement(RedisGraph graph, Long id) {
        this.graph = graph;
        this.id = id;
    }

    @Override
    public Object getProperty(String s) {
        try {
            byte[] o = (byte[]) graph.getDatabase().hget(getIdentifier("properties"), s);

            if(o != null){
                if(graph.serializeProperties()){
                    return getObject(new String(o));
                } else {
                    return new String(o);
                }
            }
        } catch(RedisException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        } catch(ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Set<String> getPropertyKeys() {
        try {
            List<String> l = graph.getDatabase().hkeys(getIdentifier("properties"));
            if(l != null){
                return new HashSet<String>(l);
            }
        } catch(RedisException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setProperty(String s, Object o) {
        try {
            String val;
            if(graph.serializeProperties()){
                val = writeObject(o);
            } else {
                val = String.valueOf(o);
            }
            if(graph.doIndexing()){
                Object oldValue = this.getProperty(s);
                for (RedisAutomaticIndex index : this.graph.getAutoIndices()) {
                    index.autoUpdate(s, o, oldValue, this);
                }
            }
            graph.getDatabase().hset(getIdentifier("properties"), s, val);
        } catch(RedisException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object removeProperty(String s) {
        try {
            Object property = getProperty(s);
            if(graph.doIndexing()) {
                for (RedisAutomaticIndex index : this.graph.getAutoIndices()) {
                    index.autoRemove(s, property, this);
                }
            }
            graph.getDatabase().hdel(getIdentifier("properties"), s);
            return property;
        } catch(RedisException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Object getId() {
        return id;
    }

    public void remove() {
        if(this instanceof RedisVertex) {
            RedisVertex vertex = (RedisVertex) this;

            // 1. Remove all edges
            // 2. Remove self
            // 3. Remove from global registry

            Iterable<Edge> edges;

            edges = vertex.getInEdges();
            for(Edge edge : edges) {
                ((RedisEdge) edge).remove();
            }

            edges = vertex.getOutEdges();
            for(Edge edge : edges) {
                ((RedisEdge) edge).remove();
            }

            try {
                graph.getDatabase().del(getIdentifier(null));
                graph.getDatabase().del(getIdentifier("properties"));
                graph.getDatabase().del(getIdentifier("edges:in"));
                graph.getDatabase().del(getIdentifier("edges:out"));
                graph.getDatabase().del("vertex:" + String.valueOf(id));
                graph.getDatabase().zrem("globals:vertices", String.valueOf(id));

            } catch(RedisException e) {
                e.printStackTrace();
            }
        } else {
            RedisEdge edge = (RedisEdge) this;

            // 1. Remove all vertices
            // 2. Remove self
            // 3. Remove from global registry

            RedisVertex in = (RedisVertex) edge.getInVertex();
            RedisVertex out = (RedisVertex) edge.getOutVertex();

            try {
                graph.getDatabase().del(getIdentifier("in"));
                graph.getDatabase().del(getIdentifier("out"));
                graph.getDatabase().del(getIdentifier("label"));
                graph.getDatabase().del(getIdentifier("properties"));
                graph.getDatabase().del("edge:" + String.valueOf(id));

                graph.getDatabase().zrem(out.getIdentifier("edges:out"), String.valueOf(getId()));
                graph.getDatabase().zrem(in.getIdentifier("edges:in"), String.valueOf(getId()));

                graph.getDatabase().zrem("globals:edges", String.valueOf(id));
            } catch(RedisException e) {
                e.printStackTrace();
            }

        }
    }

    protected String getIdentifier(String suffix) {
        String prefix = this instanceof RedisVertex ? "vertex:" : "edge:";
        String identifier = prefix.concat(String.valueOf(id));
        if(suffix != null) identifier = identifier.concat(":").concat(suffix);

        return identifier;

    }
    
    public int hashCode() {
        return this.getId().hashCode();
    }

    public boolean equals(Object object) {
        return (this.getClass().equals(object.getClass()) && this.getId().equals(((Element) object).getId()));
    }

    private String writeObject(Object o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeObject(o);
        oos.close();

        return new String(Base64Coder.encode(baos.toByteArray()));
    }

    private Object getObject(String s) throws IOException, ClassNotFoundException {
        byte[] data = Base64Coder.decode(s);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
        Object o = ois.readObject();
        ois.close();
        return o;
    }

}
