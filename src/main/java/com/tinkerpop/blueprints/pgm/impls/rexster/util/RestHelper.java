package com.tinkerpop.blueprints.pgm.impls.rexster.util;

import com.tinkerpop.blueprints.pgm.impls.rexster.RexsterTokens;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class RestHelper {

    private static final JSONParser parser = new JSONParser();
    private static final String POST = "POST";
    private static final String DELETE = "DELETE";

    public static JSONObject get(final String uri) {
        try {
            URL url = new URL(uri);
            URLConnection connection = url.openConnection();
            connection.connect();
            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            JSONObject object = (JSONObject) parser.parse(reader);
            reader.close();
            return object;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /*public static JSONArray getResultArray(final String uri) {
        return (JSONArray) RestHelper.get(uri).get(RexsterTokens.RESULTS);
    }*/

    public static JSONObject getResultObject(final String uri) {
        return (JSONObject) RestHelper.get(uri).get(RexsterTokens.RESULTS);
    }

    public static JSONObject postResultObject(final String uri) {
        try {
            URL url = new URL(uri);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(POST);
            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            JSONObject retObject = (JSONObject) ((JSONObject) parser.parse(reader)).get(RexsterTokens.RESULTS);
            reader.close();
            return retObject;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static JSONObject postResultObjectForm(final String uri, final String formData) {
        return RestHelper.postResultObject(uri + RexsterTokens.QUESTION + formData);
    }

    public static void delete(final String uri) {
        try {
            URL url = new URL(uri);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(DELETE);
            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            reader.close();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
