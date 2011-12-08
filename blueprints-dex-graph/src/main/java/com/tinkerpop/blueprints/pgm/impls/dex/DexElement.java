/**
 *
 */
package com.tinkerpop.blueprints.pgm.impls.dex;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.impls.dex.util.DexAttributes;
import com.tinkerpop.blueprints.pgm.impls.dex.util.DexTypes;
import edu.upc.dama.dex.core.Graph;
import edu.upc.dama.dex.core.Graph.AttributeData;
import edu.upc.dama.dex.core.Value;

import java.util.HashSet;
import java.util.Set;

/**
 * {@link Element} implementation for DEX.
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
     * DEX OID.
     */
    protected long oid = Graph.INVALID_OID;
    /**
     * DEX object type.
     * <p/>
     * It is loaded as late as possible (and just if required).
     *
     * @see #getObjectType()
     */
    private int type = Graph.INVALID_TYPE;

    /**
     * Gets the DEX object type.
     * <p/>
     * If it has not been loaded yet, it is retrieved from DEX database.
     *
     * @return The element type.
     */
    protected int getObjectType() {
        if (type == Graph.INVALID_TYPE) {
            type = graph.getRawGraph().getType(oid);
        }
        return type;
    }

    /**
     * Gets the type or label of the element.
     * <p/>
     * The label is the name of the DEX object type.
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
     * @param oid DEX OID.
     */
    protected DexElement(final DexGraph g, final long oid) {
        assert g != null;
        assert oid != Graph.INVALID_OID;

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
        AttributeData adata = DexAttributes.getAttributeData(graph.getRawGraph(), getObjectType(), key);
        if (adata == null) {
            return null;
        }

        Long attr = DexAttributes.getAttributeId(graph.getRawGraph(),
                getObjectType(), key);
        assert attr != null && attr.longValue() != Graph.INVALID_ATTRIBUTE;

        Value v = graph.getRawGraph().getAttribute(oid, attr);
        Object result = null;
        switch (v.getType()) {
            case Value.NULL:
                result = null;
                break;
            case Value.BOOL:
                result = v.getBool();
                break;
            case Value.INT:
                result = v.getInt();
                break;
            case Value.STRING:
                result = v.getString();
                break;
            case Value.DOUBLE:
                result = v.getDouble();
                break;
            default:
                throw new UnsupportedOperationException(DexTokens.TYPE_EXCEPTION_MESSAGE);
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
        Set<Long> attrs = graph.getRawGraph().getAttributes(oid);
        Set<String> attrKeys = new HashSet<String>();
        for (Long attr : attrs) {
            String key = graph.getRawGraph().getAttributeData(attr).getName();
            attrKeys.add(key);
        }
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
        if (key.equals(StringFactory.ID) || (key.equals(StringFactory.LABEL) && this instanceof Edge))
            throw new RuntimeException(key + StringFactory.PROPERTY_EXCEPTION_MESSAGE);

        if (key.compareTo(LABEL_PROPERTY) == 0) {
            throw new UnsupportedOperationException(LABEL_PROPERTY
                    + " property cannot be set.");
        }

        Long attr = DexAttributes.getAttributeId(graph.getRawGraph(), getObjectType(), key);
        short datatype = Value.NULL;
        if (attr == Graph.INVALID_ATTRIBUTE) {
            //
            // First time we set this attribute, let's create it.
            //
            if (value instanceof Boolean) {
                datatype = Value.BOOL;
            } else if (value instanceof Integer) {
                datatype = Value.INT;
            } else if (value instanceof String) {
                datatype = Value.STRING;
            } else if (value instanceof Double || value instanceof Float) {
                datatype = Value.DOUBLE;
            } else if (value instanceof Value) {
                datatype = ((Value) value).getType();
            } else {
                throw new UnsupportedOperationException(DexTokens.TYPE_EXCEPTION_MESSAGE);
            }
            assert datatype != Value.NULL;
            attr = graph.getRawGraph().newAttribute(type, key, datatype, Graph.ATTR_KIND_INDEXED);
            assert attr != Graph.INVALID_ATTRIBUTE;
        } else {
            datatype = DexAttributes.getAttributeData(graph.getRawGraph(), attr).getDatatype();
        }
        //
        // Set the Value
        //
        Value v = new Value();
        if (value instanceof Value) {
            v = (Value) value;
        } else {
            // from Object to Value
            switch (datatype) {
                case Value.BOOL:
                    v.setBool((Boolean) value);
                    break;
                case Value.INT:
                    v.setInt((Integer) value);
                    break;
                case Value.STRING:
                    v.setString((String) value);
                    break;
                case Value.DOUBLE:
                    if (value instanceof Double) {
                        v.setDouble((Double) value);
                    }
                    if (value instanceof Float) {
                        v.setDouble(((Float) value));
                    }
                    break;
                default:
                    throw new UnsupportedOperationException(DexTokens.TYPE_EXCEPTION_MESSAGE);
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
        Object ret = getProperty(key);
        Value v = new Value();
        v.setNull();
        setProperty(key, v);
        return ret;
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
