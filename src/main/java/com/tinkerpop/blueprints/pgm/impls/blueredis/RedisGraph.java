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

import com.tinkerpop.blueprints.pgm.*;
import com.tinkerpop.blueprints.pgm.impls.blueredis.index.RedisAutomaticIndex;
import com.tinkerpop.blueprints.pgm.impls.blueredis.index.RedisIndex;
import com.tinkerpop.blueprints.pgm.impls.blueredis.index.RedisIndexManager;
import com.tinkerpop.blueprints.pgm.impls.blueredis.iterators.RedisEdgeIterable;
import com.tinkerpop.blueprints.pgm.impls.blueredis.iterators.RedisVertexIterable;
import com.tinkerpop.blueprints.pgm.util.AutomaticIndexHelper;
import org.jredis.JRedis;
import org.jredis.RedisException;
import org.jredis.ri.alphazero.JRedisClient;

import java.util.*;

public class RedisGraph implements IndexableGraph {

    private JRedis database = null;
    private boolean serializeProps = false; // if true, save type info with prop values
    private Index index;
    private boolean do_index = true;
    private RedisIndexManager indexManager;

    protected Map<String, RedisIndex> indices = new HashMap<String, RedisIndex>();
    protected Map<String, RedisAutomaticIndex> autoIndices = new HashMap<String, RedisAutomaticIndex>();

    public RedisGraph() {
        database = new JRedisClient();
    }

    public RedisGraph(String password) {
        database = new JRedisClient(password);
    }

    public RedisGraph(String host, int port) {
        database = new JRedisClient(host, port);
    }

    public RedisGraph(String host, int port, String password, int database) {
        this.database = new JRedisClient(host, port, password, database);
    }

    public void serializeProperties(boolean serialize) {
        serializeProps = serialize;
    }

    public boolean serializeProperties() {
        return serializeProps;
    }

    public void setIndexing(boolean b) {
        do_index = b;
        if(b == true){
            this.indexManager = new RedisIndexManager();
            this.prepareIndexing();
        }
    }

    public void setIndexing(boolean b, RedisIndexManager manager) {
        do_index = b;
        this.indexManager = manager;
        if(b == true){
            this.prepareIndexing();
        }
    }

    public boolean doIndexing(){
        return do_index;
    }

    public long nextVertexId(){
        try {
            return database.incr("globals:next_vertex_id");
        } catch(RedisException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public long nextEdgeId(){
        try {
            return database.incr("globals:next_edge_id");
        } catch(RedisException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public JRedis getDatabase(){
        return database;
    }

    @Override
    public Vertex addVertex(Object o) {
        final Vertex vertex = new RedisVertex(this);
        return vertex;
    }

    @Override
    public Vertex getVertex(Object o) {
        try{
            Long id = getLong(o);

            Object v = database.get("vertex:" + String.valueOf(id));

            if(v != null){
                final Vertex vertex = new RedisVertex(id, this);
                return vertex;
            }

            return null;
        } catch (Exception e){
            return null;
        }
    }

    @Override
    public void removeVertex(Vertex vertex) {
        if(this.doIndexing()) {
            this.indexManager.removeElement(vertex);
        }
        ((RedisElement)vertex).remove();
    }

    @Override
    public Iterable<Vertex> getVertices() {
        return new RedisVertexIterable(this);
    }

    @Override
    public Edge addEdge(Object o, Vertex outVertex, Vertex inVertex, String s) {
        final Edge edge = new RedisEdge((RedisVertex)inVertex, (RedisVertex)outVertex, s, this);
        return edge;
    }

    @Override
    public Edge getEdge(Object o) {
        try{
            Long id = getLong(o);

            Object e = database.get("edge:" + String.valueOf(id));

            if(e != null) {
                final Edge edge = new RedisEdge(id, this);
                return edge;
            }
            return null;
        } catch(Exception e){
            return null;
        }
    }

    @Override
    public void removeEdge(Edge edge) {
        if(this.doIndexing()) {
            this.indexManager.removeElement(edge);
        }
        ((RedisEdge) edge).remove();
    }

    @Override
    public Iterable<Edge> getEdges() {
        return new RedisEdgeIterable(this);
    }

    @Override
    public void clear() {
        try {
            database.flushdb();
            if(this.doIndexing() && null != this.indexManager){
                this.indexManager.clear();
                // this is hackish in the following sense
                // if we've dropped automatic indices, *do not* recreate them on next start
                // we only automatically create indices for fresh databases
                this.database.set("indexing_set", 1);
            }
        } catch(RedisException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shutdown() {
        database.quit();
    }

    public static String getIdentifier(String prefix, Long id, String suffix) {
        String identifier = prefix + String.valueOf(id);
        if(suffix != null) identifier += ":" + suffix;

        return identifier;
    }

    public String toString() {
        try {
            Map<String, String> info = this.database.info();
            return "redis[" + info.get("redis_version") + "]";
        } catch(RedisException e) {
            e.printStackTrace();
        }
        return "redis[error retrieving info]";
    }

    @Override
    public <T extends Element> Index<T> createManualIndex(final String indexName, final Class<T> indexClass) {
        if(!this.doIndexing()){
            return null;
        }
        return this.indexManager.createManualIndex(indexName, indexClass);
    }

    @Override
    public <T extends Element> AutomaticIndex<T> createAutomaticIndex(final String indexName, final Class<T> indexClass, Set<String> keys) {
        if(!this.doIndexing()){
            return null;
        }
        return this.indexManager.createAutomaticIndex(indexName, indexClass, keys);
    }

    @Override
    public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
        if(!this.doIndexing()){
            return null;
        }
        return this.indexManager.getIndex(indexName, indexClass);
    }

    @Override
    public Iterable<Index<? extends Element>> getIndices() {
        if(!this.doIndexing()){
            return new ArrayList<Index<? extends Element>>();
        }
        return this.indexManager.getIndices();
    }

    @Override
    public void dropIndex(final String indexName) {
        if(this.doIndexing()){
            this.indexManager.dropIndex(indexName);
        }
    }

    protected Iterable<RedisAutomaticIndex> getAutoIndices() {
        if(!this.doIndexing()){
            return new ArrayList<RedisAutomaticIndex>();
        }
        return this.indexManager.getAutoIndices();
    }

    protected Iterable<RedisIndex> getManualIndices() {
        if(!this.doIndexing()){
            return new ArrayList<RedisIndex>();
        }
        return  this.indexManager.getManualIndices();
    }

    private void prepareIndexing(){

        this.indexManager.setGraph(this);
        this.indexManager.restoreIndices();

        try {
            Object indexing_set = this.database.get("indexing_set");
            if(null == indexing_set){
                this.createAutomaticIndex(Index.VERTICES, RedisVertex.class, null);
                this.createAutomaticIndex(Index.EDGES, RedisEdge.class, null);
            }

            this.database.set("indexing_set", 1);
        } catch (RedisException e) {
            e.printStackTrace();
        }
    }

    // see http://stackoverflow.com/questions/1302605/how-do-i-convert-from-int-to-long-in-java/2904999#2904999
    private final Long getLong(Object obj) throws IllegalArgumentException {
        Long rv;

        if((obj.getClass() == Integer.class) || (obj.getClass() == Long.class) || (obj.getClass() == Double.class)) {
            rv = Long.parseLong(obj.toString());
        } else if((obj.getClass() == int.class) || (obj.getClass() == long.class) || (obj.getClass() == double.class)) {
            rv = (Long) obj;
        } else if(obj.getClass() == String.class) {
            rv = Long.parseLong(obj.toString());
        } else {
            throw new IllegalArgumentException("getLong: type " + obj.getClass() + " = \"" + obj.toString() + "\" unaccounted for");
        }

        return rv;
    }
}
