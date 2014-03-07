package com.tinkerpop.blueprints.oupls.sail;

import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 * A utility to import an RDF document or set of RDF documents to a Sail
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class SailLoader {
    private static final Logger LOGGER = Logger.getLogger(SailLoader.class.getName());

    private static final long BUFFER_SIZE = 1000;

    private static final long LOGGING_BUFFER_SIZE = 10000;

    private boolean verbose = false;

    private final Sail sail;

    private String baseUri = "http://example.org/baseURI/";

    public SailLoader(final Sail sail) {
        this.sail = sail;
    }

    public void setBaseUri(final String baseUri) {
        this.baseUri = baseUri;
    }

    public void setVerbose(final boolean verbose) {
        this.verbose = verbose;
    }

    public void load(final File fileOrDirectory) throws Exception {
        SailConnection c = sail.getConnection();
        try {
            c.begin();
            long startTime = System.currentTimeMillis();

            loadFile(fileOrDirectory, c);

            long endTime = System.currentTimeMillis();

            c.commit();
            LOGGER.info("resulting triple store has " + c.size() + " new statements");
            LOGGER.info("total load time: " + (endTime - startTime) + "ms");
        } finally {
            c.rollback();
            c.close();
        }
    }

    private void loadFile(final File fileOrDirectory,
                          final SailConnection c) throws IOException {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                loadFile(child, c);
            }
        } else {
            RDFFormat format;

            long before = System.currentTimeMillis();
            LOGGER.info("loading file: " + fileOrDirectory);
            String n = fileOrDirectory.getName();
            InputStream is;
            if (n.endsWith(".gz")) {
                String n0 = n.substring(0, n.lastIndexOf("."));
                format = RDFFormat.forFileName(n0);
                is = new GZIPInputStream(new FileInputStream(fileOrDirectory));
            } else {
                format = RDFFormat.forFileName(n);
                is = new FileInputStream(fileOrDirectory);
            }

            if (null == format) {
                LOGGER.warning("could not guess format of file: " + n);
                return;
            }

            RDFParser p = Rio.createParser(format);
            p.setStopAtFirstError(false);
            p.setRDFHandler(new SailConnectionAdder(c));

            try {
                p.parse(is, baseUri);
            } catch (Throwable t) {
                // Attempt to recover.
                t.printStackTrace(System.err);
            } finally {
                is.close();
            }

            long after = System.currentTimeMillis();
            LOGGER.info("\tfinished in " + (after - before) + "ms");
        }
    }

    private class SailConnectionAdder implements RDFHandler {
        private final SailConnection c;
        private long count = 0;

        private SailConnectionAdder(SailConnection c) {
            this.c = c;
        }

        public void startRDF() throws RDFHandlerException {
        }

        public void endRDF() throws RDFHandlerException {
            try {
                c.commit();
                c.begin();
            } catch (SailException e) {
                throw new RDFHandlerException(e);
            }
        }

        public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
            try {
                c.setNamespace(prefix, uri);
            } catch (SailException e) {
                throw new RDFHandlerException(e);
            }

            incrementCount();
        }

        public void handleStatement(Statement statement) throws RDFHandlerException {
            try {
                c.addStatement(statement.getSubject(), statement.getPredicate(), statement.getObject(), statement.getContext());
            } catch (SailException e) {
                throw new RDFHandlerException(e);
            }

            incrementCount();
        }

        public void handleComment(String s) throws RDFHandlerException {
            // do nothing
        }

        private void incrementCount() throws RDFHandlerException {
            count++;
            if (0 == count % BUFFER_SIZE) {
                try {
                    c.commit();
                    c.begin();

                    if (verbose && 0 == count % LOGGING_BUFFER_SIZE) {
                        LOGGER.info("" + System.currentTimeMillis() + "\t" + count);
                    }

                    //count = 0;
                } catch (SailException e) {
                    throw new RDFHandlerException(e);
                }
            }
        }
    }
}
