package com.tinkerpop.blueprints.util.wrappers.batch.vertexcache;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public interface StringCompression {
    
    public static final StringCompression NO_COMPRESSION = new StringCompression() {
        @Override
        public String compress(String input) {
            return input;
        }
    };

    public String compress(String input);

}
