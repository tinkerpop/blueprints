package com.tinkerpop.blueprints.util.wrappers.batch.loader;

import java.util.Arrays;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public class Debugger {

    public static volatile boolean DEBUG = true;


    protected static final void debug(String msg, Object o) {
        if (DEBUG) {
            if (o!=null) {
                if (o instanceof Object[]) msg = msg + Arrays.toString((Object[]) o);
                else msg = msg + o.toString();
            }
            System.out.println(msg);
        }
    }

    protected static final void debug(String msg) {
        debug(msg,null);
    }

}
