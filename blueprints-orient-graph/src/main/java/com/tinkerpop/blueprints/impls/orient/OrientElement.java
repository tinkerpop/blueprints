package com.tinkerpop.blueprints.impls.orient;

import java.util.Map;

import com.orientechnologies.common.log.OLogManager;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.db.record.ORecordElement.STATUS;
import com.orientechnologies.orient.core.exception.OSchemaException;
import com.orientechnologies.orient.core.exception.OSerializationException;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.serialization.OSerializableStream;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.util.ElementHelper;

/**
 * Base Graph Element where OrientVertex and OrientEdge classes extends from. Labels are managed as OrientDB classes.
 * 
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
@SuppressWarnings("unchecked")
public abstract class OrientElement implements Element, OSerializableStream, OIdentifiable {
  private static final long       serialVersionUID          = 1L;

  public static final String      LABEL_FIELD_NAME          = "label";
  public static final Object      DEF_ORIGINAL_ID_FIELDNAME = "origId";

  // TODO: CAN REMOVE THIS REF IN FAVOR OF CONTEXT INSTANCE?
  protected final OrientBaseGraph graph;
  protected OIdentifiable         rawElement;

  protected OrientElement(final OrientBaseGraph rawGraph, final OIdentifiable iRawElement) {
    graph = rawGraph;
    rawElement = iRawElement;
  }

  public abstract String getBaseClassName();

  public abstract String getElementType();

  @Override
  public void remove() {
    graph.autoStartTransaction();
    getRecord().delete();
  }

  public <T extends OrientElement> T setProperties(final Object... fields) {
    if (fields != null && fields.length > 0 && fields[0] != null) {
      graph.autoStartTransaction();
      if (fields.length == 1) {
        Object f = fields[0];
        if (f instanceof Map<?, ?>) {
          for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) f).entrySet())
            setPropertyInternal(this, (ODocument) rawElement.getRecord(), entry.getKey().toString(), entry.getValue());

        } else
          throw new IllegalArgumentException(
              "Invalid fields: expecting a pairs of fields as String,Object or a single Map<String,Object>, but found: " + f);
      } else
        // SET THE FIELDS
        for (int i = 0; i < fields.length; i += 2)
          setPropertyInternal(this, (ODocument) rawElement.getRecord(), fields[i].toString(), fields[i + 1]);
    }
    return (T) this;
  }

  public void setProperty(final String key, final Object value) {
    ElementHelper.validateProperty(this, key, value);
    graph.autoStartTransaction();
    getRecord().field(key, value);
    save();
  }

  public <T> T removeProperty(final String key) {
    graph.autoStartTransaction();
    final Object oldValue = getRecord().removeField(key);
    save();
    return (T) oldValue;
  }

  public <T> T getProperty(final String key) {
    if (key == null)
      return null;

    if (key.equals("_class"))
      return (T) getRecord().getSchemaClass().getName();
    else if (key.equals("_version"))
      return (T) new Integer(getRecord().getVersion());
    else if (key.equals("_rid"))
      return (T) rawElement.getIdentity().toString();

    return getRecord().field(key);
  }

  /**
   * Returns the Element Id assuring to save it if it's transient yet.
   */
  public Object getId() {
    return getIdentity();
  }

  public void save() {
    save(null);
  }

  /**
   * Saves the edge's document.
   * 
   * @param iClusterName
   *          Cluster name or null to use the default "E"
   */
  public void save(final String iClusterName) {
    if (rawElement instanceof ODocument)
      if (iClusterName != null)
        ((ODocument) rawElement).save(iClusterName);
      else
        ((ODocument) rawElement).save();
  }

  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((rawElement == null) ? 0 : rawElement.hashCode());
    return result;
  }

  @Override
  public byte[] toStream() throws OSerializationException {
    return rawElement.getIdentity().toString().getBytes();
  }

  @Override
  public OSerializableStream fromStream(final byte[] iStream) throws OSerializationException {
    final ODocument record = getRecord();
    ((ORecordId) record.getIdentity()).fromString(new String(iStream));
    record.setInternalStatus(STATUS.NOT_LOADED);
    return this;
  }

  @Override
  public ORID getIdentity() {
    if (rawElement == null)
      return ORecordId.EMPTY_RECORD_ID;
    
    final ORID rid = rawElement.getIdentity();
    if (!rid.isValid()) {
      // SAVE THE RECORD TO OBTAIN A VALID RID
      graph.autoStartTransaction();
      save();
    }
    return rid;
  }

  @Override
  public ODocument getRecord() {
    if (rawElement instanceof ODocument)
      return (ODocument) rawElement;

    final ODocument doc = rawElement.getRecord();
    if (doc == null)
      return null;

    // CHANGE THE RID -> DOCUMENT
    rawElement = doc;
    return doc;
  }

  public boolean equals(final Object object) {
    return ElementHelper.areEqual(this, object);
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

  protected void checkClass() {
    // FORCE EARLY UNMARSHALLING
    final ODocument doc = getRecord();
    doc.deserializeFields();

    final OClass cls = doc.getSchemaClass();

    if (cls == null || !cls.isSubClassOf(getBaseClassName()))
      throw new IllegalArgumentException("The document received is not a " + getElementType() + ". Found class '" + cls + "'");
  }

  /**
   * Check if a class already exists, otherwise create it at the fly. If a transaction is running commit changes, create the class
   * and begin a new transaction.
   * 
   * @param iClassName
   *          Class's name
   */
  protected String checkForClassInSchema(final String iClassName) {
    if (iClassName == null)
      return null;

    final OSchema schema = graph.getRawGraph().getMetadata().getSchema();

    if (!schema.existsClass(iClassName)) {
      // CREATE A NEW CLASS AT THE FLY
      boolean txActive = graph.getRawGraph().getTransaction().isActive();
      try {

        if (txActive) {
          OLogManager
              .instance()
              .warn(
                  this,
                  "[OrientEdge] committing the active transaction to create the new Edge type '%s'. The transaction will be reopen right after that. To avoid this behavior create the classes outside the transaction",
                  iClassName);
          graph.commit();
        }

        schema.createClass(iClassName, schema.getClass(getBaseClassName()));

      } catch (OSchemaException e) {
        if (!schema.existsClass(iClassName))
          throw e;
      } finally {
        if (txActive)
          graph.autoStartTransaction();
      }
    } else {
      // CHECK THE CLASS INHERITANCE
      final OClass cls = schema.getClass(iClassName);
      if (!cls.isSubClassOf(getBaseClassName()))
        throw new IllegalArgumentException("Class '" + iClassName + "' is not an instance of " + getBaseClassName());
    }

    return iClassName;
  }

  protected static void setPropertyInternal(final Element element, final ODocument doc, final String key, final Object value) {
    ElementHelper.validateProperty(element, key, value);
    doc.field(key, value);
  }

  public OrientBaseGraph getGraph() {
    return graph;
  }
}