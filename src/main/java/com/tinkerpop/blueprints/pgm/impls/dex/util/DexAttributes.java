package com.tinkerpop.blueprints.pgm.impls.dex.util;

import edu.upc.dama.dex.core.Graph;
import edu.upc.dama.dex.core.Graph.AttributeData;

import java.util.HashMap;
import java.util.Map;

/**
 * Caches info about DEX attributes.
 *
 * @author <a href="http://www.sparsity-technologies.com">Sparsity
 *         Technologies</a>
 */
public class DexAttributes {
    /**
     * DEX attribute identifier --> {@link AttributeData}
     */
    private static Map<Long, AttributeData> attrs = new HashMap<Long, AttributeData>();
    /**
     * Attribute name --> DEX attribute identifier
     */
    private static Map<String, Long> names = new HashMap<String, Long>();

    /**
     * Gets corresponding {@link AttributeData} for the given attribute
     * identifier.
     *
     * @param g    DEX Graph.
     * @param attr DEX attribute identifier.
     * @return {@link AttributeData} or null if the attribute does not exist.
     */
    public static AttributeData getAttributeData(Graph g, long attr) {
        assert attr != Graph.INVALID_ATTRIBUTE;

        AttributeData adata = attrs.get(attr);
        if (adata == null) {
            adata = g.getAttributeData(attr);
            if (adata != null) {
                attrs.put(attr, adata);
                names.put(adata.getName(), attr);
            }
        }
        return adata;
    }

    /**
     * Gets corresponding {@link AttributeData} for the given attribute
     * identifier name.
     *
     * @param g    DEX Graph.
     * @param type DEX type identifier.
     * @param name DEX attribute identifier name.
     * @return {@link AttributeData} or null if the attribute does not exist.
     */
    public static AttributeData getAttributeData(Graph g, int type, String name) {
        assert name != null;

        AttributeData adata = null;
        Long attr = names.get(name);
        if (attr == null) {
            attr = g.findAttribute(type, name);
            if (attr != Graph.INVALID_ATTRIBUTE) {
                adata = g.getAttributeData(attr);
                assert adata != null;
                attrs.put(attr, adata);
                names.put(adata.getName(), attr);
            }
        } else {
            adata = attrs.get(attr);
            assert adata != null;
        }
        return adata;
    }

    /**
     * Gets corresponding attribute identifier for the given attribute
     * identifier name.
     *
     * @param g    DEX Graph.
     * @param type DEX type identifier.
     * @param name DEX attribute identifier name.
     * @return The attribute identifier or INVALID_ATTRIBUTE if the attribute
     *         does not exist.
     */
    public static long getAttributeId(Graph g, int type, String name) {
        assert name != null;

        AttributeData adata = null;
        Long attr = names.get(name);
        if (attr == null) {
            attr = g.findAttribute(type, name);
            if (attr != Graph.INVALID_ATTRIBUTE) {
                adata = g.getAttributeData(attr);
                assert adata != null;
                attrs.put(attr, adata);
                names.put(adata.getName(), attr);
            }
        }
        return attr;
    }

    /**
     * Clears cached values.
     */
    public static void clear() {
        attrs.clear();
        names.clear();
    }
}
