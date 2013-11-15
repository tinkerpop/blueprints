package com.tinkerpop.blueprints.util.wrappers.batch;

import com.tinkerpop.blueprints.util.wrappers.batch.cache.LongIDVertexCache;
import com.tinkerpop.blueprints.util.wrappers.batch.cache.ObjectIDVertexCache;
import com.tinkerpop.blueprints.util.wrappers.batch.cache.StringIDVertexCache;
import com.tinkerpop.blueprints.util.wrappers.batch.cache.URLCompression;
import com.tinkerpop.blueprints.util.wrappers.batch.cache.VertexCache;

/**
 * Type of vertex ids expected by BatchGraph. The default is IdType.OBJECT.
 * Use the IdType that best matches the used vertex id types in order to save memory.
 *
 * @author Matthias Broecheler (http://www.matthiasb.com)
 */
public enum VertexIDType {

    OBJECT {
        @Override
        public VertexCache getVertexCache() {
            return new ObjectIDVertexCache();
        }
    },

    NUMBER {
        @Override
        public VertexCache getVertexCache() {
            return new LongIDVertexCache();
        }
    },

    STRING {
        @Override
        public VertexCache getVertexCache() {
            return new StringIDVertexCache();
        }
    },

    URL {
        @Override
        public VertexCache getVertexCache() {
            return new StringIDVertexCache(new URLCompression());

        }
    };

    public abstract VertexCache getVertexCache();
}