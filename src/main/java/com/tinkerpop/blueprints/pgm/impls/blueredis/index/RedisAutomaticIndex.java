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

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.impls.blueredis.RedisElement;
import com.tinkerpop.blueprints.pgm.impls.blueredis.RedisGraph;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.Set;

/**
 * @see RedisIndex
 * @see RedisIndexManager
 */

public class RedisAutomaticIndex<T extends Element> extends RedisIndex<T> implements AutomaticIndex<T> {

    public RedisAutomaticIndex(RedisGraph graph, String indexName, Class<T> indexClass, Set<String> keys) {
        super(graph, indexName, indexClass);
        if(null == keys){
            this.indexKeys = new HashSet<String>();
        } else {
            this.indexKeys = new HashSet<String>(keys);
            this.indexAll = false;
        }

        // convert any index name to a form that can be sent to redis, i.e.
        // convert "Tĥïŝ ĩš â fůňķŷ Šťŕĭńġ" to "this_is_a_funky_string"
        this.indexName = Normalizer.normalize(indexName, Normalizer.Form.NFD)
                                   .replaceAll("[^\\p{ASCII}]", "")
                                   .replaceAll(" ", "_")
                                   .toLowerCase();
        this.nodeName = RedisIndexKeys.AUTO +  this.indexName + ":";
    }

    public RedisAutomaticIndex(RedisGraph graph, final String indexName, final Class<T> indexClass) {
        this(graph, indexName, indexClass, new HashSet<String>());
    }

    @Override
    public Set<String> getAutoIndexKeys() {
        return this.indexKeys;
    }
}
