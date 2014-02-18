package com.tinkerpop.blueprints.oupls.sail.pg;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
class PropertyGraphValueFactory implements ValueFactory {
    private final ValueFactory other = new ValueFactoryImpl();

    // TODO: wrap actual vertices and edges in URIs
    public URI createURI(final String uri) {
        return other.createURI(uri);
    }

    public URI createURI(String ns, String name) {
        return createURI(ns + name);
    }

    public BNode createBNode() {
        throw new UnsupportedOperationException();
    }

    public BNode createBNode(String s) {
        throw new UnsupportedOperationException();
    }

    public Literal createLiteral(String s) {
        return other.createLiteral(s);
    }

    public Literal createLiteral(String s, String s1) {
        return other.createLiteral(s, s1);
    }

    public Literal createLiteral(String s, URI uri) {
        return other.createLiteral(s, uri);
    }

    public Literal createLiteral(boolean b) {
        return other.createLiteral(b);
    }

    public Literal createLiteral(byte b) {
        return other.createLiteral(b);
    }

    public Literal createLiteral(short i) {
        return other.createLiteral(i);
    }

    public Literal createLiteral(int i) {
        return other.createLiteral(i);
    }

    public Literal createLiteral(long l) {
        return other.createLiteral(l);
    }

    public Literal createLiteral(float v) {
        return other.createLiteral(v);
    }

    public Literal createLiteral(double v) {
        return other.createLiteral(v);
    }

    public Literal createLiteral(XMLGregorianCalendar c) {
        return other.createLiteral(c);
    }

    public Literal createLiteral(Date date) {
        return other.createLiteral(date);
    }

    public Statement createStatement(Resource resource, URI uri, Value value) {
        return other.createStatement(resource, uri, value);
    }

    public Statement createStatement(Resource resource, URI uri, Value value, Resource resource1) {
        throw new UnsupportedOperationException("graph contexts are not yet supported");
    }
}
