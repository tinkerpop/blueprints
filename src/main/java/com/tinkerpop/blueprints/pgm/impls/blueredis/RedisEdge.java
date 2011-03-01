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

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.blueredis.index.RedisAutomaticIndex;
import org.jredis.RedisException;

public class RedisEdge extends RedisElement implements Edge {

    private String label = null;
    private RedisVertex in, out;

    public RedisEdge(RedisVertex in, RedisVertex out, String label, RedisGraph db) {
        super(db, db.nextEdgeId());

        this.in = in;
        this.out = out;
        this.label = label;

        try {
            this.graph.getDatabase().set("edge:".concat(String.valueOf(id)).concat(":out"), String.valueOf(out.getId()));
            this.graph.getDatabase().set("edge:".concat(String.valueOf(id)).concat(":in"), String.valueOf(in.getId()));
            this.graph.getDatabase().set("edge:".concat(String.valueOf(id)).concat(":label"), label);

            this.graph.getDatabase().zadd("vertex:".concat(String.valueOf(in.getId())).concat(":edges:in"), id, String.valueOf(id));
            this.graph.getDatabase().zadd("vertex:".concat(String.valueOf(out.getId())).concat(":edges:out"), id, String.valueOf(id));

            this.graph.getDatabase().set("edge:" + String.valueOf(id), String.valueOf(id));
            this.graph.getDatabase().zadd("globals:edges", id, String.valueOf(id));

            for (RedisAutomaticIndex index : this.graph.getAutoIndices()) {
                index.autoUpdate(AutomaticIndex.LABEL, this.label, null, this);
            }

        } catch(RedisException e) {
            e.printStackTrace();
        }
    }

    public RedisEdge(Long id, RedisGraph db) {
        super(db, id);
        try {
            this.label = new String(db.getDatabase().get(getIdentifier("label")));
        } catch(RedisException e) {
            e.printStackTrace();
        }
    }


    @Override
    public Vertex getOutVertex() {
        try {
            byte[] b = graph.getDatabase().get(getIdentifier("out"));
            Long id = Long.parseLong(new String(b));
            return new RedisVertex(id, graph);
        } catch(RedisException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Vertex getInVertex() {
        try {
            byte[] b = graph.getDatabase().get(getIdentifier("in"));
            Long id = Long.parseLong(new String(b));
            return new RedisVertex(id, graph);
        } catch(RedisException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
