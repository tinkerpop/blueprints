package com.tinkerpop.blueprints.impls.tg;

import com.tinkerpop.blueprints.Graph;

/**
 * Tests IgnoreIdTinkerGraph using the standard test suite.
 *
 * @author Matthias Broecheler (http://www.matthiasb.com)
 */
public class IgnoreIdTinkerGraphTest extends TinkerGraphTest {

    @Override
    public Graph generateGraph() {
        return new IgnoreIdTinkerGraph(getDirectory());
    }

}
