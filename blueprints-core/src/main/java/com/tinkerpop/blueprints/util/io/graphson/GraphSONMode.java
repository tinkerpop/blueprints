package com.tinkerpop.blueprints.util.io.graphson;

/**
 * Modes of operation of the GraphSONUtility.
 *
 * @author Stephen Mallette
 */
public enum GraphSONMode {
    /**
     * COMPACT constructs GraphSON on the assumption that all property keys
     * are fair game for exclusion including _type, _inV, _outV, _label and _id.
     * It is possible to write GraphSON that cannot be read back into Graph,
     * if some or all of these keys are excluded.
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
