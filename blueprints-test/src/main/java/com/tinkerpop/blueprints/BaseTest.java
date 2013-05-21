package com.tinkerpop.blueprints;

import junit.framework.TestCase;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class BaseTest extends TestCase {

    double timer = -1.0d;

    public static <T> T getOnlyElement(final Iterator<T> iterator) {
        if (!iterator.hasNext()) return null;
        T element = iterator.next();
        if (iterator.hasNext()) throw new IllegalArgumentException("Iterator has multiple elmenets");
        return element;
    }

    public static <T> T getOnlyElement(final Iterable<T> iterable) {
        return getOnlyElement(iterable.iterator());
    }

    public static int count(final Iterator iterator) {
        int counter = 0;
        while (iterator.hasNext()) {
            iterator.next();
            counter++;
        }
        return counter;
    }

    public static int count(final Iterable iterable) {
        return count(iterable.iterator());
    }

    public static int count(final CloseableIterable iterable) {
        return count(iterable.iterator());
    }

    public static <T> List<T> asList(Iterable<T> iterable) {
        List<T> list = new ArrayList<T>();
        for (T object : iterable) {
            list.add(object);
        }
        return list;

    }

    public double stopWatch() {
        if (this.timer == -1.0d) {
            this.timer = System.nanoTime() / 1000000.0d;
            return -1.0d;
        } else {
            double temp = (System.nanoTime() / 1000000.0d) - this.timer;
            this.timer = -1.0d;
            return temp;
        }
    }

    public static void printPerformance(String name, Integer events, String eventName, double timeInMilliseconds) {
        if (null != events)
            System.out.println("\t" + name + ": " + events + " " + eventName + " in " + timeInMilliseconds + "ms");
        else
            System.out.println("\t" + name + ": " + eventName + " in " + timeInMilliseconds + "ms");
    }

    public static void printTestPerformance(String testName, double timeInMilliseconds) {
        System.out.println("*** TOTAL TIME [" + testName + "]: " + timeInMilliseconds + " ***");
    }

    public static void equalIterators(final Iterator itty1, final Iterator itty2) {
        assertEquals(itty1.hasNext(), itty2.hasNext());
        while (itty1.hasNext()) {
            assertEquals(itty1.hasNext(), itty2.hasNext());
            assertEquals(itty1.next(), itty2.next());
        }
        assertEquals(itty1.hasNext(), itty2.hasNext());
    }

    protected static void deleteDirectory(final File directory) {
        if (directory.exists()) {
            for (File file : directory.listFiles()) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
            directory.delete();
        }

        // overkill code, simply allowing us to detect when data dir is in use.  useful though because without it
        // tests may fail if a database is re-used in between tests somehow.  this directory really needs to be
        // cleared between tests runs and this exception will make it clear if it is not.
        if (directory.exists()) {
            throw new RuntimeException("unable to delete directory " + directory.getAbsolutePath());
        }
    }

    public File computeTestDataRoot() {
        final String clsUri = this.getClass().getName().replace('.', '/') + ".class";
        final URL url = this.getClass().getClassLoader().getResource(clsUri);
        final String clsPath = url.getPath();
        final File root = new File(clsPath.substring(0, clsPath.length() - clsUri.length()));
        return new File(root.getParentFile(), "test-data");
    }


}
