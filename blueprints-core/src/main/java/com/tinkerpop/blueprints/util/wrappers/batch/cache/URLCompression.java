package com.tinkerpop.blueprints.util.wrappers.batch.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public class URLCompression implements StringCompression {

    private static final String DELIMITER = "$";

    private int prefixCounter = 0;

    private final Map<String, String> urlPrefix = new HashMap<String, String>();


    @Override
    public String compress(String input) {
        String[] url = splitURL(input);
        String prefix = urlPrefix.get(url[0]);
        if (prefix == null) {
            //New Prefix
            prefix = Long.toString(prefixCounter, Character.MAX_RADIX) + DELIMITER;
            prefixCounter++;
            urlPrefix.put(url[0], prefix);
        }
        return prefix + url[1];
    }

    private final static char[] urlDelimiters = new char[]{'/', '#', ':'};

    private static final String[] splitURL(String url) {
        String[] res = new String[2];
        int pos = -1;
        for (char delimiter : urlDelimiters) {
            int currentpos = url.lastIndexOf(delimiter);
            if (currentpos > pos) pos = currentpos;
        }
        if (pos < 0) {
            res[0] = "";
            res[1] = url;
        } else {
            res[0] = url.substring(0, pos + 1);
            res[1] = url.substring(pos + 1);
        }
        return res;
    }
}
