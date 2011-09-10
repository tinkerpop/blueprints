/**
 *
 */
package com.tinkerpop.blueprints.pgm.impls.dex;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.CloseableSequence;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.impls.dex.util.DexAttributes;
import com.tinkerpop.blueprints.pgm.impls.dex.util.DexTypes;
import edu.upc.dama.dex.core.Graph;
import edu.upc.dama.dex.core.Graph.AttributeData;
import edu.upc.dama.dex.core.Objects;
import edu.upc.dama.dex.core.Value;

import java.util.HashSet;
import java.util.Set;

/**
 * {@link AutomaticIndex} implementation for DEX.
 * <p/>
 * There is an index for each node or edge type. Each of the attributes of the
 * node or edge type corresponds to a key of the index. Thus, indexName
 * corresponds to the vertex or edge type (label) and it has as many keys as
 * attributes for this vertex or edge type exist.
 *
 * @author <a href="http://www.sparsity-technologies.com">Sparsity
 *         Technologies</a>
 */
public class DexAutomaticIndex<T extends Element> implements AutomaticIndex<T> {

    private DexGraph graph = null;
    private Class<T> clazz = null;
    private int type = Graph.INVALID_TYPE;
    private String name = null;

    public DexAutomaticIndex(final DexGraph g, final Class<T> clazz, final int type) {
        assert type != Graph.INVALID_ATTRIBUTE;

        this.graph = g;
        this.clazz = clazz;
        this.type = type;
        this.name = DexTypes.getTypeData(graph.getRawGraph(), type).getName();
    }

    @Override
    public String getIndexName() {
        return name;
    }

    @Override
    public Class<T> getIndexClass() {
        return clazz;
    }

    @Override
    public com.tinkerpop.blueprints.pgm.Index.Type getIndexType() {
        return Type.AUTOMATIC;
    }

    @Override
    public void put(final String key, final Object value, final T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CloseableSequence<T> get(final String key, final Object value) {
        return new DexIterable<T>(graph, rawGet(key, value), clazz);
    }

    @Override
    public long count(final String key, final Object value) {
        return rawGet(key, value).size();
    }

    @Override
    public void remove(final String key, final Object value, final T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getAutoIndexKeys() {
        Set<String> ret = new HashSet<String>();
        for (Long attr : graph.getRawGraph().getAttributesFromType(type)) {
            ret.add(DexAttributes.getAttributeData(graph.getRawGraph(), attr).getName());
        }
        return ret;
    }

    private Objects rawGet(final String key, final Object value) {
        long attr = DexAttributes.getAttributeId(graph.getRawGraph(), type, key);
        if (attr == Graph.INVALID_ATTRIBUTE) {
            throw new IllegalArgumentException(key + " is not a valid key");
        }

        AttributeData adata = DexAttributes.getAttributeData(this.graph.getRawGraph(), attr);
        Value v = new Value();
        switch (adata.getDatatype()) {
            case Value.BOOL:
                v.setBool((Boolean) value);
                break;
            case Value.INT:
                v.setInt((Integer) value);
                break;
            case Value.LONG:
                v.setLong((Long) value);
                break;
            case Value.STRING:
                v.setString((String) value);
                break;
            case Value.DOUBLE:
                if (value instanceof Double) {
                    v.setDouble((Double) value);
                } else if (value instanceof Float) {
                    v.setDouble(((Float) value).doubleValue());
                }
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return graph.getRawGraph().select(attr, Graph.OPERATION_EQ, v);
    }
}
