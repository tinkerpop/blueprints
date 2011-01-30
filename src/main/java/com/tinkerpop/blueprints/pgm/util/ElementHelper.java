package com.tinkerpop.blueprints.pgm.util;

import com.tinkerpop.blueprints.pgm.Element;

import java.util.ArrayList;
import java.util.List;

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
    public static void copyElementProperties(final Element from, final Element to) {
        for (final String key : from.getPropertyKeys()) {
            to.setProperty(key, from.getProperty(key));
        }
    }

    /**
     * Clear all the properties from an element.
     *
     * @param element the element to remove properties from
     */
    public static void removeProperties(final Element element) {
        final List<String> keys = new ArrayList<String>();
        keys.addAll(element.getPropertyKeys());
        for (String key : keys) {
            element.removeProperty(key);
        }
    }
}
