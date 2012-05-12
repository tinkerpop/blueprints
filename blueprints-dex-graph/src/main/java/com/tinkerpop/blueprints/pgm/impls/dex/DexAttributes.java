package com.tinkerpop.blueprints.pgm.impls.dex;

import java.util.HashMap;
import java.util.Map;

/**
 * Caches info about Dex attributes.
 *
 * @author <a href="http://www.sparsity-technologies.com">Sparsity
 *         Technologies</a>
 */
public class DexAttributes {
    /**
     * Dex attribute identifier --> {@link com.sparsity.dex.gdb.Attribute}
     */
    private static Map<Integer, com.sparsity.dex.gdb.Attribute> attrs = new HashMap<Integer, com.sparsity.dex.gdb.Attribute>();

    /**
     * Gets corresponding {@link com.sparsity.dex.gdb.Attribute} for the given attribute
     * identifier.
     *
     * @param g    Dex {@link com.sparsity.dex.gdb.Graph}.
     * @param attr Dex attribute identifier.
     * @return {@link com.sparsity.dex.gdb.Attribute} or null if the attribute does not exist.
     */
    public static com.sparsity.dex.gdb.Attribute getAttributeData(com.sparsity.dex.gdb.Graph g, int attr) {
        assert attr != com.sparsity.dex.gdb.Attribute.InvalidAttribute;

        com.sparsity.dex.gdb.Attribute adata = attrs.get(attr);
        if (adata == null) {
            adata = g.getAttribute(attr);
            if (adata != null) {
                attrs.put(attr, adata);
            }
        }
        return adata;
    }

    /**
     * Gets corresponding {@link com.sparsity.dex.gdb.Attribute} for the given attribute
     * identifier name.
     *
     * @param g    Dex {@link com.sparsity.dex.gdb.Graph}.
     * @param type Dex type identifier.
     * @param name Dex attribute identifier name.
     * @return {@link com.sparsity.dex.gdb.Attribute} or null if the attribute does not exist.
     */
    public static com.sparsity.dex.gdb.Attribute getAttributeData(com.sparsity.dex.gdb.Graph g, int type, String name) {
        assert name != null;

        com.sparsity.dex.gdb.Attribute adata = null;
        final Integer attr = g.findAttribute(type, name);
        if (attr != com.sparsity.dex.gdb.Attribute.InvalidAttribute) {
            adata = g.getAttribute(attr);
            assert adata != null;
            attrs.put(attr, adata);
        }
        return adata;
    }

    /**
     * Gets corresponding attribute identifier for the given attribute
     * identifier name.
     *
     * @param g    Dex {@link com.sparsity.dex.gdb.Graph}.
     * @param type Dex type identifier.
     * @param name Dex attribute identifier name.
     * @return The attribute identifier or {@link com.sparsity.dex.gdb.Attribute#InvalidAttribute} if the attribute
     *         does not exist.
     */
    public static int getAttributeId(com.sparsity.dex.gdb.Graph g, int type, String name) {
        assert name != null;

        final Integer attr = g.findAttribute(type, name);
        if (attr != com.sparsity.dex.gdb.Attribute.InvalidAttribute) {
            final com.sparsity.dex.gdb.Attribute adata = g.getAttribute(attr);
            assert adata != null;
            attrs.put(attr, adata);
        }
        return attr;
    }

    /**
     * Clears cached values.
     */
    public static void clear() {
        attrs.clear();
    }
}
