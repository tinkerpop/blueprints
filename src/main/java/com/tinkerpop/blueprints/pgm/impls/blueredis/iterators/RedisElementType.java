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

import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Vertex;

public class RedisElementType {
    public static enum TYPE {
        REDIS_ELEMENT_VERTEX,
        REDIS_ELEMENT_EDGE,
        REDIS_ELEMENT_EDGES_IN,
        REDIS_ELEMENT_EDGES_OUT
        //REDIS_ELEMENT_VERTEX_IN,        only one in/out vertex per edge
        //REDIS_ELEMENT_VERTEX_OUT        no need for an iterator
    }

    public static String key(TYPE type, Element element){
        if(null == element){
            if(type.equals(TYPE.REDIS_ELEMENT_VERTEX)) return "globals:vertices";
            if(type.equals(TYPE.REDIS_ELEMENT_EDGE)) return "globals:edges";
        } else {
            if(element instanceof Vertex){
                if(type.equals(TYPE.REDIS_ELEMENT_EDGES_IN))
                    return "vertex:".concat(element.getId().toString()).concat(":edges:in");
                if(type.equals(TYPE.REDIS_ELEMENT_EDGES_OUT))
                    return "vertex:".concat(element.getId().toString()).concat(":edges:out");
            }
            /*
            else {
                if(type.equals(TYPE.REDIS_ELEMENT_VERTEX_IN))
                    return "edge:".concat(element.getId().toString()).concat(":in");
                if(type.equals(TYPE.REDIS_ELEMENT_VERTEX_OUT))
                return "edge:".concat(element.getId().toString()).concat(":out");
            } */
        }
        return "";
    }
}
