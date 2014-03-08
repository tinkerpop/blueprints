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

    private static final int
            DEFAULT_BUFFER_SIZE = 1000,
            DEFAULT_LOGGING_BUFFER_SIZE = 10000;

    private final Sail sail;
    private boolean verbose = false;
    private String baseUri = "http://example.org/baseURI/";
    private int bufferSize = DEFAULT_BUFFER_SIZE;
    private int loggingBufferSize = DEFAULT_LOGGING_BUFFER_SIZE;

    public SailLoader(final Sail sail) {
        this.sail = sail;
    }

    /**
     * @param baseUri the base URI for any relative URIs in the loaded file(s)
     */
    public void setBaseUri(final String baseUri) {
        this.baseUri = baseUri;
    }

    /**
     * @param verbose whether to log information on individual files loaded and statements added
     */
    public void setVerbose(final boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * @param bufferSize the number of statements to be committed per transaction
     */
    public void setBufferSize(final int bufferSize) {
        this.bufferSize = bufferSize;
    }

    /**
     * @param loggingBufferSize the number of statements per logging message (if verbose=true)
     */
    public void setLoggingBufferSize(final int loggingBufferSize) {
        this.loggingBufferSize = loggingBufferSize;
    }

    /**
     * Loads an RDF file, or a directory containing RDF files, into a Sail
     *
     * @param fileOrDirectory a file in a supported RDF format or a directory which contains (at any level)
     *                        one or more files in a supported RDF format.
     *                        A directory should contain all or mostly RDF files.
     *                        Files must be named with a format-appropriate extension, e.g. *.rdf, *.ttl, *.nt
     *                        or a format-appropriate extension followed by .gz if they are compressed with Gzip.
     */
    public synchronized void load(final File fileOrDirectory) throws Exception {
        LOGGER.info("loading from " + fileOrDirectory);
        SailConnection c = sail.getConnection();
        try {
            c.begin();

            long startTime = System.currentTimeMillis();
            long count = loadFile(fileOrDirectory, c);
            long endTime = System.currentTimeMillis();

            // commit leftover statements
            c.commit();

            LOGGER.info("loaded " + count + " statements in " + (endTime - startTime) + "ms");
        } finally {
            c.rollback();
            c.close();
        }
    }

    private long loadFile(final File fileOrDirectory,
                          final SailConnection c) throws IOException {
        if (fileOrDirectory.isDirectory()) {
            long count = 0;

            for (File child : fileOrDirectory.listFiles()) {
                count += loadFile(child, c);
            }

            return count;
        } else {
            RDFFormat format;

            long before = System.currentTimeMillis();
            if (verbose) {
                LOGGER.info("loading file: " + fileOrDirectory);
            }
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
                return 0;
            }

            RDFParser p = Rio.createParser(format);
            p.setStopAtFirstError(false);
            SailConnectionAdder adder = new SailConnectionAdder(c);
            p.setRDFHandler(adder);

            try {
                p.parse(is, baseUri);
            } catch (Throwable t) {
                // Attempt to recover.
                t.printStackTrace(System.err);
            } finally {
                is.close();
            }

            long after = System.currentTimeMillis();
            if (verbose) {
                LOGGER.info("\tfinished in " + (after - before) + "ms");
            }

            return adder.count;
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
            if (0 == count % bufferSize) {
                try {
                    c.commit();
                    c.begin();

                    if (verbose && 0 == count % loggingBufferSize) {
                        LOGGER.info("" + System.currentTimeMillis() + "\t" + count);
                    }
                } catch (SailException e) {
                    throw new RDFHandlerException(e);
                }
            }
        }
    }
}
