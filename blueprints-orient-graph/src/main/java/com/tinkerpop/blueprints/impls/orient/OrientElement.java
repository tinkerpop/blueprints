package com.tinkerpop.blueprints.impls.orient;

import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.db.record.ORecordElement.STATUS;
import com.orientechnologies.orient.core.exception.OSerializationException;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.ORecord;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.serialization.OSerializableStream;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.util.ElementHelper;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import com.tinkerpop.blueprints.util.StringFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public abstract class OrientElement implements Element, OSerializableStream, OIdentifiable {

    protected final OrientBaseGraph graph;
    protected final ODocument rawElement;

    protected OrientElement(final OrientBaseGraph rawGraph, final ODocument rawElement) {
        this.graph = rawGraph;
        this.rawElement = rawElement;
    }

    public void setProperty(final String key, final Object value) {
        if (key.equals(StringFactory.ID))
            throw ExceptionFactory.propertyKeyIdIsReserved();
        if (key.equals(StringFactory.LABEL) && this instanceof Edge)
            throw ExceptionFactory.propertyKeyLabelIsReservedForEdges();
        if (key.equals(StringFactory.EMPTY_STRING))
            throw ExceptionFactory.elementKeyCanNotBeEmpty();

        this.graph.autoStartTransaction();
        this.rawElement.field(key, value);
        this.graph.getRawGraph().save(rawElement);
    }

    public Object removeProperty(final String key) {
        this.graph.autoStartTransaction();
        final Object oldValue = this.rawElement.removeField(key);
        this.save();
        return oldValue;
    }

    public Object getProperty(final String key) {
        if (key == null)
            return null;

        if (key.equals("_class"))
            return rawElement.getSchemaClass().getName();
        else if (key.equals("_version"))
            return rawElement.getVersion();
        else if (key.equals("_rid"))
            return rawElement.getIdentity().toString();

        return this.rawElement.field(key);
    }

    public Set<String> getPropertyKeys() {
        Set<String> result = new HashSet<String>();

        final String[] fields = this.rawElement.fieldNames();
        for (String field : fields)
            if (!field.equals(StringFactory.LABEL))
                result.add(field);

        return result;
    }

    /**
     * Returns the Element Id assuring to save it if it's transient yet.
     */
    public Object getId() {
        return getIdentity();
    }

    protected void save() {
        this.rawElement.save();
    }

    public ODocument getRawElement() {
        return rawElement;
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.rawElement == null) ? 0 : this.rawElement.hashCode());
        return result;
    }

    public boolean equals(final Object object) {
        return ElementHelper.areEqual(this, object);
    }

    @Override
    public byte[] toStream() throws OSerializationException {
        return rawElement.getIdentity().toString().getBytes();
    }

    @Override
    public OSerializableStream fromStream(byte[] iStream) throws OSerializationException {
        ((ORecordId) rawElement.getIdentity()).fromString(new String(iStream));
        rawElement.setInternalStatus(STATUS.NOT_LOADED);
        return this;
    }

    @Override
    public ORID getIdentity() {
        ORID rid = this.rawElement.getIdentity();
        if (!rid.isValid())
            this.save();
        return rid;
    }

    @Override
    public ORecord<?> getRecord() {
        return this.rawElement;
    }

    public int compare(final OIdentifiable iFirst, final OIdentifiable iSecond) {
        if (iFirst == null || iSecond == null)
            return -1;
        return iFirst.compareTo(iSecond);
    }

    public int compareTo(final OIdentifiable iOther) {
        if (iOther == null)
            return 1;

        final ORID myRID = getIdentity();
        final ORID otherRID = iOther.getIdentity();

        if (myRID == null && otherRID == null)
            return 0;

        return myRID.compareTo(otherRID);
    }
}