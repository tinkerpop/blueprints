package com.tinkerpop.blueprints.pgm.impls.rexster.util;

import com.tinkerpop.blueprints.pgm.impls.rexster.RexsterTokens;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class RestHelper {

    public static JSONArray parseResultArray(String uri) {

        try {
            JSONParser parser = new JSONParser();
            URL url = new URL(uri);
            URLConnection conn = url.openConnection();
            conn.connect();
            InputStreamReader content = new InputStreamReader(conn.getInputStream());
            return (JSONArray) ((JSONObject) parser.parse(content)).get(RexsterTokens.RESULTS);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static JSONObject parseResultObject(String uri) {
        try {
            JSONParser parser = new JSONParser();
            URL url = new URL(uri);
            URLConnection conn = url.openConnection();
            conn.connect();
            InputStreamReader content = new InputStreamReader(conn.getInputStream());
            return (JSONObject) ((JSONObject) parser.parse(content)).get(RexsterTokens.RESULTS);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
