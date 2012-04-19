package com.tinkerpop.blueprints.pgm.impls.datomic.util;

import clojure.lang.Keyword;
import com.tinkerpop.blueprints.pgm.impls.datomic.DatomicGraph;
import datomic.Datom;
import datomic.Peer;
import datomic.Util;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * @author Davy Suvee (http://datablend.be)
 */
public class DatomicUtil {

    private static final Map<String,String> types;
    private static final String RESERVED = ":graph";

    static {
        // Types supported by the underlying Datomic data model
        types = new HashMap<String,String>();
        types.put("java.lang.String",":db.type/string");
        types.put("java.lang.Boolean",":db.type/boolean");
        types.put("java.lang.Long",":db.type/long");
        types.put("java.lang.Integer",":db.type/long");
        types.put("java.math.BigInteger",":db.type/bigint");
        types.put("java.lang.Float",":db.type/float");
        types.put("java.lang.Double",":db.type/double");
        types.put("java.match.BigDecimal",":db.type/bigdeci");
        types.put("java.util.UUID",":db.type/uuid");
        types.put("java.net.URI",":db.type/uri");
    }

        // Check whether a key is part of the reserved space
    public static boolean isReservedKey(final String key) {
        // Key specific to the graph model or the general Datomic namespace
        return (key.startsWith(RESERVED) || key.startsWith(":db/"));
    }

    // Retrieve the original name of a property
    public static String getPropertyName(final Keyword property) {
        if (property.toString().contains(".")) {
            return property.toString().substring(1, property.toString().lastIndexOf(".")).replace("$","_");
        }
        return null;
    }

    // Retrieve the Datomic to for the Java equivalent
    public static String mapJavaTypeToDatomicType(final Object value) {
        if (types.containsKey(value.getClass().getName())) {
            return types.get(value.getClass().getName());
        }
        throw new IllegalArgumentException("Object type " + value.getClass().getName() + " not supported");
    }

    // Create the attribute definition if it does not exist yet
    public static void createAttributeDefinition(final String key, final Object value, DatomicGraph graph) {
        if (!existingAttributeDefinition(key, value, graph)) {
            try {
                graph.getConnection().transact(Util.list(Util.map(":db/id", Peer.tempid(":db.part/db"),
                                                                  ":db/ident", createKey(key, value),
                                                                  ":db/valueType", mapJavaTypeToDatomicType(value),
                                                                  ":db/cardinality", ":db.cardinality/one",
                                                                  ":db.install/_attribute", ":db.part/db"))).get();
            } catch (InterruptedException e) {
                throw new RuntimeException(DatomicGraph.DATOMIC_ERROR_EXCEPTION_MESSAGE);
            } catch (ExecutionException e) {
                throw new RuntimeException(DatomicGraph.DATOMIC_ERROR_EXCEPTION_MESSAGE);
            }
        }
    }

    // Checks whether a new attribute defintion needs to be created on the fly
    public static boolean existingAttributeDefinition(final String key, final Object value, DatomicGraph graph) {
        int attributekeysize = Peer.q("[:find ?attribute " +
                                       ":in $ ?key " +
                                       ":where [?attribute :db/ident ?key] ]", graph.getRawGraph(), createKey(key, value)).size();
        // Existing attribute
        return attributekeysize != 0;
    }

    // Checks whether a new attribute defintion needs to be created on the fly
    public static boolean existingAttributeDefinition(final Keyword key, DatomicGraph graph) {
        int attributekeysize = Peer.q("[:find ?attribute " +
                                       ":in $ ?key " +
                                       ":where [?attribute :db/ident ?key] ]", graph.getRawGraph(), key).size();
        // Existing attribute
        return attributekeysize != 0;
    }

    // Creates a unique key for each key-valuetype attribute (as only one attribute with the same name can be specified)
    public static Keyword createKey(final String key, final Object value) {
        return Keyword.intern(key.replace("_","$") + "." + mapJavaTypeToDatomicType(value).split("/")[1]);
    }

    // Creates an edge sequence
    public static DatomicEdgeSequence getEdgeSequence(Iterator<List<Object>> edgesit, DatomicGraph graph) {
        List<Object> edges = new ArrayList<Object>();
        while (edgesit.hasNext()) {
            edges.add(edgesit.next().get(0));
        }
        return new DatomicEdgeSequence(edges, graph);
    }

    // Creates an vertex sequence
    public static DatomicVertexSequence getVertexSequence(Iterator<List<Object>> verticessit, DatomicGraph graph) {
        List<Object> vertices = new ArrayList<Object>();
        while (verticessit.hasNext()) {
            vertices.add(verticessit.next().get(0));
        }
        return new DatomicVertexSequence(vertices, graph);
    }

}
