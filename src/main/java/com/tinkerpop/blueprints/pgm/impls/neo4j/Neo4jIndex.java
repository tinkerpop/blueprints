package com.tinkerpop.blueprints.pgm.impls.neo4j;


import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.impls.neo4j.util.Neo4jElementVertexSequence;
import org.neo4j.graphdb.Node;
import org.neo4j.index.IndexHits;
import org.neo4j.index.IndexService;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jIndex implements Index {

    private final IndexService indexService;
    private final Neo4jGraph graph;
    public final Set<String> indexKeys;

    public boolean indexAll = true;

    public Neo4jIndex(final IndexService indexService, final Neo4jGraph graph) {
        this.indexService = indexService;
        this.graph = graph;
        this.indexKeys = new HashSet<String>();
    }

    public IndexService getIndexService() {
        return this.indexService;
    }

    public void put(final String key, final Object value, final Element element) {
        if (this.indexAll || this.indexKeys.contains(key)) {
            if (element instanceof Neo4jVertex) {
                Node node = (Node) ((Neo4jVertex) element).getRawElement();
                this.indexService.index(node, key, value);
            }
        }
    }

    public Iterable<Element> get(final String key, final Object value) {
        IndexHits<Node> itty = this.indexService.getNodes(key, value);
        return new Neo4jElementVertexSequence(itty, this.graph);
    }

    public void remove(final String key, final Object value, final Element element) {
        if (element instanceof Neo4jVertex) {
            Node node = (Node) ((Neo4jVertex) element).getRawElement();
            this.indexService.removeIndex(node, key, value);
        }
    }

    public void indexAll(final boolean indexAll) {
        this.indexAll = indexAll;
    }

    public void addIndexKey(final String key) {
        this.indexKeys.add(key);
    }

    public void removeIndexKey(final String key) {
        this.indexKeys.remove(key);
        // TODO: drop index in LuceneIndexService
    }

    protected void shutdown() {
        indexService.shutdown();
    }
}
