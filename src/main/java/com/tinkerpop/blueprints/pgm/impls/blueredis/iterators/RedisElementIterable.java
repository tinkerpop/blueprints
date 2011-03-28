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

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.impls.blueredis.RedisEdge;
import com.tinkerpop.blueprints.pgm.impls.blueredis.RedisElement;
import com.tinkerpop.blueprints.pgm.impls.blueredis.RedisGraph;
import org.jredis.JRedis;
import org.jredis.RedisException;

public class RedisElementIterable {

    protected RedisElementType.TYPE type;
    protected RedisGraph graph;
    private JRedis db;
    protected long count = 0, elementCount = 0;
    protected RedisElement element;
    protected String elementCollectionKey;
    protected String label = null;

    public RedisElementIterable(RedisElementType.TYPE type, RedisGraph graph, RedisElement element) {
        this.type  = type;
        this.graph = graph;
        this.db    = graph.getDatabase();
        this.element = element;

        elementCollectionKey = RedisElementType.key(type, element);

        try {
            this.count = this.elementCount = this.db.zcard(elementCollectionKey);
        } catch(RedisException e) {
            e.printStackTrace();
        }
    }

    public RedisElementIterable(RedisElementType.TYPE type, RedisGraph graph, RedisElement element, final String label) {
        this(type, graph, element);
        this.label = label;

        if(null != this.label){
            RedisEdgeIterable itr = new RedisEdgeIterable(type, graph, element);

            this.count = 0;
            for(Edge e : itr){
                if(e.getLabel().equals(this.label)){
                    this.count++;
                }
            }
        }
    }
    public long count(){
        return count;
    }


    /* used in iterators */

    public RedisGraph getGraph() {
        return graph;
    }

    public long getCount() {
        return count;
    }

    public long getElementCount() {
        return elementCount;
    }

    public String getLabel() {
        return label;
    }

    public RedisElement getElement() {
        return element;
    }

    public String getElementCollectionKey() {
        return elementCollectionKey;
    }
}
