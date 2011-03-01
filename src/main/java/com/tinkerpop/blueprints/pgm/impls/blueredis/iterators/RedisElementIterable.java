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

import com.tinkerpop.blueprints.pgm.impls.blueredis.RedisElement;
import com.tinkerpop.blueprints.pgm.impls.blueredis.RedisGraph;
import org.jredis.JRedis;
import org.jredis.RedisException;

public class RedisElementIterable {

    protected RedisElementType.TYPE type;
    protected RedisGraph graph;
    private JRedis db;
    protected long count = 0;
    protected RedisElement element;
    protected String elementCollectionKey;

    public RedisElementIterable(RedisElementType.TYPE type, RedisGraph graph) {
        this(type, graph, null);
    }
    public RedisElementIterable(RedisElementType.TYPE type, RedisGraph graph, RedisElement element) {
        this.type  = type;
        this.graph = graph;
        this.db    = graph.getDatabase();
        this.element = element;

        elementCollectionKey = RedisElementType.key(type, element);

        try {
            count = db.zcard(elementCollectionKey);
        } catch(RedisException e) {
            e.printStackTrace();
        }
    }

    public long count(){
        return count;
    }
}
