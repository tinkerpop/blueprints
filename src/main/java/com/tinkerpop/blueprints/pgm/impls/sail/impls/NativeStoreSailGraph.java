package com.tinkerpop.blueprints.pgm.impls.sail.impls;

import com.tinkerpop.blueprints.pgm.impls.sail.SailGraph;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.nativerdf.NativeStore;

import java.io.File;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class NativeStoreSailGraph extends SailGraph {

    public NativeStoreSailGraph(String directory) {
        super(createNativeStore(new File(directory), null));
    }

    public NativeStoreSailGraph(String directory, String tripleIndices) {
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
