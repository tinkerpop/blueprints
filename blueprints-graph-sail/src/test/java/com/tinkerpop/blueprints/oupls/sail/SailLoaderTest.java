package com.tinkerpop.blueprints.oupls.sail;

import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.memory.MemoryStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class SailLoaderTest {
    @Test
    public void testAll() throws Exception {
        KeyIndexableGraph g = new TinkerGraph();
        Sail sail = new GraphSail(g);
        sail.initialize();
        try {
            SailLoader loader = new SailLoader(sail);

            File f = resourceToFile("graph-example-sail-test.trig");

            SailConnection sc = sail.getConnection();
            try {
                sc.begin();
                assertEquals(0, sc.size());

                loader.load(f);

                sc.rollback();

                assertEquals(29, sc.size());
            } finally {
                sc.close();
            }
        } finally {
            sail.shutDown();
        }
    }

    // hastily stolen from http://stackoverflow.com/questions/941754/how-to-get-a-path-to-a-resource-in-a-java-jar-file
    private File resourceToFile(final String resource) {
        File file = null;
        URL res = getClass().getResource(resource);
        if (res.toString().startsWith("jar:")) {
            try {
                InputStream input = getClass().getResourceAsStream(resource);
                file = File.createTempFile("tempfile", ".tmp");
                OutputStream out = new FileOutputStream(file);
                int read;
                byte[] bytes = new byte[1024];

                while ((read = input.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
                file.deleteOnExit();
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        } else {
            file = new File(res.getFile());
        }

        if (file != null && !file.exists()) {
            throw new RuntimeException("Error: File " + file + " not found!");
        }

        return file;
    }

    /*
    @Test
    public void testFormatExtensions() throws Exception {
        Set<String> extensions = new HashSet<String>();
        for (RDFFormat f : RDFFormat.values()) {
            System.out.println("" + f);
            extensions.addAll(f.getFileExtensions());
        }

        for (String ext : extensions) {
            System.out.println(ext);
        }
    }//*/
}
