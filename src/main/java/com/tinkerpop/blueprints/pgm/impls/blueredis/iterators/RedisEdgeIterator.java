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

public class RedisEdgeIterator extends RedisElementIterator {
    public RedisEdgeIterator(final RedisGraph graph, final long count) {
        this(RedisElementType.TYPE.REDIS_ELEMENT_EDGE, graph, count, null);
    }
    public RedisEdgeIterator(final RedisGraph graph, final long count, final RedisElement element) {
        this(RedisElementType.TYPE.REDIS_ELEMENT_EDGE, graph, count, element);
    }
    public RedisEdgeIterator(final RedisElementType.TYPE type, final RedisGraph graph, final long count, final RedisElement element) {
        super(type, graph, count, element);
    }
}
