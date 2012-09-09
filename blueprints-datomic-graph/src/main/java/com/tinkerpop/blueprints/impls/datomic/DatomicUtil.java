package com.tinkerpop.blueprints.impls.datomic;

import clojure.lang.Keyword;
import com.tinkerpop.blueprints.TimeAwareElement;
import datomic.Database;
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
        types.put("java.math.BigDecimal",":db.type/bigdec");
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
            return property.toString().substring(1, property.toString().indexOf(".")).replace("$","_");
        }
        return null;
    }

    // Retrieve the Datomic to for the Java equivalent
    public static String mapJavaTypeToDatomicType(final Class clazz) {
        if (types.containsKey(clazz.getName())) {
            return types.get(clazz.getName());
        }
        throw new IllegalArgumentException("Object type " + clazz.getName() + " not supported");
    }

    // Create the attribute definition if it does not exist yet
    public static void createAttributeDefinition(final String key, final Class valueClazz, final Class elementClazz, DatomicGraph graph) {
        if (!existingAttributeDefinition(key, valueClazz, elementClazz, graph)) {
            try {
                if (graph.getTransactionTime() == null) {
                    graph.getConnection().transact(Util.list(Util.map(":db/id", Peer.tempid(":db.part/db"),
                                                                      ":db/ident", createKey(key, valueClazz, elementClazz),
                                                                      ":db/valueType", mapJavaTypeToDatomicType(valueClazz),
                                                                      ":db/cardinality", ":db.cardinality/one",
                                                                      ":db.install/_attribute", ":db.part/db"))).get();
                }
                else {
                    graph.getConnection().transact(Util.list(Util.map(":db/id", Peer.tempid(":db.part/db"),
                                                                      ":db/ident", createKey(key, valueClazz, elementClazz),
                                                                      ":db/valueType", mapJavaTypeToDatomicType(valueClazz),
                                                                      ":db/cardinality", ":db.cardinality/one",
                                                                      ":db.install/_attribute", ":db.part/db"), datomic.Util.map(":db/id", datomic.Peer.tempid(":db.part/tx"), ":db/txInstant", graph.getTransactionTime()))).get();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(DatomicGraph.DATOMIC_ERROR_EXCEPTION_MESSAGE);
            } catch (ExecutionException e) {
                throw new RuntimeException(DatomicGraph.DATOMIC_ERROR_EXCEPTION_MESSAGE);
            }
        }
    }

    // Sets/Unsets an index for a particular attribute
    public static void setAttributeIndex(final String key, final Class elementClazz, DatomicGraph graph, boolean index) {
        // For a specific key, multiple attributes could be specified in Datomic that have a different type. We need to create an index for all of them
        for (String type : types.keySet()) {
            try {
                if (!existingAttributeDefinition(key, Class.forName(type), elementClazz, graph)) {
                    // Attribute of this type does not exist, create it first
                    createAttributeDefinition(key, Class.forName(type), elementClazz, graph);
                }
                // Retrieve the attribute and index it
                Object attribute = getAttributeDefinition(key, Class.forName(type), elementClazz, graph);
                graph.getConnection().transact(Util.list(Util.map(":db/id", attribute,
                                                                  ":db/index", index))).get();
            } catch(ClassNotFoundException e) {
                throw new RuntimeException(DatomicGraph.DATOMIC_ERROR_EXCEPTION_MESSAGE);
            } catch (InterruptedException e) {
                throw new RuntimeException(DatomicGraph.DATOMIC_ERROR_EXCEPTION_MESSAGE);
            } catch (ExecutionException e) {
                throw new RuntimeException(DatomicGraph.DATOMIC_ERROR_EXCEPTION_MESSAGE);
            }
        }
    }

    // Creates an index for a particular attribute
    public static void createAttributeIndex(final String key, final Class elementClazz, DatomicGraph graph) {
        setAttributeIndex(key, elementClazz, graph, true);
    }

    // Creates an index for a particular attribute
    public static void removeAttributeIndex(final String key, final Class elementClazz, final DatomicGraph graph)  {
        setAttributeIndex(key, elementClazz, graph, false);
    }

    // Checks whether a new attribute defintion needs to be created on the fly
    public static boolean existingAttributeDefinition(final String key, final Class valueClazz, final Class elementClazz, final DatomicGraph graph) {
        int attributekeysize = Peer.q("[:find ?attribute " +
                                       ":in $ ?key " +
                                       ":where [?attribute :db/ident ?key] ]", graph.getRawGraph(), createKey(key, valueClazz, elementClazz)).size();
        // Existing attribute
        return attributekeysize != 0;
    }

    // Retrieve the attribute definition (if it exists). Otherwise, it returns null
    public static Object getAttributeDefinition(final String key, final Class valueClazz, final Class elementClazz, final DatomicGraph graph) {
        if (existingAttributeDefinition(key, valueClazz, elementClazz, graph)) {
            Collection<List<Object>> attributekeysize = Peer.q("[:find ?attribute " +
                                                                ":in $ ?key " +
                                                                ":where [?attribute :db/ident ?key] ]", graph.getRawGraph(), createKey(key, valueClazz, elementClazz));
            return attributekeysize.iterator().next().get(0);
        }
        return null;
    }

    public static Set<String> getIndexedAttributes(final Class elementClazz, final DatomicGraph graph) {
        Set<String> results = new HashSet<String>();
        Collection<List<Object>> indexedAttributes = Peer.q("[:find ?key " +
                                                             ":in $ " +
                                                             ":where [?attribute :db/ident ?key] " +
                                                                    "[?attribute :db/index true] ]", graph.getRawGraph());
        for(List<Object> indexedAttribute : indexedAttributes) {
            String elementClazzName = elementClazz.getSimpleName();
            if (indexedAttribute.get(0).toString().endsWith("." + elementClazzName.toLowerCase())) {
                results.add(getPropertyName((Keyword)indexedAttribute.get(0)));
            }
        }
        return results;
    }

    // Checks whether a new attribute defintion needs to be created on the fly
    public static boolean existingAttributeDefinition(final Keyword key, final DatomicGraph graph) {
        int attributekeysize = Peer.q("[:find ?attribute " +
                                       ":in $ ?key " +
                                       ":where [?attribute :db/ident ?key] ]", graph.getRawGraph(), key).size();
        // Existing attribute
        return attributekeysize != 0;
    }

    // Creates a unique key for each key-valuetype attribute (as only one attribute with the same name can be specified)
    public static Keyword createKey(final String key, final Class valueClazz, final Class elementClazz) {
        String elementType = "vertex";
        if (elementClazz.isAssignableFrom(DatomicEdge.class)) {
            elementType = "edge";
        }
        return Keyword.intern(key.replace("_","$") + "." + mapJavaTypeToDatomicType(valueClazz).split("/")[1] + "." + elementType);
    }

    // Returns the previous transaction for a particular time aware element
    public static Object getPreviousTransaction(DatomicGraph graph, TimeAwareElement element) {
        Iterator<List<Object>> previoustransaction  = (Peer.q("[:find ?previousTransactionId " +
                                                               ":in $ ?currentTransactionId ?id " +
                                                               ":where [?currentTransactionId :graph.element/previousTransaction ?previousTransaction] " +
                                                                      "[?previousTransaction :graph.element/previousTransaction/elementId ?id] " +
                                                                      "[?previousTransaction :graph.element/previousTransaction/transactionId ?previousTransactionId] ]", graph.getRawGraph(), element.getTimeId(), element.getId())).iterator();
        if (previoustransaction.hasNext()) {
            return previoustransaction.next().get(0);
        }
        return null;
    }

    // Returns the next transaction for a particular time aware element (null if the transaction id does not exist)
    public static Object getNextTransactionId(DatomicGraph graph, TimeAwareElement element) {
        // Retrieve the last encountered transaction before the input transaction
        Iterator<List<Object>> nexttransaction = (Peer.q("[:find ?nextTransactionId " +
                                                          ":in $ ?currenttransactionId ?id " +
                                                          ":where [?nextTransactionId :graph.element/previousTransaction ?currenttransaction] " +
                                                                 "[?currenttransaction :graph.element/previousTransaction/elementId ?id] " +
                                                                 "[?currenttransaction :graph.element/previousTransaction/transactionId ?currenttransactionId] ]", graph.getRawGraph(), element.getTimeId(), element.getId())).iterator();
        if (nexttransaction.hasNext()) {
            return nexttransaction.next().get(0);
        }
        return null;
    }

    public static Object getActualTimeId(Database database, TimeAwareElement element) {
        // Get the actual time id for a particular element and database value
        String timeRule = "[ [ (previous ?id ?tx) [?id _ _ ?tx] ] " +
                            "[ (previous ?id ?tx) [_ :graph.element/previousTransaction/elementId ?id ?tx] ] ] ]";
        Collection<List<Object>> alltxs = (datomic.Peer.q("[:find ?tx " +
                                                           ":in $ ?id % " +
                                                           ":where [previous ?id ?tx] ]", database.history(), element.getId(), timeRule));
        Iterator<List<Object>> tx = alltxs.iterator();
        Object lastTransaction = null;
        if (tx.hasNext()) {
            java.util.List<Object> transactionElement = tx.next();
            lastTransaction = transactionElement.get(0);
        }
        while (tx.hasNext()) {
            java.util.List<Object> transactionElement = tx.next();
            if ((Long)transactionElement.get(0) > (Long)lastTransaction) {
                lastTransaction = transactionElement.get(0);
            }
        }

        return lastTransaction;
    }

    // Helper method to retrieve the date associated with a particular transaction id
    public static Date getTransactionDate(DatomicGraph graph, Object transaction) {
        return (Date)datomic.Peer.q("[:find ?time " +
                                     ":in $ ?tx " +
                                     ":where [?tx :db/txInstant ?time] ]", graph.getRawGraph(), transaction).iterator().next().get(0);
    }

    public static Object getIdForAttribute(DatomicGraph graph, String attribute) {
        return Peer.q("[:find ?entity " +
                       ":in $ ?attribute " +
                       ":where [?entity :db/ident ?attribute] ] ", graph.getRawGraph(), Keyword.intern(attribute)).iterator().next().get(0);
    }

}
