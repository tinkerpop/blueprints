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

import com.tinkerpop.blueprints.pgm.impls.blueredis.iterators.RedisEdgeIterable;
import com.tinkerpop.blueprints.pgm.impls.blueredis.iterators.RedisElementType;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import org.jredis.RedisException;

public class RedisVertex extends RedisElement implements Vertex {

    public RedisVertex(RedisGraph db) {
        super(db, db.nextVertexId());

        try {
            this.graph.getDatabase().zadd("globals:vertices", id, String.valueOf(id));
            this.graph.getDatabase().set("vertex:" + String.valueOf(id), String.valueOf(id));
        } catch(RedisException e) {
            e.printStackTrace();
        }

    }

    public RedisVertex(Long id, RedisGraph db) {
        super(db, id);
    }

    @Override
    public Iterable<Edge> getOutEdges() {
        return new RedisEdgeIterable(RedisElementType.TYPE.REDIS_ELEMENT_EDGES_OUT, graph, this);
    }

    @Override
    public Iterable<Edge> getOutEdges(final String label) {
        return new RedisEdgeIterable(RedisElementType.TYPE.REDIS_ELEMENT_EDGES_OUT, graph, this, label);
    }

    @Override
    public Iterable<Edge> getInEdges() {
        return new RedisEdgeIterable(RedisElementType.TYPE.REDIS_ELEMENT_EDGES_IN, graph, this);
    }

    @Override
    public Iterable<Edge> getInEdges(final String label) {
        return new RedisEdgeIterable(RedisElementType.TYPE.REDIS_ELEMENT_EDGES_IN, graph, this, label);
    }

}
