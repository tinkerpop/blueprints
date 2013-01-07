package com.tinkerpop.blueprints.util.wrappers.readonly;

import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.Parameter;

import java.util.Set;

/**
 * A ReadOnlyKeyIndexableGraph wraps a KeyIndexableGraph and overrides the underlying graph's mutating methods.
 * In this way, a ReadOnlyKeyIndexableGraph can only be read from, not written to.
 *
 * @author Darrick Wiebe (http://૯.com/)
 */
public class ReadOnlyKeyIndexableGraph<T extends KeyIndexableGraph> extends ReadOnlyIndexableGraph<IndexableGraph> implements KeyIndexableGraph {
    public ReadOnlyKeyIndexableGraph(final T baseKIGraph) {
        super((IndexableGraph) baseKIGraph);
    }

    /**
     * @throws UnsupportedOperationException
     */
    public <T extends Element> void dropKeyIndex(final String name, Class<T> elementClass) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(ReadOnlyTokens.MUTATE_ERROR_MESSAGE);
    }

    /**
     * @throws UnsupportedOperationException
     */
    public <T extends Element> void createKeyIndex(final String name, Class<T> elementClass, final Parameter... indexParameters) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(ReadOnlyTokens.MUTATE_ERROR_MESSAGE);
    }

    public <T extends Element> Set<String> getIndexedKeys(Class<T> elementClass) {
        return ((KeyIndexableGraph) this.baseGraph).getIndexedKeys(elementClass);
    }
}
