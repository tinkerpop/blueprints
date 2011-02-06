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
     * Clear all the properties from an iterable of elements.
     *
     * @param elements the elements to remove properties from
     */
    public static void removeProperties(final Iterable<Element> elements) {
        for (final Element element : elements) {
            final List<String> keys = new ArrayList<String>();
            keys.addAll(element.getPropertyKeys());
            for (final String key : keys) {
                element.removeProperty(key);
            }
        }
    }

    /**
     * Remove a property from all elements in the provided iterable.
     *
     * @param key      the property to remove by key
     * @param elements the elements to remove the property from
     */
    public static void removeProperty(final String key, final Iterable<Element> elements) {
        for (final Element element : elements) {
            element.removeProperty(key);
        }
    }

    /**
     * Renames a property by removing the old key and adding the stored value to the new key.
     * If property does not exist, nothing occurs.
     *
     * @param oldKey   the key to rename
     * @param newKey   the key to rename to
     * @param elements the elements to rename
     */
    public static void renameProperty(final String oldKey, final String newKey, final Iterable<Element> elements) {
        for (final Element element : elements) {
            Object value = element.removeProperty(oldKey);
            if (null != value)
                element.setProperty(newKey, value);
        }
    }

    /**
     * Typecasts a property value. This only works for casting to a class that has a constructor of the for new X(String).
     * If no such constructor exists, a RuntimeException is thrown and the original element property is left unchanged.
     *
     * @param key       the key for the property value to typecast
     * @param classCast the class to typecast to
     * @param elements  the elements to have their property typecasted
     */
    public static void typecastProperty(final String key, final Class classCast, final Iterable<Element> elements) {
        for (final Element element : elements) {
            final Object value = element.removeProperty(key);
            if (null != value) {
                try {
                    element.setProperty(key, classCast.getConstructor(String.class).newInstance(value.toString()));
                } catch (Exception e) {
                    element.setProperty(key, value);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        }
    }
}
