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

package com.tinkerpop.blueprints.pgm.impls.blueredis.iterators;

import com.tinkerpop.blueprints.pgm.impls.blueredis.RedisEdge;
import com.tinkerpop.blueprints.pgm.impls.blueredis.RedisElement;
import com.tinkerpop.blueprints.pgm.impls.blueredis.RedisGraph;
import com.tinkerpop.blueprints.pgm.impls.blueredis.RedisVertex;
import com.tinkerpop.blueprints.pgm.Element;
import org.jredis.JRedis;
import org.jredis.RedisException;

import java.util.Iterator;
import java.util.List;

public class RedisElementIterator implements Iterator {

    protected RedisGraph graph;
    private JRedis db;
    protected RedisElementType.TYPE type;
    protected RedisElement element;
    protected long count = 0, elementCount = 0, current = 0, currentElementIndex = 0;
    protected String label = null;
    protected RedisElementIterable iterable = null;

    private String elementCollectionKey;

    public RedisElementIterator(RedisElementType.TYPE type, RedisElementIterable iterable) {
        this.iterable = iterable;
        this.graph = iterable.getGraph();
        this.db = this.graph.getDatabase();
        this.label = iterable.getLabel();
        this.count = iterable.getCount();
        this.elementCount = iterable.getElementCount();
        this.elementCollectionKey = iterable.getElementCollectionKey();
        this.element = iterable.getElement();
        this.type = type;
    }

    @Override
    public boolean hasNext() {
        return current < count;
    }

    @Override
    public Object next() {
        List<byte[]> db_vertices = null;
        Element el = null;

        try {
            db_vertices = db.zrange(elementCollectionKey, current, current);
            long id = Long.parseLong(new String(db_vertices.get(0)));

            if(type.equals(RedisElementType.TYPE.REDIS_ELEMENT_VERTEX)){
                el = new RedisVertex(id, graph);
            }
            else{
                el = new RedisEdge(id, graph);
                if(null != this.label && !((RedisEdge)el).getLabel().equals(this.label)){
                    el = null;
                    while(true && this.currentElementIndex < this.elementCount){
                        this.currentElementIndex++;
                        db_vertices = db.zrange(elementCollectionKey, this.currentElementIndex, this.currentElementIndex);
                        id = Long.parseLong(new String(db_vertices.get(0)));

                        el = new RedisEdge(id, graph);
                        if(((RedisEdge)el).getLabel().equals(this.label)){
                            break;
                        }
                    }
                }
            }
        } catch(RedisException e) {
            e.printStackTrace();
        }

        current++;

        return el;
    }

    @Override
    public void remove() {

    }
}
