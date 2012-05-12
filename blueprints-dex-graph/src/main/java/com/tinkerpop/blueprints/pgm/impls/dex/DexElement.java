/**
 *
 */
package com.tinkerpop.blueprints.pgm.impls.dex;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.util.ExceptionFactory;
import com.tinkerpop.blueprints.pgm.util.StringFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * {@link Element} implementation for Dex.
 * <p/>
 * All elements are typed or labeled. The way to get the type or label for an
 * element is retrieving the property {@link DexElement#LABEL_PROPERTY}. This
 * property cannot be set or removed.
 * <p/>
 * TODO: When a Float value is set for an attribute, it is stored as a Double
 * value. Thus, when it is retrieved later, it is retrieved as a Double and not
 * as a Float.
 *
 * @author <a href="http://www.sparsity-technologies.com">Sparsity
 *         Technologies</a>
 */
public class DexElement implements Element {
    /**
     * "Ghost" property to retrieve the element type or label.
     */
    public static final String LABEL_PROPERTY = "?DEX_LABEL_PROPERTY?";
    /**
     * DexGraph instance.
     */
    protected DexGraph graph = null;
    /**
     * Dex OID.
     */
    protected long oid = com.sparsity.dex.gdb.Objects.InvalidOID;
    /**
     * Dex object type.
     * <p/>
     * It is loaded as late as possible (and just if required).
     *
     * @see #getObjectType()
     */
    private int type = com.sparsity.dex.gdb.Type.InvalidType;

    /**
     * Gets the Dex object type.
     * <p/>
     * If it has not been loaded yet, it is retrieved from Dex database.
     *
     * @return The element type.
     */
    protected int getObjectType() {
        if (type == com.sparsity.dex.gdb.Type.InvalidType) {
            type = graph.getRawGraph().getObjectType(oid);
        }
        return type;
    }

    /**
     * Gets the type or label of the element.
     * <p/>
     * The label is the name of the Dex object type.
     *
     * @return Type or label of the element.
     */
    protected String getTypeLabel() {
        return DexTypes.getTypeData(graph.getRawGraph(), getObjectType())
                .getName();
    }

    /**
     * Creates a new instance.
     *
     * @param g   DexGraph.
     * @param oid Dex OID.
     */
    protected DexElement(final DexGraph g, final long oid) {
        assert g != null;
        assert oid != com.sparsity.dex.gdb.Objects.InvalidOID;

        this.graph = g;
        this.oid = oid;
    }

    /*
      * (non-Javadoc)
      *
      * @see com.tinkerpop.blueprints.pgm.Element#getProperty(java.lang.String)
      */
    @Override
    public Object getProperty(final String key) {
        com.sparsity.dex.gdb.Attribute adata = DexAttributes.getAttributeData(graph.getRawGraph(), getObjectType(), key);
        if (adata == null) {
            return null;
        }

        Integer attr = DexAttributes.getAttributeId(graph.getRawGraph(),
                getObjectType(), key);
        assert attr != null && attr.longValue() != com.sparsity.dex.gdb.Attribute.InvalidAttribute;

        com.sparsity.dex.gdb.Value v = new com.sparsity.dex.gdb.Value();
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
                case String:
                    result = v.getString();
                    break;
                case Double:
                    result = v.getDouble();
                    break;
                default:
                    throw new UnsupportedOperationException(DexTokens.TYPE_EXCEPTION_MESSAGE);
            }
        }
        return result;
    }

    /*
      * (non-Javadoc)
      *
      * @see com.tinkerpop.blueprints.pgm.Element#getPropertyKeys()
      */
    @Override
    public Set<String> getPropertyKeys() {
        com.sparsity.dex.gdb.AttributeList alist = graph.getRawGraph().getAttributes(oid);
        Set<String> attrKeys = new HashSet<String>();
        for (Integer attr : alist) {
            String key = graph.getRawGraph().getAttribute(attr).getName();
            attrKeys.add(key);
        }
        alist = null;
        return attrKeys;
    }

    /*
      * (non-Javadoc)
      *
      * @see com.tinkerpop.blueprints.pgm.Element#setProperty(java.lang.String,
      * java.lang.Object)
      */
    @Override
    public void setProperty(final String key, final Object value) {
        //System.out.println(this + "!!" + key + "!!" + value);
        if (key.equals(StringFactory.ID))
            throw ExceptionFactory.propertyKeyIdIsReserved();
        if (key.equals(StringFactory.LABEL) && this instanceof Edge)
            throw ExceptionFactory.propertyKeyLabelIsReservedForEdges();

        if (key.compareTo(LABEL_PROPERTY) == 0) {
            throw new UnsupportedOperationException(LABEL_PROPERTY
                    + " property cannot be set.");
        }

        Integer attr = DexAttributes.getAttributeId(graph.getRawGraph(), getObjectType(), key);
        com.sparsity.dex.gdb.DataType datatype = null;
        if (attr == com.sparsity.dex.gdb.Attribute.InvalidAttribute) {
            //
            // First time we set this attribute, let's create it.
            //
            if (value instanceof Boolean) {
                datatype = com.sparsity.dex.gdb.DataType.Boolean;
            } else if (value instanceof Integer) {
                datatype = com.sparsity.dex.gdb.DataType.Integer;
            } else if (value instanceof String) {
                datatype = com.sparsity.dex.gdb.DataType.String;
            } else if (value instanceof Double || value instanceof Float) {
                datatype = com.sparsity.dex.gdb.DataType.Double;
            } else if (value instanceof com.sparsity.dex.gdb.Value) {
                datatype = ((com.sparsity.dex.gdb.Value) value).getDataType();
            } else {
                throw new IllegalArgumentException(DexTokens.TYPE_EXCEPTION_MESSAGE);
            }
            assert datatype != null;
            attr = graph.getRawGraph().newAttribute(type, key, datatype, com.sparsity.dex.gdb.AttributeKind.Indexed);
            assert attr != com.sparsity.dex.gdb.Attribute.InvalidAttribute;
        } else {
            datatype = DexAttributes.getAttributeData(graph.getRawGraph(), attr).getDataType();
        }
        //
        // Set the Value
        //
        com.sparsity.dex.gdb.Value v = new com.sparsity.dex.gdb.Value();
        if (value instanceof com.sparsity.dex.gdb.Value) {
            v = (com.sparsity.dex.gdb.Value) value;
        } else {
            // from Object to Value
            switch (datatype) {
                case Boolean:
                    v.setBooleanVoid((Boolean) value);
                    break;
                case Integer:
                    v.setIntegerVoid((Integer) value);
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
                    throw new IllegalArgumentException(DexTokens.TYPE_EXCEPTION_MESSAGE);
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
      * com.tinkerpop.blueprints.pgm.Element#removeProperty(java.lang.String)
      */
    @Override
    public Object removeProperty(final String key) {
        try {
            Object ret = getProperty(key);
            com.sparsity.dex.gdb.Value v = new com.sparsity.dex.gdb.Value();
            v.setNull();
            setProperty(key, v);
            return ret;
        } catch (RuntimeException e) {
            return null;
        }
    }

    /*
      * (non-Javadoc)
      *
      * @see com.tinkerpop.blueprints.pgm.Element#getId()
      */
    @Override
    public Object getId() {
        return oid;
    }

    @Override
    public boolean equals(final Object object) {
        return (null != object) && (this.getClass().equals(object.getClass()) && this.getId().equals(((Element) object).getId()));
    }

    @Override
    public int hashCode() {
        return (int) oid;
    }
}
