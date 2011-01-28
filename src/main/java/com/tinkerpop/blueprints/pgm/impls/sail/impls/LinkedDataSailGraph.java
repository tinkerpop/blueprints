package com.tinkerpop.blueprints.pgm.impls.sail.impls;

import com.tinkerpop.blueprints.pgm.impls.sail.SailGraph;
import net.fortytwo.linkeddata.sail.LinkedDataSail;
import net.fortytwo.ripple.Ripple;
import net.fortytwo.ripple.URIMap;
import org.openrdf.sail.Sail;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class LinkedDataSailGraph extends SailGraph {

    public LinkedDataSailGraph(SailGraph storageGraph) {
        try {
            Ripple.initialize();
            final Sail sail = new LinkedDataSail(storageGraph.getRawGraph(), new URIMap());
            sail.initialize();
            this.startSail(sail);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
