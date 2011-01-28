package com.tinkerpop.blueprints.pgm.util;

import com.tinkerpop.blueprints.pgm.Element;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ElementHelper {

    /**
     * Copy the properties (key and value) from one element to another.
     * The properties are preserved on the from element.
     * Properties that share the same key on the to element are overwritten.
     *
     * @param from the element to copy properties from
     * @param to   the element to copy properties to
     */
    public static void copyElementProperties(Element from, Element to) {
        for (String key : from.getPropertyKeys()) {
            to.setProperty(key, from.getProperty(key));
        }
    }
}
