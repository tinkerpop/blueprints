package com.tinkerpop.blueprints.util.io.gml;

/**
 * A collection of tokens used for GML related data.
 *
 * Tokens defined from GML Tags
 * (http://www.fim.uni-passau.de/fileadmin/files/lehrstuhl/brandenburg/projekte/gml/gml-documentation.tar.gz)
 *
 * @author Stuart Hendren (http://stuarthendren.net)
 */
public class GMLTokens {
    public static final String GML = "gml";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String LABEL = "label";
    public static final String COMMENT = "comment";
    public static final String CREATOR = "Creator";
    public static final String VERSION = "Version";
    public static final String GRAPH = "graph";
    public static final String NODE = "node";
    public static final String EDGE = "edge";
    public static final String SOURCE = "source";
    public static final String TARGET = "target";
    public static final String DIRECTED = "directed"; // directed (0) undirected (1) default is undirected
    public static final String GRAPHICS = "graphics";
    public static final String LABEL_GRAPHICS = "LabelGraphics";
    public static final char COMMENT_CHAR = '#';

    /**
     * Special token used to store Blueprint ids as they may not be integers
     */
    public static final String BLUEPRINTS_ID = "blueprintsId";
}
