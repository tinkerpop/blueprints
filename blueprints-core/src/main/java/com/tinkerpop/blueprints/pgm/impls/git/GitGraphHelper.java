package com.tinkerpop.blueprints.pgm.impls.git;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * An object to read and write <code>GitGraph</code>s from the file system.
 * <p/>
 * User: josh
 * Date: 4/13/11
 * Time: 2:00 PM
 */
class GitGraphHelper {
    private static final String
            VERTICES = "vertices",
            EDGES = "edges",
            VERTEX_PROPERTIES = "vprops",
            EDGE_PROPERTIES = "eprops";

    private final ObjectSerializer serializer;
    private final ObjectDeserializer deserializer;

    public GitGraphHelper() throws IOException {
        serializer = new ObjectSerializer();
        deserializer = new ObjectDeserializer();
    }

    public void save(final Graph graph,
                     final File directory) throws IOException {
        saveNonRecursive(graph, directory);
    }

    /**
     * Caution: the graph will be cleared of any pre-existing data
     *
     * @param directory
     * @param graph
     * @throws IOException
     */
    public void load(final File directory,
                     final Graph graph) throws IOException {
        graph.clear();

        if (directory.exists()) {
            loadRecursive(graph, directory, "");
        }
    }

    private void saveNonRecursive(final Graph graph,
                                  final File directory) throws IOException {
        // Since this is non-recursive, we always use the local graph root.
        String parentPath = "";

        // Clear the directory
        if (directory.exists()) {
            deleteDirectory(directory);
        }
        directory.mkdirs();

        String curPath;
        OutputStream elOut;
        OutputStream propsOut;

        // Write vertices and vertex properties.
        List<String> allVertexIds = new LinkedList<String>();
        for (Vertex v : graph.getVertices()) {
            Object id = v.getId();
            if (!(id instanceof String)) {
                throw new IOException("vertex ID is not a string: " + id);
            }
            allVertexIds.add((String) id);
        }
        Collections.sort(allVertexIds);
        curPath = null;
        elOut = null;
        propsOut = null;
        for (String id : allVertexIds) {
            RelativeId p = new RelativeId(id, parentPath);
            if (null == curPath || !curPath.equals(p.getPath())) {
                if (null != elOut) {
                    elOut.close();
                    propsOut.close();
                }

                File dir;
                if (p.getPath().length() > 0) {
                    dir = new File(directory, p.getPath());
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                } else {
                    dir = directory;
                }

                elOut = new FileOutputStream(new File(dir, VERTICES));
                propsOut = new FileOutputStream(new File(dir, VERTEX_PROPERTIES));

                curPath = p.getPath();
            }

            writeVertex(p, elOut);
            writeProperties(graph.getVertex(id), p, propsOut);
        }
        if (null != elOut) {
            elOut.close();
        }
        if (null != propsOut) {
            propsOut.close();
        }

        // Write edges and edge properties.
        List<String> allEdgeIds = new LinkedList<String>();
        for (Edge e : graph.getEdges()) {
            Object id = e.getId();
            if (!(id instanceof String)) {
                throw new IOException("edge ID is not a string: " + id);
            }
            allEdgeIds.add((String) id);
        }
        Collections.sort(allEdgeIds);
        curPath = null;
        elOut = null;
        propsOut = null;
        for (String id : allEdgeIds) {
            //System.out.println("writing edge " + id);
            RelativeId p = new RelativeId(id, parentPath);
            if (null == curPath || !curPath.equals(p.getPath())) {
                //System.out.println("\tp.getPath() = " + p.getPath());
                if (null != elOut) {
                    elOut.close();
                    propsOut.close();
                }

                File dir;
                if (p.getPath().length() > 0) {
                    dir = new File(directory, p.getPath());
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                } else {
                    dir = directory;
                }

                // Note: these are opened in append mode, as the ordering of IDs does not guarantee that a file will not
                //       have to be opened more than once.  E.g. for {"a", "dir/x", "z"}
                elOut = new FileOutputStream(new File(dir, EDGES), true);
                propsOut = new FileOutputStream(new File(dir, EDGE_PROPERTIES), true);

                curPath = p.getPath();
            }

            Edge e = graph.getEdge(id);
            writeEdge(e, p, elOut);
            writeProperties(e, p, propsOut);
        }
        if (null != elOut) {
            elOut.close();
        }
        if (null != propsOut) {
            propsOut.close();
        }

        // TODO: this is a hack
        sortProperties(directory);
    }

    private void sortProperties(final File directory) throws IOException {
        for (File f : directory.listFiles()) {
            if (f.isDirectory()) {
                sortProperties(f);
            } else if (f.getName().equals(VERTEX_PROPERTIES)
                    || f.getName().equals(EDGE_PROPERTIES)) {
                List<String> rows = new LinkedList<String>();
                BufferedReader reader = new BufferedReader(new FileReader(f));
                try {
                    String s;
                    while ((s = reader.readLine()) != null) {
                        rows.add(s);
                    }
                } finally {
                    reader.close();
                }

                Collections.sort(rows);

                FileWriter writer = new FileWriter(f);
                try {
                    for (String cur : rows) {
                        writer.write(cur + "\n");
                    }
                } finally {
                    writer.close();
                }
            }
        }
    }

    public static boolean deleteDirectory(final File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteDirectory(f);
                } else {
                    f.delete();
                }
            }
        }
        return dir.delete();
    }

    private void writeVertex(final RelativeId r,
                             final OutputStream out) throws IOException {
        out.write(escape(r.getName()).getBytes());
        out.write('\n');
    }

    private void writeEdge(final Edge e,
                           final RelativeId r,
                           final OutputStream out) throws IOException {
        //System.out.println("writing edge " + r.getPath() + ":" + r.getName());
        // ID first...
        out.write(escape(r.getName()).getBytes());
        out.write('\t');

        // ...then tail vertex, then head vertex...
        Object outId = e.getOutVertex().getId();
        Object inId = e.getInVertex().getId();
        if (!(outId instanceof String)) {
            throw new IOException("vertex ID is not a string: " + outId);
        }
        if (!(inId instanceof String)) {
            throw new IOException("vertex ID is not a string: " + inId);
        }
        RelativeId outR = new RelativeId((String) outId, r.getPath());
        RelativeId inR = new RelativeId((String) inId, r.getPath());
        //System.out.println("\t" + outR.getPath() + ":" + outR.getName() + ", " + inR.getPath() + ":" + inR.getName());
        out.write(escape(outR.getPath() + outR.getName()).getBytes());
        out.write('\t');
        out.write(escape(inR.getPath() + inR.getName()).getBytes());
        out.write('\t');

        // ...finally, label
        out.write(escape(e.getLabel()).getBytes());
        out.write('\n');
    }

    private void writeProperties(final Element e,
                                 final RelativeId r,
                                 final OutputStream out) throws IOException {
        for (String key : e.getPropertyKeys()) {
            Object value = e.getProperty(key);
            if (!(value instanceof Serializable)) {
                throw new IOException("value for property '" + key + "' is not serializable");
            }

            out.write(escape(r.getName()).getBytes());
            out.write('\t');
            out.write(escape(key).getBytes());
            out.write('\t');
            out.write(serializer.serialize((Serializable) value));
            out.write('\n');
        }
    }

    private void loadRecursive(final Graph graph,
                               final File directory,
                               final String parentPath) throws IOException {
        //System.out.println("loading from: " + directory + " using parentPath " + parentPath);
        if (!directory.isDirectory()) {
            throw new IOException("file is not a directory: " + directory);
        }

        // Examine all files in this directory.  There are four special files, which may or may not be present.
        // Any other file is assumed to be the directory of a child graph.
        Map<String, File> files = new HashMap<String, File>();
        for (File child : directory.listFiles()) {
            files.put(child.getName(), child);
        }

        File vertices = files.get(VERTICES);
        //if (null == vertices) {
        //    throw new IOException("missing '" + VERTICES + "' file in " + directory);
        //}
        files.remove(VERTICES);

        File edges = files.get(EDGES);
        //if (null == edges) {
        //    throw new IOException("missing '" + EDGES + "' file in " + directory);
        //}
        files.remove(EDGES);

        File vprops = files.get(VERTEX_PROPERTIES);
        //if (null == vprops) {
        //    throw new IOException("missing '" + VERTEX_PROPERTIES + "' file in " + directory);
        //}
        files.remove(VERTEX_PROPERTIES);

        File eprops = files.get(EDGE_PROPERTIES);
        //if (null == eprops) {
        //    throw new IOException("missing '" + EDGE_PROPERTIES + "' file in " + directory);
        //}
        files.remove(EDGE_PROPERTIES);

        // Load children first, so that all vertices are defined prior to edges being read.
        for (File child : files.values()) {
            if (child.getName().contains("/")) {
                throw new IOException("file name contains the reserved '/' character");
            }

            loadRecursive(graph, child, parentPath + child.getName() + "/");
        }

        if (null != vertices) {
            readVertices(vertices, graph, parentPath);
        }

        if (null != edges) {
            readEdges(edges, graph, parentPath);
        }

        if (null != vprops) {
            readProperties(vprops, graph, parentPath, false);
        }

        if (null != eprops) {
            readProperties(eprops, graph, parentPath, true);
        }
    }

    private void readVertices(final File file,
                              final Graph graph,
                              final String parentPath) throws IOException {
        InputStream is = new FileInputStream(file);
        try {
            InputStreamReader r = new InputStreamReader(is, "UTF-8");
            BufferedReader br = new BufferedReader(r);
            String l;
            while ((l = br.readLine()) != null) {
                graph.addVertex(parentPath + unescape(l));
            }
        } finally {
            is.close();
        }
    }

    private void readEdges(final File file,
                           final Graph graph,
                           final String parentPath) throws IOException {
        //System.out.println("parent path: " + parentPath);
        InputStream is = new FileInputStream(file);
        try {
            InputStreamReader r = new InputStreamReader(is, "UTF-8");
            BufferedReader br = new BufferedReader(r);
            String l;
            while ((l = br.readLine()) != null) {
                int t1 = l.indexOf('\t');
                int t2 = l.indexOf('\t', t1 + 1);
                int t3 = l.indexOf('\t', t2 + 1);
                if (t1 < 1 || t2 < 3 || t3 < 5) {
                    throw new IOException("badly-formatted '" + EDGES + "' file: " + file);
                }

                String idStr = l.substring(0, t1);
                String outStr = l.substring(t1 + 1, t2);
                String inStr = l.substring(t2 + 1, t3);
                String labelStr = l.substring(t3 + 1);

                //System.out.println("looking for vertex: " + parentPath + ":" + outStr);
                Vertex outV = graph.getVertex(parentPath + unescape(outStr));
                if (null == outV) {
                    throw new IOException("vertex not found: " + parentPath + unescape(outStr));
                }
                Vertex inV = graph.getVertex(parentPath + unescape(inStr));
                if (null == inV) {
                    throw new IOException("vertex not found: " + parentPath + unescape(inStr));
                }

                String label = unescape(labelStr);
                graph.addEdge(parentPath + unescape(idStr), outV, inV, label);
            }
        } finally {
            is.close();
        }
    }

    private void readProperties(final File file,
                                final Graph graph,
                                final String parentPath,
                                final boolean edgesVsVertices) throws IOException {
        InputStream is = new FileInputStream(file);
        try {
            InputStreamReader r = new InputStreamReader(is, "UTF-8");
            BufferedReader br = new BufferedReader(r);
            String l;
            while ((l = br.readLine()) != null) {
                //System.out.println("l = " + l);
                int t1 = l.indexOf('\t');
                int t2 = l.indexOf('\t', t1 + 1);
                if (t1 < 1 || t2 < 3) {
                    throw new IOException("badly-formatted '" + (edgesVsVertices ? EDGE_PROPERTIES : VERTEX_PROPERTIES) + "' file");
                }

                String idStr = l.substring(0, t1);
                String keyStr = l.substring(t1 + 1, t2);
                String valStr = l.substring(t2 + 1);

                String id = parentPath + unescape(idStr);
                Element e = edgesVsVertices
                        ? graph.getEdge(id)
                        : graph.getVertex(id);

                String key = unescape(keyStr);

                Object val = deserializer.deserialize(valStr);

                e.setProperty(key, val);
            }
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        } finally {
            is.close();
        }
    }

    private static String escape(final String s) {
        return s.replaceAll("\\\\", "\\\\")
                .replaceAll("\\n", "\\\\n")   // Line breaks need to be escaped
                .replaceAll("\\t", "\\\\t");  // So do tabs, which delimit fields in the files.
    }

    private static String unescape(final String s) {
        return s.replaceAll("\\\\t", "\t")
                .replaceAll("\\\\n", "\n")
                .replaceAll("\\\\", "\\");
    }

    /*
    public static void main(final String[] args) throws Exception {
        show("weird\tlabel");
    }

    private static void show(String s) {
        System.out.println(s + " -- " + escape(s) + " -- " + unescape(escape(s)));
    }
    */

    private class RelativeId {
        private final String path;
        private final String name;

        public RelativeId(final String id,
                          final String parentPath) {
            if (!id.startsWith(parentPath)) {
                throw new IllegalStateException("id '" + id + "' is not in the expected path '" + parentPath + "'");
            }

            String l = id.substring(parentPath.length());
            int i = l.lastIndexOf("/");
            if (i < 0) {
                path = "";
                name = l;
            } else {
                path = l.substring(0, i + 1);
                name = l.substring(i + 1);
            }

            if (0 == name.length()) {
                throw new IllegalStateException("empty local part of ID: '" + id + "'");
            }
        }

        public String getPath() {
            return path;
        }

        public String getName() {
            return name;
        }
    }

    /*
    private class ObjectSerializer {
        private final ByteArrayOutputStream helper;
        private final ObjectOutputStream out;

        public ObjectSerializer() throws IOException {
            helper = new ByteArrayOutputStream();
            out = new ObjectOutputStream(helper);
        }

        public byte[] serialize(final Serializable obj) throws IOException {
            helper.reset();
            out.writeObject(obj);
            out.flush();
            return helper.toByteArray();
        }
    }

    private class ObjectDeserializer {
        public Object deserialize(final byte[] data) throws IOException, ClassNotFoundException {
            // "Trick" ObjectInputStream by prepending ObjectOutputStream's header to the input data.
            byte[] buffer = new byte[data.length + 4];
            System.arraycopy(data, 0, buffer, 4, data.length);
            buffer[0] = -84;
            buffer[1] = -19;
            buffer[2] = 0;
            buffer[3] = 5;

            ByteArrayInputStream helper = new ByteArrayInputStream(buffer);

            // TODO: just how inefficient is this (creating a new stream for each object)?  Perhaps reset() instead.
            ObjectInputStream in = new ObjectInputStream(helper);
            try {
                return in.readObject();
            } finally {
                in.close();
            }
        }
    } */

    private class ObjectSerializer {
        public byte[] serialize(final Serializable obj) throws IOException {
            Type t = findType(obj);
            if (null == t) {
                throw new IOException("property value is of an unsupported class (" + obj.getClass() + "): " + obj);
            }

            return ("" + t + "\t" + escape("" + obj)).getBytes();
        }
    }

    private class ObjectDeserializer {
        public Object deserialize(final String data) throws IOException, ClassNotFoundException {
            int i = data.indexOf("\t");
            if (i < 1) {
                throw new IOException("badly-formatted properties file");
            }

            Type t = Type.valueOf(data.substring(0, i));
            String val = unescape(data.substring(i + 1));

            switch (t) {
                case String:
                    return val;
                case Integer:
                    return Integer.valueOf(val);
                case Long:
                    return Long.valueOf(val);
                case Boolean:
                    return Boolean.valueOf(val);
                case Double:
                    return Double.valueOf(val);
                case Float:
                    return Float.valueOf(val);
                default:
                    throw new IllegalStateException("unexpected type " + t);
            }
        }
    }

    private Type findType(final Object obj) {
        return obj instanceof String
                ? Type.String
                : obj instanceof Integer
                ? Type.Integer
                : obj instanceof Long
                ? Type.Long
                : obj instanceof Boolean
                ? Type.Boolean
                : obj instanceof Float
                ? Type.Float
                : obj instanceof Double
                ? Type.Double
                : null;

    }

    private enum Type {
        String, Integer, Long, Boolean, Double, Float
    }
}
