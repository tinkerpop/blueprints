package com.tinkerpop.blueprints.pgm.impls.orientdb;

import java.util.HashSet;
import java.util.Set;

import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.db.record.ORecordElement.STATUS;
import com.orientechnologies.orient.core.exception.OSerializationException;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.ORecord;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.serialization.OSerializableStream;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;

/**
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public abstract class OrientElement implements Element, OSerializableStream, OIdentifiable {

    protected static final String LABEL = "label";
    protected final OrientGraph graph;
    protected final ODocument rawElement;

    protected OrientElement(final OrientGraph rawGraph, final ODocument rawElement) {
        this.graph = rawGraph;
        this.rawElement = rawElement;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void setProperty(final String key, final Object value) {
        if (key.equals(StringFactory.ID) || (key.equals(StringFactory.LABEL) && this instanceof Edge))
            throw new RuntimeException(key + StringFactory.PROPERTY_EXCEPTION_MESSAGE);

        this.graph.autoStartTransaction();

        try {
            final Object oldValue = this.getProperty(key);
            for (final OrientAutomaticIndex autoIndex : this.graph.getAutoIndices()) {
                autoIndex.autoUpdate(key, value, oldValue, this);
            }

            this.rawElement.field(key, value);
            this.graph.getRawGraph().save(rawElement);
            this.graph.autoStopTransaction(TransactionalGraph.Conclusion.SUCCESS);
        } catch (RuntimeException e) {
            graph.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw e;
        } catch (Exception e) {
            graph.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Object removeProperty(final String key) {
        this.graph.autoStartTransaction();

        try {
            final Object oldValue = this.rawElement.removeField(key);
            if (null != oldValue) {
                for (final OrientAutomaticIndex autoIndex : this.graph.getAutoIndices()) {
                    autoIndex.autoRemove(key, oldValue, this);
                }
            }
            this.save();
            this.graph.autoStopTransaction(TransactionalGraph.Conclusion.SUCCESS);
            return oldValue;

        } catch (RuntimeException e) {
            this.graph.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw e;
        } catch (Exception e) {
            this.graph.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw new RuntimeException(e.getMessage(), e);
        }
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
            if (!field.equals(LABEL))
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

    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final OrientElement other = (OrientElement) obj;
        if (this.rawElement == null) {
            if (other.rawElement != null)
                return false;
        } else if (!this.rawElement.equals(other.rawElement))
            return false;
        return true;
    }

		@Override
		public byte[] toStream() throws OSerializationException {
				return rawElement.getIdentity().toString().getBytes();
		}

		@Override
		public OSerializableStream fromStream(byte[] iStream) throws OSerializationException {
				((ORecordId)rawElement.getIdentity()).fromString( new String(iStream) );
				rawElement.setInternalStatus(STATUS.NOT_LOADED);
				return this;
		}

		@Override
		public ORID getIdentity() {
      ORID rid = this.rawElement.getIdentity();
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