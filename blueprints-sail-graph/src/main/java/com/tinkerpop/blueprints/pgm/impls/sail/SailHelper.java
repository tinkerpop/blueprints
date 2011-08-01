package com.tinkerpop.blueprints.pgm.impls.sail;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class SailHelper {
    public static final Pattern literalPattern = Pattern.compile("^\"(.*?)\"((\\^\\^<(.+?)>)$|(@(.{2}))$)");

    protected static void removeStatement(final Statement statement, final SailConnection sailConnection) {
        try {
            if (null != statement.getContext()) {
                sailConnection.removeStatements(statement.getSubject(), statement.getPredicate(), statement.getObject(), statement.getContext());
            } else {
                sailConnection.removeStatements(statement.getSubject(), statement.getPredicate(), statement.getObject());
            }
        } catch (SailException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected static void addStatement(final Statement statement, final SailConnection sailConnection) {
        try {
            if (null != statement.getContext()) {
                sailConnection.addStatement(statement.getSubject(), statement.getPredicate(), statement.getObject(), statement.getContext());
            } else {
                sailConnection.addStatement(statement.getSubject(), statement.getPredicate(), statement.getObject());
            }
        } catch (SailException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected static void addStatement(final Resource subject, final URI predicate, final Value object, final Resource context, final SailConnection sailConnection) {
        Statement statement;
        if (null != context) {
            statement = new ContextStatementImpl(subject, predicate, object, context);
        } else {
            statement = new StatementImpl(subject, predicate, object);
        }
        SailHelper.addStatement(statement, sailConnection);
    }

    public static boolean isBNode(final String resource) {
        return resource.length() > 2 && resource.startsWith(SailTokens.BLANK_NODE_PREFIX);
    }

    public static boolean isLiteral(final String resource) {
        return (literalPattern.matcher(resource).matches() || (resource.startsWith("\"") && resource.endsWith("\"") && resource.length() > 1));
    }

    public static boolean isURI(final String resource) {
        return !isBNode(resource) && !isLiteral(resource) && (resource.contains(":") || resource.contains("/") || resource.contains("#"));
    }

    public static Literal makeLiteral(final String resource, SailGraph graph) {
        final Matcher matcher = literalPattern.matcher(resource);
        if (matcher.matches()) {
            if (null != matcher.group(4))
                return new LiteralImpl(matcher.group(1), new URIImpl(graph.expandPrefix(matcher.group(4))));
            else
                return new LiteralImpl(matcher.group(1), matcher.group(6));
        } else {
            if (resource.startsWith("\"") && resource.endsWith("\"") && resource.length() > 1) {
                return new LiteralImpl(resource.substring(1, resource.length() - 1));
            } else {
                return null;
            }
        }
    }
}
