package com.tinkerpop.blueprints.pgm.impls.dex.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Caches info about Dex types.
 *
 * @author <a href="http://www.sparsity-technologies.com">Sparsity
 *         Technologies</a>
 */
public class DexTypes {

    /**
     * Dex type identifier --> {@link com.sparsity.dex.gdb.Type}
     */
    private static Map<Integer, com.sparsity.dex.gdb.Type> types = new HashMap<Integer, com.sparsity.dex.gdb.Type>();
    /**
     * Type name --> Dex type identifier
     */
    private static Map<String, Integer> names = new HashMap<String, Integer>();

    /**
     * Gets corresponding {@link com.sparsity.dex.gdb.Type} for the given type identifier.
     *
     * @param g    Dex {@link com.sparsity.dex.gdb.Graph}.
     * @param type Dex type identifier.
     * @return {@link com.sparsity.dex.gdb.Type} or null if the type does not exist.
     */
    public static com.sparsity.dex.gdb.Type getTypeData(com.sparsity.dex.gdb.Graph g, int type) {
        assert type != com.sparsity.dex.gdb.Type.InvalidType;

        com.sparsity.dex.gdb.Type tdata = types.get(type);
        if (tdata == null) {
            tdata = g.getType(type);
            if (tdata != null) {
                types.put(type, tdata);
            }
        }
        return tdata;
    }

    /**
     * Gets corresponding {@link com.sparsity.dex.gdb.Type} for the given type identifier name.
     *
     * @param g    Dex {@link com.sparsity.dex.gdb.Graph}.
     * @param name Dex type identifier name.
     * @return {@link com.sparsity.dex.gdb.Type} or null if the type does not exist.
     */
    public static com.sparsity.dex.gdb.Type getTypeData(com.sparsity.dex.gdb.Graph g, String name) {
        assert name != null;

        com.sparsity.dex.gdb.Type tdata = null;
        Integer type = names.get(name);
        if (type == null) {
            type = g.findType(name);
            if (type != com.sparsity.dex.gdb.Type.InvalidType) {
                tdata = g.getType(type);
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
     * @param g    Dex {@link com.sparsity.dex.gdb.Graph}.
     * @param name Dex type identifier name.
     * @return The type identifier
     */
    public static Integer getTypeId(com.sparsity.dex.gdb.Graph g, String name) {
        assert name != null;

        com.sparsity.dex.gdb.Type tdata = null;
        Integer type = names.get(name);
        if (type == null) {
            type = g.findType(name);
            if (type != com.sparsity.dex.gdb.Type.InvalidType) {
                tdata = g.getType(type);
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
