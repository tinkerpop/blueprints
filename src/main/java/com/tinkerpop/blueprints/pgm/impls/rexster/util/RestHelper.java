package com.tinkerpop.blueprints.pgm.impls.rexster.util;

import com.tinkerpop.blueprints.pgm.impls.rexster.RexsterTokens;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class RestHelper {

    public static JSONParser parser = new JSONParser();

    public static JSONArray getResultArray(String uri) {
        try {

            URL url = new URL(uri);
            URLConnection connection = url.openConnection();
            connection.connect();
            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            JSONArray retArray = (JSONArray) ((JSONObject) parser.parse(reader)).get(RexsterTokens.RESULTS);
            reader.close();
            return retArray;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static JSONObject getResultObject(String uri) {
        try {
            URL url = new URL(uri);
            URLConnection conn = url.openConnection();
            conn.connect();
            InputStreamReader reader = new InputStreamReader(conn.getInputStream());
            JSONObject retObject = (JSONObject) ((JSONObject) parser.parse(reader)).get(RexsterTokens.RESULTS);
            reader.close();
            return retObject;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static JSONObject postObject(String uri) {
        try {
            URL url = new URL(uri);
            URLConnection connection = url.openConnection();
            connection.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            // Get the response
            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            JSONObject retObject = (JSONObject) ((JSONObject) parser.parse(reader)).get(RexsterTokens.RESULTS);
            reader.close();
            writer.close();
            return retObject;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    public static JSONObject postObjectForm(String uri, String formData) {
        try {
            URL url = new URL(uri);
            URLConnection connection = url.openConnection();
            connection.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(formData);
            writer.flush();
            // Get the response
            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            JSONObject retObject = (JSONObject) ((JSONObject) parser.parse(reader)).get(RexsterTokens.RESULTS);
            reader.close();
            writer.close();
            return retObject;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }
}
