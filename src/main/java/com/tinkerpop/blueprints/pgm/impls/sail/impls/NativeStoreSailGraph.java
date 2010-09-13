package com.tinkerpop.blueprints.pgm.impls.sail.impls;

import com.tinkerpop.blueprints.pgm.impls.sail.SailGraph;
import org.openrdf.sail.nativerdf.NativeStore;

import java.io.File;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class NativeStoreSailGraph extends SailGraph {

    public NativeStoreSailGraph(String directory) {
        super(new NativeStore(new File(directory)));
    }

    public NativeStoreSailGraph(String directory, String tripleIndices) {
        super(new NativeStore(new File(directory), tripleIndices));
    }

}
