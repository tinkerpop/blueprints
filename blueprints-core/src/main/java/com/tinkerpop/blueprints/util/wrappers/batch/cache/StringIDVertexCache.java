package com.tinkerpop.blueprints.util.wrappers.batch.cache;

import com.tinkerpop.blueprints.Vertex;


/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public class StringIDVertexCache extends AbstractIDVertexCache {

    private final StringCompression compression;

    public StringIDVertexCache(final StringCompression compression) {
        super();
        if (compression == null) throw new IllegalArgumentException("Compression expected.");
        this.compression = compression;
    }

    public StringIDVertexCache() {
        this(StringCompression.NO_COMPRESSION);
    }

    @Override
    public Object getEntry(Object externalId) {
        return super.getEntry(compression.compress(externalId.toString()));
    }

    @Override
    public void setId(Object vertexId, Object externalId) {
        super.setId(vertexId, compression.compress(externalId.toString()));
    }

    @Override
    public boolean contains(Object externalId) {
        return super.contains(compression.compress(externalId.toString()));
    }
}
