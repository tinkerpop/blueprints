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


package com.tinkerpop.blueprints.pgm.impls.blueredis.index;

import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.impls.blueredis.RedisEdge;
import com.tinkerpop.blueprints.pgm.impls.blueredis.RedisGraph;
import com.tinkerpop.blueprints.pgm.impls.blueredis.RedisVertex;
import org.apache.commons.codec.language.DoubleMetaphone;
import org.jredis.JRedis;
import org.jredis.RedisException;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @see RedisAutomaticIndex
 * @see RedisIndexManager
 */

public class RedisIndex<T extends Element> implements Index<T> {

    protected RedisGraph graph;
    protected JRedis database;

    protected String indexName;
    protected Class<T> indexClass;
    protected String nodeName;

    protected Set<String> indexKeys = new HashSet<String>();

    protected DoubleMetaphone metaphone = new DoubleMetaphone();

    protected boolean indexAll = true;

    protected Type indexType = Type.MANUAL;

    public RedisIndex(RedisGraph graph, final String indexName, final Class<T> indexClass) {
        this.graph = graph;
        this.database = graph.getDatabase();
        this.indexClass = indexClass;

        // convert any index name to a form that can be sent to redis, i.e.
        // convert "Tĥïŝ ĩš â fůňķŷ Šťŕĭńġ" to "this_is_a_funky_string"
        this.indexName = RedisIndex.normalizeName(indexName);
        this.nodeName = RedisIndexKeys.MANUAL + this.indexName + ":";
        this.metaphone.setMaxCodeLen(12);
    }
    public RedisIndex(RedisGraph graph, final String indexName, final Class<T> indexClass, Set<String> indexKeys) {
        this(graph, indexName, indexClass);
        this.indexKeys = indexKeys;
    }

    @Override
    public String getIndexName() {
        return this.indexName;
    }

    @Override
    public Class getIndexClass() {
        return this.indexClass;
    }

    @Override
    public Type getIndexType() {
        return this.indexType;
    }

    @Override
    public void put(final String key, final Object value, final T element) {
        if(!this.indexAll && !this.indexKeys.contains(key)) {
            return;
        }

        if (!this.indexClass.isAssignableFrom(element.getClass())) {
            return;
        }

        indexKeys.add(key);

        String val = getMetaphone(value);
        String node_name = this.nodeName + key + ":" + val;

        try {
            database.sadd(node_name, element.getId().toString());
        } catch(RedisException e) {
            e.printStackTrace();
        }

        if(this.indexAll){
            this.updateIndexKey(key);
        }
    }

    @Override
    public Iterable<T> get(String key, Object value) {
        if(!indexKeys.contains(key)){
            return new ArrayList<T>();
        }
        ArrayList<T> arr = new ArrayList<T>();

        String val = getMetaphone(value);
        String node_name;
        List<byte[]> l;

        try {
            node_name = this.nodeName + key + ":" + val;
            l = database.smembers(node_name);
            if(l != null) {
                for(byte[] o : l) {
                    Long idx = Long.parseLong(new String(o));
                    if(this.indexClass.isAssignableFrom(RedisVertex.class)){
                        arr.add((T)new RedisVertex(idx, graph));
                    } else {
                        arr.add((T)new RedisEdge(idx, graph));
                    }
                }
            }
        } catch(RedisException e) {
            e.printStackTrace();
        }

        return arr;
    }

    @Override
    public void remove(String key, Object value, T element) {
        if(value == null) return;

        if (!this.indexClass.isAssignableFrom(element.getClass())) {
            return;
        }

        String val = getMetaphone(value);
        String node_name = this.nodeName + key + ":" + val;

        try {
            database.srem(node_name, element.getId().toString());
        } catch(RedisException e) {
            e.printStackTrace();
        }
    }

    public void removeElement(Element element){
        if (!this.indexClass.isAssignableFrom(element.getClass())) {
            return;
        }

        // note: this may be VERY resource intensive (especially memory-wise)
        // this implementation retrieves all values from each key and attempts
        // to remove the current element from the key

        for(final String key : this.indexKeys){
            try {
                List<String> keys = database.keys(this.nodeName + "*");

                for(final String val : keys){
                    try{
                        database.srem(val, element.getId().toString());
                    } catch (RedisException ignore) {
                    }
                }
            } catch (RedisException ignore) {
            }
        }
    }

    public void autoUpdate(final String key, final Object newValue, final Object oldValue, final T element) {
        if (this.getIndexClass().isAssignableFrom(element.getClass()) && (this.indexAll || this.indexKeys.size() == 0 || this.indexKeys.contains(key))) {
            if (oldValue != null)
                this.remove(key, oldValue, element);
            this.put(key, newValue, element);
            if(this.indexAll){
                this.updateIndexKey(key);
            }
        }
    }

    public void autoRemove(final String key, final Object oldValue, final T element) {
        if (this.getIndexClass().isAssignableFrom(element.getClass()) && (this.indexAll || this.indexKeys.size() == 0 || this.indexKeys.contains(key))) {
            this.remove(key, oldValue, element);
        }
    }

    protected void updateIndexKey(String key){
        try{
            if(this.indexType.equals(Type.AUTOMATIC)){
                database.sadd(RedisIndexKeys.META_AUTO + this.indexName + ":keys", key);
            } else {
                database.sadd(RedisIndexKeys.META_MANUAL + this.indexName + ":keys", key);
            }
        } catch (RedisException ignore) {
        }

    }

    private String getMetaphone(Object o){
        String val = "";
        try{
            val = (String) metaphone.encode(String.valueOf(o));
        } catch(Exception ignored) {
        }

        if(val.equals("")) val = o.toString();

        return val;
    }

    public static String normalizeName(final String indexName){
        return Normalizer.normalize(indexName, Normalizer.Form.NFD)
                                   .replaceAll("[^\\p{ASCII}]", "")
                                   .replaceAll(" ", "_")
                                   .toLowerCase();
    }

    public void addKey(String key){
        this.indexKeys.add(key);
        this.updateIndexKey(key);
    }
}
