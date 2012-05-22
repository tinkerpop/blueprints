package com.tinkerpop.blueprints.util.wrappers.batch.cache;

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
