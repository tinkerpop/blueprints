/**
 *
 */
package com.tinkerpop.blueprints.impls.sparksee;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.ElementHelper;
import com.tinkerpop.blueprints.util.StringFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * {@link Element} implementation for Sparksee.
 * <p/>
 * All elements are typed or labeled. The way to get the type or label for an
 * element is retrieving the property {@link StringFactory#LABEL}. This
 * property cannot be set or removed.
 * <p/>
 * TODO: When a Float value is set for an attribute, it is stored as a Double
 * value. Thus, when it is retrieved later, it is retrieved as a Double and not
 * as a Float.
 *
 * @author <a href="http://www.sparsity-technologies.com">Sparsity
 *         Technologies</a>
 */
class SparkseeElement implements Element {
    /**
     * SparkseeGraph instance.
     */
    protected SparkseeGraph graph = null;
    /**
     * Sparksee OID.
     */
    protected long oid = com.sparsity.sparksee.gdb.Objects.InvalidOID;
    /**
     * Sparksee object type.
     * <p/>
     * It is loaded as late as possible (and just if required).
     *
     * @see #getObjectType()
     */
    private int type = com.sparsity.sparksee.gdb.Type.InvalidType;

    /**
     * Gets the Sparksee object type.
     * <p/>
     * If it has not been loaded yet, it is retrieved from Sparksee database.
     *
     * @return The element type.
     */
    protected int getObjectType() {
        if (type == com.sparsity.sparksee.gdb.Type.InvalidType) {
            type = graph.getRawGraph().getObjectType(oid);
        }
        return type;
    }

    /**
     * Gets the type or label of the element.
     * <p/>
     * The label is the name of the Sparksee object type.
     *
     * @return Type or label of the element.
     */
    protected String getTypeLabel() {
        return graph.getRawGraph().getType(getObjectType()).getName();
    }

    /**
     * Creates a new instance.
     *
     * @param g   SparkseeGraph.
     * @param oid Sparksee OID.
     */
    protected SparkseeElement(final SparkseeGraph g, final long oid) {
        assert g != null;
        assert oid != com.sparsity.sparksee.gdb.Objects.InvalidOID;

        this.graph = g;
        this.oid = oid;
    }

    /*
      * (non-Javadoc)
      *
      * @see com.tinkerpop.blueprints.Element#getProperty(java.lang.String)
      */
    @Override
    public <T> T getProperty(final String key) {
        graph.autoStartTransaction();

        int type = getObjectType();
        if (key.compareTo(StringFactory.LABEL) == 0) {
            com.sparsity.sparksee.gdb.Type tdata = graph.getRawGraph().getType(type);
            return (T) tdata.getName();
        }
        int attr = graph.getRawGraph().findAttribute(getObjectType(), key);
        if (attr == com.sparsity.sparksee.gdb.Attribute.InvalidAttribute) {
            return null;
        }
        com.sparsity.sparksee.gdb.Attribute adata = graph.getRawGraph().getAttribute(attr);
        assert adata != null;

        com.sparsity.sparksee.gdb.Value v = new com.sparsity.sparksee.gdb.Value();
        graph.getRawGraph().getAttribute(oid, attr, v);
        Object result = null;
        if (!v.isNull()) {
            switch (v.getDataType()) {
                case Boolean:
                    result = v.getBoolean();
                    break;
                case Integer:
                    result = v.getInteger();
                    break;
                case Long:
                    result = v.getLong();
                    break;
                case String:
                    result = v.getString();
                    break;
                case Double:
                    result = v.getDouble();
                    break;
                default:
                    throw new UnsupportedOperationException(SparkseeTokens.TYPE_EXCEPTION_MESSAGE);
            }
        }
        return (T) result;
    }

    /*
      * (non-Javadoc)
      *
      * @see com.tinkerpop.blueprints.Element#getPropertyKeys()
      */
    @Override
    public Set<String> getPropertyKeys() {
        graph.autoStartTransaction();

        com.sparsity.sparksee.gdb.AttributeList alist = graph.getRawGraph().getAttributes(oid);
        Set<String> attrKeys = new HashSet<String>();
        for (Integer attr : alist) {
            String key = graph.getRawGraph().getAttribute(attr).getName();
            attrKeys.add(key);
        }
        alist.delete();
        alist = null;
        return attrKeys;
    }

    /*
      * (non-Javadoc)
      *
      * @see com.tinkerpop.blueprints.Element#setProperty(java.lang.String,
      * java.lang.Object)
      */
    @Override
    public void setProperty(final String key, final Object value) {
        ElementHelper.validateProperty(this, key, value);
        if (key.equals(StringFactory.LABEL))
            throw new IllegalArgumentException("Property key is reserved for all vertices and edges: " + StringFactory.LABEL);
        graph.autoStartTransaction();

        int attr = graph.getRawGraph().findAttribute(getObjectType(), key);
        com.sparsity.sparksee.gdb.DataType datatype = null;
        if (attr == com.sparsity.sparksee.gdb.Attribute.InvalidAttribute) {
            //
            // First time we set this attribute, let's create it.
            //
            if (value instanceof Boolean) {
                datatype = com.sparsity.sparksee.gdb.DataType.Boolean;
            } else if (value instanceof Integer) {
                datatype = com.sparsity.sparksee.gdb.DataType.Integer;
            } else if (value instanceof Long) {
                datatype = com.sparsity.sparksee.gdb.DataType.Long;
            } else if (value instanceof String) {
                datatype = com.sparsity.sparksee.gdb.DataType.String;
            } else if (value instanceof Double || value instanceof Float) {
                datatype = com.sparsity.sparksee.gdb.DataType.Double;
            } else if (value instanceof com.sparsity.sparksee.gdb.Value) {
                datatype = ((com.sparsity.sparksee.gdb.Value) value).getDataType();
            } else {
                throw new IllegalArgumentException(SparkseeTokens.TYPE_EXCEPTION_MESSAGE);
            }
            assert datatype != null;
            attr = graph.getRawGraph().newAttribute(type, key, datatype, com.sparsity.sparksee.gdb.AttributeKind.Basic);
            assert attr != com.sparsity.sparksee.gdb.Attribute.InvalidAttribute;
        } else {
            datatype = graph.getRawGraph().getAttribute(attr).getDataType();
        }
        //
        // Set the Value
        //
        com.sparsity.sparksee.gdb.Value v = new com.sparsity.sparksee.gdb.Value();
        if (value instanceof com.sparsity.sparksee.gdb.Value) {
            v = (com.sparsity.sparksee.gdb.Value) value;
        } else {
            // from Object to Value
            switch (datatype) {
                case Boolean:
                    v.setBooleanVoid((Boolean) value);
                    break;
                case Integer:
                    v.setIntegerVoid((Integer) value);
                    break;
                case Long:
                    if (value instanceof Long) {
                        v.setLongVoid((Long) value);
                    } else if (value instanceof Integer) {
                        v.setLongVoid(((Integer) value).longValue());
                    } else {
                        throw new IllegalArgumentException(SparkseeTokens.TYPE_EXCEPTION_MESSAGE);
                    }
                    break;
                case String:
                    v.setString((String) value);
                    break;
                case Double:
                    if (value instanceof Double) {
                        v.setDouble((Double) value);
                    }
                    if (value instanceof Float) {
                        v.setDouble(((Float) value));
                    }
                    break;
                default:
                    throw new IllegalArgumentException(SparkseeTokens.TYPE_EXCEPTION_MESSAGE);
            }
        }
        //try {
        this.graph.getRawGraph().setAttribute(oid, attr, v);
        //} catch(RuntimeException e) {
        //System.out.println("\t" + this + "!!" + attr + "!!" + v);
        //    throw e;
        //}

    }

    /*
      * (non-Javadoc)
      *
      * @see
      * com.tinkerpop.blueprints.Element#removeProperty(java.lang.String)
      */
    @Override
    public <T> T removeProperty(final String key) {
        graph.autoStartTransaction();

        try {
            Object ret = getProperty(key);
            com.sparsity.sparksee.gdb.Value v = new com.sparsity.sparksee.gdb.Value();
            v.setNull();
            setProperty(key, v);
            return (T) ret;
        } catch (RuntimeException e) {
            return null;
        }
    }

    /*
      * (non-Javadoc)
      *
      * @see com.tinkerpop.blueprints.Element#getId()
      */
    @Override
    public Object getId() {
        return oid;
    }

    public void remove() {
        if (this instanceof Vertex)
            this.graph.removeVertex((Vertex) this);
        else
            this.graph.removeEdge((Edge) this);
    }

    public boolean equals(final Object object) {
        graph.autoStartTransaction();

        return ElementHelper.areEqual(this, object);
    }

    @Override
    public int hashCode() {
        return (int) oid;
    }
}