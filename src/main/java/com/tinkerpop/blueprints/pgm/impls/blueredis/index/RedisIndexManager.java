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

import com.tinkerpop.blueprints.pgm.*;
import com.tinkerpop.blueprints.pgm.impls.blueredis.RedisEdge;
import com.tinkerpop.blueprints.pgm.impls.blueredis.RedisGraph;
import com.tinkerpop.blueprints.pgm.impls.blueredis.RedisVertex;
import com.tinkerpop.blueprints.pgm.util.AutomaticIndexHelper;
import org.jredis.JRedis;
import org.jredis.RedisException;

import java.util.*;

/**
 * @see RedisIndex
 * @see RedisAutomaticIndex
 */

public class RedisIndexManager {

    protected RedisGraph graph = null;
    protected Map<String, Index<Element>> manualIndices = new HashMap<String, Index<Element>>();
    protected Map<String, AutomaticIndex<Element>> autoIndices = new HashMap<String, AutomaticIndex<Element>>();
    protected boolean restoreMode = false;  // when in restore mode, don't save data back to db

    public RedisIndexManager() {
    }

    public void setGraph(RedisGraph graph) {
        this.graph = graph;
    }

    public <T extends Element> Index<T> createManualIndex(final String indexName, final Class<T> indexClass){
        RedisIndex<T> idx = new RedisIndex<T>(this.graph, indexName, indexClass);

        String idxName = idx.getIndexName();

        this.manualIndices.put(idxName, (Index<Element>)idx);

        if(!this.restoreMode){
            this.saveIndexMeta(RedisIndexKeys.MANUAL, indexName, indexClass.getCanonicalName(), null);
        }

        return idx;
    }

    public <T extends Element> AutomaticIndex<T> createAutomaticIndex(final String indexName, final Class<T> indexClass, Set<String> keys){
        RedisAutomaticIndex<T> idx = new RedisAutomaticIndex<T>(this.graph, indexName, indexClass, keys);

        String idxName = idx.getIndexName();
        this.autoIndices.put(idxName, (AutomaticIndex<Element>)idx);
        if(!this.restoreMode){
            this.saveIndexMeta(RedisIndexKeys.AUTO, indexName, indexClass.getCanonicalName(), keys);
        }

        return idx;
    }

    public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
        Index index = this.manualIndices.get(indexName);
        if (null == index)
            index = this.autoIndices.get(indexName);

        if(null == index)
            throw new RuntimeException("No such index exists: " + indexName);
        if (!indexClass.isAssignableFrom(index.getIndexClass()))
            throw new RuntimeException(indexClass + " is not assignable from " + index.getIndexClass());
        else
            return (Index<T>) index;
    }

    public Iterable<Index<? extends Element>> getIndices() {
        List<Index<? extends Element>> list = new ArrayList<Index<? extends Element>>();
        for (Index index : manualIndices.values()) {
            list.add(index);
        }
        for (Index index : autoIndices.values()) {
            list.add(index);
        }
        return list;
    }

    public void dropIndex(final String indexName) {
        JRedis db = this.graph.getDatabase();

        try {
            List<String> keyList = db.keys(RedisIndexKeys.AUTO + indexName + ":*");
            for(String key : keyList){
                    db.del(key);
            }

            keyList = db.keys(RedisIndexKeys.META_AUTO + indexName + ":*");
            for(String key : keyList){
                    db.del(key);
            }

            db.lrem(RedisIndexKeys.META_INDICES_AUTO, indexName, 0);

            keyList = db.keys(RedisIndexKeys.MANUAL + indexName + ":*");
            for(String key : keyList){
                    db.del(key);
            }

            keyList = db.keys(RedisIndexKeys.META_MANUAL + indexName + ":*");
            for(String key : keyList){
                    db.del(key);
            }

            db.lrem(RedisIndexKeys.META_INDICES_MANUAL, indexName, 0);
        } catch (RedisException e) {
            e.printStackTrace();
        }

        this.manualIndices.remove(indexName);
        this.autoIndices.remove(indexName);

    }

    public Iterable<RedisAutomaticIndex> getAutoIndices() {
        List<RedisAutomaticIndex> list = new ArrayList<RedisAutomaticIndex>();
        for (Index index : autoIndices.values()) {
            list.add((RedisAutomaticIndex)index);
        }
        return list;
    }

    public Iterable<RedisIndex> getManualIndices() {
        List<RedisIndex> list = new ArrayList<RedisIndex>();
        for (Index index : manualIndices.values()) {
            list.add((RedisIndex)index);
        }
        return list;
    }

    private void saveIndexMeta(final String type, final String indexName, final String className, Set<String> keys){
        JRedis db = this.graph.getDatabase();

        try {
            if (type.equals(RedisIndexKeys.AUTO)) {
                db.lpush(RedisIndexKeys.META_INDICES_AUTO, indexName);
                db.set(RedisIndexKeys.META_AUTO + indexName + ":class", className);
                if(null != keys){
                    String key_list = RedisIndexKeys.META_AUTO + indexName + ":keys";
                    for(String key : keys){
                        db.lpush(key_list, key);
                    }
                }
            } else {
                db.lpush(RedisIndexKeys.META_INDICES_MANUAL, indexName);
                db.set(RedisIndexKeys.META_MANUAL + indexName + ":class", className);
            }
        } catch (RedisException e) {
            e.printStackTrace();
        }
    }

    public void restoreIndices(){
        this.restoreMode = true;
        this.restoreIndices(RedisIndexKeys.AUTO);
        this.restoreIndices(RedisIndexKeys.MANUAL);
        this.restoreMode = false;
    }

    public void restoreIndices(String type){
        JRedis db = this.graph.getDatabase();

        String metaIndices = type.equals(RedisIndexKeys.AUTO) ? RedisIndexKeys.META_INDICES_AUTO : RedisIndexKeys.META_INDICES_MANUAL;
        String metaType    = type.equals(RedisIndexKeys.AUTO) ? RedisIndexKeys.META_AUTO : RedisIndexKeys.META_MANUAL;

        try {
            List<byte[]> indices = db.lrange(metaIndices, 0, db.llen(metaIndices));

            if(null == indices) return;

            for(byte[] idx: indices){
                String indexName = new String(idx);

                String className = new String(db.get(metaType + indexName + ":class"));

                Class indexClass = Class.forName(className);

                List<byte[]> keys = db.lrange(metaType + indexName + ":keys", 0, db.llen(metaType + indexName + ":keys"));
                HashSet<String> indexKeys = new HashSet<String>();

                if(null != keys){
                    for(byte[] key : keys){
                        indexKeys.add(new String(key));
                    }
                }

                if(type.equals(RedisIndexKeys.AUTO)){
                    this.createAutomaticIndex(indexName, indexClass, indexKeys.size() != 0 ? indexKeys : null);
                } else {
                    this.createManualIndex(indexName, indexClass);
                }
            }
        } catch (RedisException e) {
            e.printStackTrace();
        }catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void removeElement(Element el){
        AutomaticIndexHelper.removeElement(this.graph, el);
        for (Index index : this.getManualIndices()) {
            if (Vertex.class.isAssignableFrom(index.getIndexClass())) {
                RedisIndex<RedisVertex> idx = (RedisIndex<RedisVertex>) index;
                idx.removeElement(el);
            } else if(Edge.class.isAssignableFrom(index.getIndexClass())) {
                RedisIndex<RedisEdge> idx = (RedisIndex<RedisEdge>) index;
                idx.removeElement(el);
            }
        }
    }
}
