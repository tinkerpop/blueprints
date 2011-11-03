package com.tinkerpop.blueprints.pgm.impls.rexster.util;

import com.tinkerpop.blueprints.pgm.impls.rexster.RexsterTokens;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class RestHelper {

    private static final String POST = "POST";
    private static final String DELETE = "DELETE";

    public static JSONObject get(final String uri) {
        try {
            final URL url = new URL(safeUri(uri));
            final URLConnection connection = url.openConnection();
            connection.setRequestProperty(RexsterTokens.ACCEPT, RexsterTokens.APPLICATION_REXSTER_TYPED_JSON);
            connection.connect();
            final JSONTokener tokener = new JSONTokener(convertStreamToString(connection.getInputStream()));
            final JSONObject object = new JSONObject(tokener);
            return object;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static JSONArray getResultArray(final String uri) {
        return RestHelper.get(safeUri(uri)).optJSONArray(RexsterTokens.RESULTS);
    }

    public static JSONObject getResultObject(final String uri) {
        return RestHelper.get(safeUri(uri)).optJSONObject(RexsterTokens.RESULTS);
    }

    public static JSONObject postResultObject(final String uri) {
        try {
            // convert querystring into POST form data
            URL url = new URL(postUri(uri));
            String data = postData(uri);
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty(RexsterTokens.ACCEPT, RexsterTokens.APPLICATION_REXSTER_TYPED_JSON);
            connection.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(data); // post data with Content-Length automatically set
            writer.close();

            final JSONTokener tokener = new JSONTokener(convertStreamToString(connection.getInputStream()));
            final JSONObject resultObject = new JSONObject(tokener);
            final JSONObject retObject = resultObject.optJSONObject(RexsterTokens.RESULTS);

            return retObject;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static void post(final String uri) {
        try {
            // convert querystring into POST form data
            URL url = new URL(postUri(uri));
            String data = postData(uri);
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty(RexsterTokens.ACCEPT, RexsterTokens.APPLICATION_REXSTER_TYPED_JSON);
            connection.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(data); // post data with Content-Length automatically set
            writer.close();

            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            reader.close();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static String postUri(final String uri) {
        String url = "";
        final String safeUri = safeUri(uri);
        final int sep = safeUri.indexOf("?");
        if (sep == -1)
            url = safeUri;
        else
            url = safeUri.substring(0, sep);
        return url;
    }

    private static String postData(final String uri) {
        String data = null;
        final String safeUri = safeUri(uri);
        final int sep = safeUri.indexOf("?");
        if (sep == -1)
            data = "";
        else {
            data = safeUri.substring(sep + 1);
        }
        return data;
    }

    public static void delete(final String uri) {
        try {
            final URL url = new URL(uri);
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty(RexsterTokens.ACCEPT, RexsterTokens.APPLICATION_REXSTER_TYPED_JSON);
            connection.setRequestMethod(DELETE);
            final InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            reader.close();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static Object typeCast(final String type, final Object value) {
        if (type.equals(RexsterTokens.STRING))
            return value.toString();
        else if (type.equals(RexsterTokens.INTEGER))
            return Integer.valueOf(value.toString());
        else if (type.equals(RexsterTokens.LONG))
            return Long.valueOf(value.toString());
        else if (type.equals(RexsterTokens.DOUBLE))
            return Double.valueOf(value.toString());
        else if (type.equals(RexsterTokens.FLOAT))
            return Float.valueOf(value.toString());
        else
            return value;
    }

    public static String uriCast(final Object value) {
        if (value instanceof String)
            return value.toString();
        else if (value instanceof Integer)
            return "(" + RexsterTokens.INTEGER + "," + value + ")";
        else if (value instanceof Long)
            return "(" + RexsterTokens.LONG + "," + value + ")";
        else if (value instanceof Double)
            return "(" + RexsterTokens.DOUBLE + "," + value + ")";
        else if (value instanceof Float)
            return "(" + RexsterTokens.FLOAT + "," + value + ")";
        else
            return value.toString();

    }

    private static String safeUri(String uri) {
        // todo: make this way more safe
        return uri.replace(" ", "%20");
    }

    private static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
        }
        is.close();
        return sb.toString();
    }

    public static String encode(Object id) {
        if (id instanceof String)
            return URLEncoder.encode(id.toString());
        else
            return id.toString();
    }
}
