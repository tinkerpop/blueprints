package com.tinkerpop.blueprints.util.io;

import com.tinkerpop.blueprints.Element;

import java.util.Comparator;

/**
 * Elements are sorted in lexicographical order of IDs.
 */
public class LexicographicalElementComparator implements Comparator<Element> {

    @Override
    public int compare(final Element a, final Element b) {
        return a.getId().toString().compareTo(b.getId().toString());
    }
}