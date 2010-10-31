package com.tinkerpop.blueprints;

import junit.framework.TestCase;

import java.io.File;
import java.util.*;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class BaseTest extends TestCase {

    double timer = -1.0d;

    public static List<String> generateUUIDs(int number) {
        List<String> uuids = new ArrayList<String>();
        for (int i = 0; i < number; i++) {
            uuids.add(UUID.randomUUID().toString());
        }
        return uuids;
    }

    public static List<String> generateUUIDs(String prefix, int number) {
        List<String> uuids = new ArrayList<String>();
        for (int i = 0; i < number; i++) {
            uuids.add(prefix + UUID.randomUUID().toString());
        }
        return uuids;
    }

    public static void printCollection(final Collection collection) {
        for (Object o : collection) {
            System.out.println(o);
        }
    }

    public static void printIterator(final Iterator itty) {
        while (itty.hasNext()) {
            System.out.println(itty.next());
        }
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
        else System.out.println("\t" + name + ": " + eventName + " in " + timeInMilliseconds + "ms");
    }

    public static void warmUp(int amount) {
        List<String> uuids = new ArrayList<String>();
        for (int i = 0; i < amount; i++) {
            uuids.add(UUID.randomUUID().toString());
        }
        for (String uuid : uuids) {
            uuid.toUpperCase();
        }
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
    }


}
