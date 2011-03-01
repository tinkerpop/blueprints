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
import com.tinkerpop.blueprints.pgm.Edge;

import java.util.Iterator;

public class RedisEdgeIterable extends RedisElementIterable implements Iterable<Edge>{
    public RedisEdgeIterable(final RedisGraph graph) {
        this(RedisElementType.TYPE.REDIS_ELEMENT_EDGE, graph, null);
    }

    public RedisEdgeIterable(final RedisElementType.TYPE type, final RedisGraph graph) {
        this(type, graph, null);
    }

    public RedisEdgeIterable(final RedisElementType.TYPE type, final RedisGraph graph, final RedisElement element) {
        super(type, graph, element);
    }

    @Override
    public Iterator iterator() {
        return new RedisEdgeIterator(type, graph, count, element);
    }
}
