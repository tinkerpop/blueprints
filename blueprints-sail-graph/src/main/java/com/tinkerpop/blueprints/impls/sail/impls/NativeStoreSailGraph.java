package com.tinkerpop.blueprints.impls.sail.impls;

import com.tinkerpop.blueprints.impls.sail.SailGraph;
import org.openrdf.sail.Sail;
import org.openrdf.sail.nativerdf.NativeStore;

import java.io.File;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class NativeStoreSailGraph extends SailGraph {

    public NativeStoreSailGraph(final String directory) {
        super(createNativeStore(new File(directory), null));
    }

    public NativeStoreSailGraph(final String directory, final String tripleIndices) {
        super(createNativeStore(new File(directory), tripleIndices));
    }

    private static Sail createNativeStore(final File directory, final String tripleIndices) {
        Sail s = null == tripleIndices ? new NativeStore(directory) : new NativeStore(directory, tripleIndices);
        /*try {
            s.initialize();
        } catch (SailException e) {
            // FIXME: RuntimeExceptions are the root of all evil
            throw new RuntimeException(e);
        }*/
        return s;
    }
}
