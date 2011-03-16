/**
 *
 */
package com.tinkerpop.blueprints.pgm.impls.dex;

import com.tinkerpop.blueprints.pgm.*;
import edu.upc.dama.dex.core.Graph;
import edu.upc.dama.dex.core.Graph.TypeData;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * {@link IndexableGraph} implementation for DEX.
 *
 * @author <a href="http://www.sparsity-technologies.com">Sparsity
 *         Technologies</a>
 */
public class DexIndexableGraph extends DexGraph implements IndexableGraph {

    /**
     * {@inheritDoc}
     */
    public DexIndexableGraph(File db, boolean create)
            throws FileNotFoundException {
        super(db, create);
    }

    /*
      * (non-Javadoc)
      *
      * @see
      * com.tinkerpop.blueprints.pgm.IndexableGraph#createManualIndex(java.lang
      * .String, java.lang.Class)
      */
    @Override
    public <T extends Element> Index<T> createManualIndex(String indexName,
                                                          Class<T> indexClass) {
        throw new UnsupportedOperationException();
    }

    /*
      * (non-Javadoc)
      *
      * @see
      * com.tinkerpop.blueprints.pgm.IndexableGraph#createAutomaticIndex(java
      * .lang.String, java.lang.Class, java.util.Set)
      */
    @Override
    public <T extends Element> AutomaticIndex<T> createAutomaticIndex(
            String indexName, Class<T> indexClass, Set<String> indexKeys) {
        throw new UnsupportedOperationException();
    }

    /*
      * (non-Javadoc)
      *
      * @see
      * com.tinkerpop.blueprints.pgm.IndexableGraph#getIndex(java.lang.String,
      * java.lang.Class)
      */
    @Override
    public <T extends Element> Index<T> getIndex(String indexName,
                                                 Class<T> indexClass) {
        if (indexName.compareTo(Index.VERTICES) == 0
                || indexName.compareTo(Index.EDGES) == 0)
            return null;

        int type = DexTypes.getTypeId(getRawGraph(), indexName);
        if (type == Graph.INVALID_TYPE) {
            throw new IllegalArgumentException();
        }
        TypeData tdata = DexTypes.getTypeData(getRawGraph(), indexName);
        Index<T> index = null;
        if (tdata.isNodeType()) {
            index = (Index<T>) new DexAutomaticIndex<Vertex>(this, Vertex.class, type);
        } else {
            index = (Index<T>) new DexAutomaticIndex<Edge>(this, Edge.class, type);
        }
        return index;
    }

    /*
      * (non-Javadoc)
      *
      * @see com.tinkerpop.blueprints.pgm.IndexableGraph#getIndices()
      */
    @Override
    public Iterable<Index<? extends Element>> getIndices() {
        List<Index<? extends Element>> ret = new ArrayList<Index<? extends Element>>();
        for (Integer ntype : getRawGraph().nodeTypes()) {
            ret.add(new DexAutomaticIndex<Vertex>(this, Vertex.class, ntype));
        }
        for (Integer etype : getRawGraph().edgeTypes()) {
            ret.add(new DexAutomaticIndex<Edge>(this, Edge.class, etype));
        }
        return ret;
    }

    /*
      * (non-Javadoc)
      *
      * @see
      * com.tinkerpop.blueprints.pgm.IndexableGraph#dropIndex(java.lang.String)
      */
    @Override
    public void dropIndex(String indexName) {
        throw new UnsupportedOperationException();
    }

}
