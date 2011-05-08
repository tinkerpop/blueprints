package com.tinkerpop.blueprints.pgm.impls.sail.impls;

import com.tinkerpop.blueprints.pgm.impls.sail.SailGraph;
import net.fortytwo.linkeddata.sail.LinkedDataSail;
import net.fortytwo.ripple.Ripple;
import org.openrdf.sail.Sail;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class LinkedDataSailGraph extends SailGraph {

    public LinkedDataSailGraph(final SailGraph storageGraph) {
        try {
            Ripple.initialize();
            final Sail sail = new LinkedDataSail(storageGraph.getRawGraph());
            sail.initialize();
            this.startSail(sail);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
