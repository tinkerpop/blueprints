package com.tinkerpop.blueprints.pgm.impls.sail.impls;

import com.tinkerpop.blueprints.pgm.impls.sail.SailGraph;
import org.openrdf.sail.memory.MemoryStore;

import java.io.File;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class MemoryStoreSailGraph extends SailGraph {

    public MemoryStoreSailGraph() {
        super(new MemoryStore());
    }

    public MemoryStoreSailGraph(final String dataDirectory) {
        super(new MemoryStore(new File(dataDirectory)));
    }
}


