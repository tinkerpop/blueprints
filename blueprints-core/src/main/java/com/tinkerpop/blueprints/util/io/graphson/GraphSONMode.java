package com.tinkerpop.blueprints.util.io.graphson;

/**
 * Modes of operation of the GraphSONUtility.
 *
 * @author Stephen Mallette
 */
public enum GraphSONMode {
    /**
     * COMPACT constructs GraphSON without the _type field which denotes the type
     * of the element: edge or vertex.
     */
    COMPACT,

    /**
     * NORMAL includes the _type field and JSON data typing.
     */
    NORMAL,

    /**
     * EXTENDED includes the _type field and explicit data typing.
     */
    EXTENDED
}
