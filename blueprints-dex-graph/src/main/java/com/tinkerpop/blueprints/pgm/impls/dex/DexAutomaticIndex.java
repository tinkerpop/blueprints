/**
 *
 */
package com.tinkerpop.blueprints.pgm.impls.dex;

import java.util.HashSet;
import java.util.Set;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.CloseableSequence;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.impls.dex.util.DexAttributes;
import com.tinkerpop.blueprints.pgm.impls.dex.util.DexTypes;

/**
 * {@link AutomaticIndex} implementation for Dex.
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
    private int type = com.sparsity.dex.gdb.Type.InvalidType;
    private String name = null;

    public DexAutomaticIndex(final DexGraph g, final Class<T> clazz, final int type) {
        assert type != com.sparsity.dex.gdb.Type.InvalidType;

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
        com.sparsity.dex.gdb.AttributeList alist = graph.getRawGraph().findAttributes(type);
        for (Integer attr : alist) {
            ret.add(DexAttributes.getAttributeData(graph.getRawGraph(), attr).getName());
        }
        alist = null;
        return ret;
    }

    private com.sparsity.dex.gdb.Objects rawGet(final String key, final Object value) {
        int attr = DexAttributes.getAttributeId(graph.getRawGraph(), type, key);
        if (attr == com.sparsity.dex.gdb.Attribute.InvalidAttribute) {
            throw new IllegalArgumentException(key + " is not a valid key");
        }

        com.sparsity.dex.gdb.Attribute adata = DexAttributes.getAttributeData(this.graph.getRawGraph(), attr);
        com.sparsity.dex.gdb.Value v = new com.sparsity.dex.gdb.Value();
        switch (adata.getDataType()) {
            case Boolean:
                v.setBooleanVoid((Boolean) value);
                break;
            case Integer:
                v.setIntegerVoid((Integer) value);
                break;
            case Long:
                v.setLongVoid((Long) value);
                break;
            case String:
                v.setStringVoid((String) value);
                break;
            case Double:
                if (value instanceof Double) {
                    v.setDoubleVoid((Double) value);
                } else if (value instanceof Float) {
                    v.setDoubleVoid(((Float) value).doubleValue());
                }
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return graph.getRawGraph().select(attr, com.sparsity.dex.gdb.Condition.Equal, v);
    }
}
