package com.tinkerpop.blueprints.pgm.impls.dex;

import edu.upc.dama.dex.core.Graph;
import edu.upc.dama.dex.core.Graph.TypeData;

import java.util.HashMap;
import java.util.Map;

/**
 * Caches info about DEX types.
 *
 * @author <a href="http://www.sparsity-technologies.com">Sparsity
 *         Technologies</a>
 */
class DexTypes {

    /**
     * DEX type identifier --> {@link TypeData}
     */
    private static Map<Integer, TypeData> types = new HashMap<Integer, TypeData>();
    /**
     * Type name --> DEX type identifier
     */
    private static Map<String, Integer> names = new HashMap<String, Integer>();

    /**
     * Gets corresponding {@link TypeData} for the given type identifier.
     *
     * @param g    DEX Graph.
     * @param type DEX type identifier.
     * @return {@link TypeData} or null if the type does not exist.
     */
    public static TypeData getTypeData(Graph g, int type) {
        assert type != Graph.INVALID_TYPE;

        TypeData tdata = types.get(type);
        if (tdata == null) {
            tdata = g.getTypeData(type);
            if (tdata != null) {
                types.put(type, tdata);
            }
        }
        return tdata;
    }

    /**
     * Gets corresponding {@link TypeData} for the given type identifier name.
     *
     * @param g    DEX Graph.
     * @param name DEX type identifier name.
     * @return {@link TypeData} or null if the type does not exist.
     */
    public static TypeData getTypeData(Graph g, String name) {
        assert name != null;

        TypeData tdata = null;
        Integer type = names.get(name);
        if (type == null) {
            type = g.findType(name);
            if (type != Graph.INVALID_TYPE) {
                tdata = g.getTypeData(type);
                assert tdata != null;
                types.put(type, tdata);
                names.put(tdata.getName(), type);
            }
        }
        return tdata;
    }

    /**
     * Gets corresponding type identifier for the given type identifier name.
     *
     * @param g    DEX Graph.
     * @param name DEX type identifier name.
     * @return The type identifier or INVALID_TYPE if the type does not exist.
     */
    public static Integer getTypeId(Graph g, String name) {
        assert name != null;

        TypeData tdata = null;
        Integer type = names.get(name);
        if (type == null) {
            type = g.findType(name);
            if (type != Graph.INVALID_TYPE) {
                tdata = g.getTypeData(type);
                assert tdata != null;
                types.put(type, tdata);
                names.put(tdata.getName(), type);
            }
        }
        return type;
    }

    /**
     * Clears cached values.
     */
    public static void clear() {
        types.clear();
        names.clear();
    }
}
