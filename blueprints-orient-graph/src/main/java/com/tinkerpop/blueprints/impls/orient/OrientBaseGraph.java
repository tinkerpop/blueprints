package com.tinkerpop.blueprints.impls.orient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.orientechnologies.common.io.OFileUtils;
import com.orientechnologies.common.log.OLogManager;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.index.OPropertyIndexDefinition;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.type.tree.provider.OMVRBTreeRIDProvider;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.MetaGraph;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.DefaultGraphQuery;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import com.tinkerpop.blueprints.util.PropertyFilteredIterable;
import com.tinkerpop.blueprints.util.StringFactory;

/**
 * A Blueprints implementation of the graph database OrientDB (http://www.orientechnologies.com)
 * 
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public abstract class OrientBaseGraph implements IndexableGraph, MetaGraph<ODatabaseDocumentTx>, KeyIndexableGraph {
  public static final String                           CONNECTION_OUT  = "out";
  public static final String                           CONNECTION_IN   = "in";
  public static final String                           CLASS_PREFIX    = "class:";

  protected final static String                        ADMIN           = "admin";
  protected boolean                                    useDynamicEdges = true;
  protected boolean                                    saveOriginalIds = false;

  private String                                       url;
  private String                                       username;
  private String                                       password;

  private static final ThreadLocal<OrientGraphContext> threadContext   = new ThreadLocal<OrientGraphContext>();
  private static final List<OrientGraphContext>        contexts        = new ArrayList<OrientGraphContext>();

  /**
   * Constructs a new object using an existent OGraphDatabase instance.
   * 
   * @param iDatabase
   *          Underlying OGraphDatabase object to attach
   */
  public OrientBaseGraph(final ODatabaseDocumentTx iDatabase) {
    reuse(iDatabase);
  }

  public OrientBaseGraph(final String url) {
    this(url, ADMIN, ADMIN);
  }

  public OrientBaseGraph(final String url, final String username, final String password) {
    this.url = OFileUtils.getPath(url);
    this.username = username;
    this.password = password;
    this.openOrCreate();
  }

  @SuppressWarnings("unchecked")
  public <T extends Element> Index<T> createIndex(final String indexName, final Class<T> indexClass,
      final Parameter... indexParameters) {
    final OrientGraphContext context = getContext(true);

    if (getRawGraph().getTransaction().isActive())
      // ASSURE PENDING TX IF ANY IS COMMITTED
      this.commit();

    synchronized (contexts) {
      if (context.manualIndices.containsKey(indexName))
        throw ExceptionFactory.indexAlreadyExists(indexName);

      final OrientIndex<? extends OrientElement> index = new OrientIndex<OrientElement>(this, indexName, indexClass, null);

      // ADD THE INDEX IN ALL CURRENT CONTEXTS
      for (OrientGraphContext ctx : contexts)
        ctx.manualIndices.put(index.getIndexName(), index);

      // SAVE THE CONFIGURATION INTO THE GLOBAL CONFIG
      saveIndexConfiguration();

      return (Index<T>) index;
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
    final OrientGraphContext context = getContext(true);
    Index<? extends Element> index = context.manualIndices.get(indexName);
    if (null == index) {
      return null;
    }

    if (indexClass.isAssignableFrom(index.getIndexClass()))
      return (Index<T>) index;
    else
      throw ExceptionFactory.indexDoesNotSupportClass(indexName, indexClass);
  }

  public Iterable<Index<? extends Element>> getIndices() {
    final OrientGraphContext context = getContext(true);
    final List<Index<? extends Element>> list = new ArrayList<Index<? extends Element>>();
    for (Index<?> index : context.manualIndices.values()) {
      list.add(index);
    }
    return list;
  }

  protected Iterable<OrientIndex<? extends OrientElement>> getManualIndices() {
    return getContext(true).manualIndices.values();
  }

  public void dropIndex(final String indexName) {
    if (getRawGraph().getTransaction().isActive())
      this.commit();

    try {
      synchronized (contexts) {
        for (OrientGraphContext ctx : contexts) {
          ctx.manualIndices.remove(indexName);
        }
      }

      getRawGraph().getMetadata().getIndexManager().dropIndex(indexName);
      saveIndexConfiguration();
    } catch (Exception e) {
      this.rollback();
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  public Vertex addVertex(final Object id) {
    String className = null;
    if (id != null && id instanceof String && id.toString().startsWith(CLASS_PREFIX))
      // GET THE CLASS NAME
      className = id.toString().substring(CLASS_PREFIX.length());

    this.autoStartTransaction();

    // SAVE THE ID TOO?
    final Object[] fields = saveOriginalIds && id != null ? new Object[] { OrientElement.ORIGINAL_ID, id } : null;

    final OrientVertex vertex = new OrientVertex(this, className, fields);

    // SAVE IT
    vertex.save();
    return vertex;
  }

  public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
    String className = null;
    if (id != null && id instanceof String && id.toString().startsWith(CLASS_PREFIX))
      // GET THE CLASS NAME
      className = id.toString().substring(CLASS_PREFIX.length());

    // SAVE THE ID TOO?
    final Object[] fields = saveOriginalIds && id != null ? new Object[] { OrientElement.ORIGINAL_ID, id } : null;

    return ((OrientVertex) outVertex).addEdge(label, inVertex, className, fields);
  }

  public Vertex getVertex(final Object id) {
    if (null == id)
      throw ExceptionFactory.vertexIdCanNotBeNull();

    ORID rid;
    if (id instanceof ORID)
      rid = (ORID) id;
    else {
      try {
        rid = new ORecordId(id.toString());
      } catch (IllegalArgumentException iae) {
        // orientdb throws IllegalArgumentException: Argument 'xxxx' is not a RecordId in form of string. Format must be:
        // <cluster-id>:<cluster-position>
        return null;
      }
    }

    if (!rid.isValid())
      return null;

    final ODocument doc = getRawGraph().load(rid);
    if (doc != null) {
      return new OrientVertex(this, doc);
    }

    return null;
  }

  public void removeVertex(final Vertex vertex) {
    vertex.remove();
  }

  public Iterable<Vertex> getVertices() {
    return getVertices(true);
  }

  public Iterable<Vertex> getVertices(final String key, Object value) {
    final OIndex<?> idx = getContext(true).rawGraph.getMetadata().getIndexManager().getIndex(OrientVertex.CLASS_NAME + "." + key);
    if (idx != null) {
      if (value != null && !(value instanceof String))
        value = value.toString();

      Object indexValue = idx.get(value);
      if (indexValue != null && !(indexValue instanceof Iterable<?>))
        indexValue = Arrays.asList(indexValue);

      return new OrientElementIterable<Vertex>(this, (Iterable<?>) indexValue);
    }
    return new PropertyFilteredIterable<Vertex>(key, value, this.getVertices());
  }

  private Iterable<Vertex> getVertices(final boolean polymorphic) {
    getContext(true);
    return new OrientElementScanIterable<Vertex>(this, Vertex.class, polymorphic);
  }

  public Iterable<Edge> getEdges() {
    return getEdges(true);
  }

  public Iterable<Edge> getEdges(final String key, Object value) {
    final OIndex<?> idx = getContext(true).rawGraph.getMetadata().getIndexManager().getIndex(OrientEdge.CLASS_NAME + "." + key);
    if (idx != null) {
      if (value != null && !(value instanceof String))
        value = value.toString();

      Object indexValue = (Iterable<?>) idx.get(value);
      if (indexValue != null && !(indexValue instanceof Iterable<?>))
        indexValue = Arrays.asList(indexValue);

      return new OrientElementIterable<Edge>(this, (Iterable<?>) indexValue);
    }
    return new PropertyFilteredIterable<Edge>(key, value, this.getEdges());
  }

  private Iterable<Edge> getEdges(final boolean polymorphic) {
    getContext(true);
    return new OrientElementScanIterable<Edge>(this, Edge.class, polymorphic);
  }

  public Edge getEdge(final Object id) {
    if (null == id)
      throw ExceptionFactory.edgeIdCanNotBeNull();

    final ORID rid;
    if (id instanceof ORID)
      rid = (ORID) id;
    else {
      final String str = id.toString();

      int pos = str.indexOf("->");

      if (pos > -1) {
        // DUMMY EDGE: CREATE IT IN MEMORY
        final String from = str.substring(0, pos);
        final String to = str.substring(pos + 2);
        return new OrientEdge(this, new ORecordId(from), new ORecordId(to));
      }

      try {
        rid = new ORecordId(str);
      } catch (IllegalArgumentException iae) {
        // orientdb throws IllegalArgumentException: Argument 'xxxx' is not a RecordId in form of string. Format must be:
        // [#]<cluster-id>:<cluster-position>
        return null;
      }
    }

    final ODocument doc = getRawGraph().load(rid);
    if (doc != null)
      return new OrientEdge(this, doc);

    return null;
  }

  public void removeEdge(final Edge edge) {
    edge.remove();
  }

  /**
   * Reuses the underlying database avoiding to create and open it every time.
   * 
   * @param iDatabase
   *          Underlying OGraphDatabase object
   */
  public OrientBaseGraph reuse(final ODatabaseDocumentTx iDatabase) {
    this.url = iDatabase.getURL();
    this.username = iDatabase.getUser() != null ? iDatabase.getUser().getName() : null;
    synchronized (this) {
      OrientGraphContext context = threadContext.get();
      if (context == null || !context.rawGraph.getName().equals(iDatabase.getName())) {
        removeContext();
        context = new OrientGraphContext();
        context.rawGraph = iDatabase;
        checkForGraphSchema(iDatabase);
        threadContext.set(context);
      }
    }
    return this;
  }

  protected void checkForGraphSchema(final ODatabaseDocumentTx iDatabase) {
    final OSchema schema = iDatabase.getMetadata().getSchema();

    schema.getOrCreateClass(OMVRBTreeRIDProvider.PERSISTENT_CLASS_NAME);

    final OClass vertexBaseClass = schema.getClass(OrientVertex.CLASS_NAME);
    final OClass edgeBaseClass = schema.getClass(OrientEdge.CLASS_NAME);

    if (vertexBaseClass == null)
      // CREATE THE META MODEL USING THE ORIENT SCHEMA
      schema.createClass(OrientVertex.CLASS_NAME).setOverSize(2);

    if (edgeBaseClass == null)
      schema.createClass(OrientEdge.CLASS_NAME);

    // @COMPATIBILITY < 1.4.0:
    boolean warn = false;
    final String MSG_SUFFIX = ". Probably you are using a database created with a previous version of OrientDB. Run command 'migrate database' to convert old database to new ones";

    if (vertexBaseClass != null) {
      if (!vertexBaseClass.getName().equals(OrientVertex.CLASS_NAME)) {
        OLogManager.instance().warn(this, "Found Vertex class %s", MSG_SUFFIX, vertexBaseClass.getName());
        warn = true;
      }

      if (vertexBaseClass.existsProperty(CONNECTION_OUT) || vertexBaseClass.existsProperty(CONNECTION_IN)) {
        OLogManager.instance().warn(this, "Found property in/out against V");
        warn = true;
      }
    }

    if (edgeBaseClass != null) {
      if (!warn && !edgeBaseClass.getName().equals(OrientEdge.CLASS_NAME)) {
        OLogManager.instance().warn(this, "Found Edge class %s" + MSG_SUFFIX, edgeBaseClass.getName());
        warn = true;
      }

      if (edgeBaseClass.existsProperty(CONNECTION_OUT) || edgeBaseClass.existsProperty(CONNECTION_IN)) {
        OLogManager.instance().warn(this, "Found property in/out against E");
        warn = true;
      }
    }
  }

  public void shutdown() {
    removeContext();

    url = null;
    username = null;
    password = null;
  }

  public String toString() {
    return StringFactory.graphString(this, getRawGraph().getURL());
  }

  public ODatabaseDocumentTx getRawGraph() {
    return getContext(true).rawGraph;
  }

  public void commit() {
  }

  public void rollback() {
  }

  public OClass getVertexBaseType() {
    return getRawGraph().getMetadata().getSchema().getClass(OrientVertex.CLASS_NAME);
  }

  public final OClass getVertexType(final String iTypeName) {
    final OClass cls = getRawGraph().getMetadata().getSchema().getClass(iTypeName);
    if (cls != null)
      checkVertexType(cls);
    return cls;
  }

  public OClass createVertexType(final String iClassName) {
    return getRawGraph().getMetadata().getSchema().createClass(iClassName, getVertexBaseType());
  }

  public OClass createVertexType(final String iClassName, final String iSuperClassName) {
    return getRawGraph().getMetadata().getSchema().createClass(iClassName, getVertexType(iSuperClassName));
  }

  public OClass createVertexType(final String iClassName, final OClass iSuperClass) {
    checkVertexType(iSuperClass);
    return getRawGraph().getMetadata().getSchema().createClass(iClassName, iSuperClass);
  }

  public OClass getEdgeBaseType() {
    return getRawGraph().getMetadata().getSchema().getClass(OrientEdge.CLASS_NAME);
  }

  public final OClass getEdgeType(final String iTypeName) {
    final OClass cls = getRawGraph().getMetadata().getSchema().getClass(iTypeName);
    if (cls != null)
      checkVertexType(cls);
    return cls;
  }

  public OClass createEdgeType(final String iClassName) {
    return getRawGraph().getMetadata().getSchema().createClass(iClassName, getEdgeBaseType());
  }

  public OClass createEdgeType(final String iClassName, final String iSuperClassName) {
    return getRawGraph().getMetadata().getSchema().createClass(iClassName, getEdgeType(iSuperClassName));
  }

  public OClass createEdgeType(final String iClassName, final OClass iSuperClass) {
    checkEdgeType(iSuperClass);
    return getRawGraph().getMetadata().getSchema().createClass(iClassName, iSuperClass);
  }

  protected final void checkVertexType(final OClass iType) {
    if (iType == null)
      throw new IllegalArgumentException("Vertex class is null");

    if (!iType.isSubClassOf(OrientVertex.CLASS_NAME))
      throw new IllegalArgumentException("Type error. The class " + iType + " does not extend class '" + OrientVertex.CLASS_NAME
          + "' and therefore cannot be considered a Vertex");
  }

  protected final void checkEdgeType(final OClass iType) {
    if (iType == null)
      throw new IllegalArgumentException("Edge class is null");

    if (!iType.isSubClassOf(OrientEdge.CLASS_NAME))
      throw new IllegalArgumentException("Type error. The class " + iType + " does not extend class '" + OrientEdge.CLASS_NAME
          + "' and therefore cannot be considered an Edge");
  }

  protected void autoStartTransaction() {
  }

  protected void saveIndexConfiguration() {
    getRawGraph().getMetadata().getIndexManager().getConfiguration().save();
  }

  protected OrientGraphContext getContext(final boolean create) {
    OrientGraphContext context = threadContext.get();
    if (context == null || !context.rawGraph.getURL().equals(url)) {
      if (create)
        context = openOrCreate();
    }
    return context;
  }

  private OrientGraphContext openOrCreate() {
    if (url == null)
      throw new IllegalStateException("Database is closed");

    synchronized (this) {
      OrientGraphContext context = threadContext.get();
      if (context != null)
        removeContext();

      context = new OrientGraphContext();
      threadContext.set(context);

      synchronized (contexts) {
        contexts.add(context);
      }

      context.rawGraph = new ODatabaseDocumentTx(url);

      if (url.startsWith("remote:") || context.rawGraph.exists()) {
        context.rawGraph.open(username, password);

        // LOAD THE INDEX CONFIGURATION FROM INTO THE DICTIONARY
        // final ODocument indexConfiguration = context.rawGraph.getMetadata().getIndexManager().getConfiguration();

        for (OIndex<?> idx : context.rawGraph.getMetadata().getIndexManager().getIndexes()) {
          if (idx.getConfiguration().field(OrientIndex.CONFIG_CLASSNAME) != null)
            // LOAD THE INDEXES
            loadIndex(idx);
        }

      } else
        context.rawGraph.create();

      checkForGraphSchema(context.rawGraph);

      return context;
    }
  }

  @SuppressWarnings("rawtypes")
  private OrientIndex<?> loadIndex(final OIndex rawIndex) {
    final OrientIndex<?> index;
    index = new OrientIndex(this, rawIndex);

    // REGISTER THE INDEX
    getContext(true).manualIndices.put(index.getIndexName(), index);
    return index;
  }

  private void removeContext() {
    final OrientGraphContext context = getContext(false);

    if (context != null) {
      for (Index<? extends Element> idx : context.manualIndices.values())
        ((OrientIndex<?>) idx).close();
      context.manualIndices.clear();

      context.rawGraph.commit();
      context.rawGraph.close();

      synchronized (contexts) {
        contexts.remove(context);
      }

      threadContext.set(null);
    }
  }

  public <T extends Element> void dropKeyIndex(final String key, Class<T> elementClass) {
    if (getRawGraph().getTransaction().isActive())
      this.commit();

    final String className = getClassName(elementClass);
    getRawGraph().getMetadata().getIndexManager().dropIndex(className + "." + key);
  }

  public <T extends Element> void createKeyIndex(final String key, Class<T> elementClass, final Parameter... indexParameters) {
    final String className = getClassName(elementClass);
    final ODatabaseDocumentTx db = getRawGraph();

    if (db.getTransaction().isActive())
      this.commit();

    final OClass cls = db.getMetadata().getSchema().getClass(className);

    final OType indexType;
    final OProperty property = cls.getProperty(key);
    if (property == null)
      indexType = OType.STRING;
    else
      indexType = property.getType();

    db.getMetadata()
        .getIndexManager()
        .createIndex(cls.getName() + "." + key, OClass.INDEX_TYPE.NOTUNIQUE.name(),
            new OPropertyIndexDefinition(cls.getName(), key, indexType), cls.getPolymorphicClusterIds(), null);
  }

  public <T extends Element> Set<String> getIndexedKeys(final Class<T> elementClass) {
    final String classPrefix = getClassName(elementClass) + ".";

    Set<String> result = new HashSet<String>();
    final Collection<? extends OIndex<?>> indexes = getRawGraph().getMetadata().getIndexManager().getIndexes();
    for (OIndex<?> index : indexes) {
      if (index.getName().startsWith(classPrefix))
        result.add(index.getDefinition().getFields().get(0));
    }
    return result;
  }

  protected <T> String getClassName(final Class<T> elementClass) {
    String className = null;

    if (elementClass.isAssignableFrom(Vertex.class))
      className = OrientVertex.CLASS_NAME;
    else if (elementClass.isAssignableFrom(Edge.class))
      className = OrientEdge.CLASS_NAME;
    return className;
  }

  public GraphQuery query() {
    return new DefaultGraphQuery(this);
  }

  public boolean isUseDynamicEdges() {
    return useDynamicEdges;
  }

  public void setUseDynamicEdges(boolean useDynamicEdges) {
    this.useDynamicEdges = useDynamicEdges;
  }

  public boolean isSaveOriginalIds() {
    return saveOriginalIds;
  }

  public void setSaveOriginalIds(boolean saveIds) {
    this.saveOriginalIds = saveIds;
  }
}