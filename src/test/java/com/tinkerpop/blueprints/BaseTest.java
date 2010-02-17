package com.tinkerpop.blueprints;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class BaseTest extends TestCase {

    long timer = -1l;

    public void testTrue() {
        assertTrue(true);
    }

    public static void printList(final List list) {
        for (Object o : list) {
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

    public static List asList(final Object x, final int times) {
        List list = new ArrayList();
        for (int i = 0; i < times; i++) {
            list.add(x);
        }
        return list;
    }

    public long stopWatch() {
        if (this.timer == -1l) {
            this.timer = System.currentTimeMillis();
            return -1l;
        } else {
            long temp =  System.currentTimeMillis() - this.timer;
            this.timer = -1l;
            return temp;
        }
    }

    public static void printPerformance(String name, Integer events, String eventName, long timeInMilliseconds) {
        if(null != events)
            System.out.println("\t" + name + ": " + events + " " + eventName + " in " + timeInMilliseconds + "ms");
        else
           System.out.println("\t" + name + ": " + eventName + " in " + timeInMilliseconds + "ms");
    }

}
