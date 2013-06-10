package com.tinkerpop.blueprints.impls.tg;

import java.io.IOException;

/**
 * Implementations are responsible for loading and saving a TinkerGraph data.
 *
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
interface TinkerStorage {
    public TinkerGraph load(final String directory) throws IOException;

    public void save(final TinkerGraph graph, final String directory) throws IOException;
}
