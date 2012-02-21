package com.tinkerpop.blueprints.pgm.util.io.graphml;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import com.tinkerpop.blueprints.pgm.CloseableSequence;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.Index.Type;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 * @author Salvatore Piccione (TXT e-solutions SpA)
 */
public class GraphMLWriterTest extends TestCase {
    
    public void testNormal() throws Exception {
        TinkerGraph g = new TinkerGraph();
        GraphMLReader.inputGraph(g, GraphMLReader.class.getResourceAsStream("graph-example-1.xml"));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GraphMLWriter w = new GraphMLWriter(g);
        w.setNormalize(true);
        w.outputGraph(bos);

        String expected = streamToString(GraphMLWriterTest.class.getResourceAsStream("graph-example-1-normalized.xml"));
        //System.out.println(expected);
        assertEquals(expected.replace("\n", "").replace("\r", ""), bos.toString().replace("\n", "").replace("\r", ""));
    }

    // Note: this is only a very lightweight test of writer/reader encoding.
    // It is known that there are characters which, when written by GraphMLWriter,
    // cause parse errors for GraphMLReader.
    // However, this happens uncommonly enough that is not yet known which characters those are.
    public void testEncoding() throws Exception {

        Graph g = new TinkerGraph();
        Vertex v = g.addVertex(1);
        v.setProperty("text", "\u00E9");

        GraphMLWriter w = new GraphMLWriter(g);

        File f = File.createTempFile("test", "txt");
        OutputStream out = new FileOutputStream(f);
        w.outputGraph(out);
        out.close();

        Graph g2 = new TinkerGraph();
        GraphMLReader r = new GraphMLReader(g2);

        InputStream in = new FileInputStream(f);
        r.inputGraph(in);
        in.close();

        Vertex v2 = g2.getVertex(1);
        assertEquals("\u00E9", v2.getProperty("text"));
    }
    
    public void testCustomEdgeLabel () throws Exception {
        String edgeLabelKey = "label";
        TinkerGraph g = new TinkerGraph();
        GraphMLReader reader = new GraphMLReader(g);
        reader.setEdgeLabelKey(edgeLabelKey);
        reader.inputGraph(GraphMLReader.class.getResourceAsStream("graph-example-1_customEdgeLabel.xml"));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GraphMLWriter w = new GraphMLWriter(g);
        w.setNormalize(true);
        w.setEdgeLabelKey(edgeLabelKey);
        w.outputGraph(bos);

        String expected = streamToString(GraphMLWriterTest.class.getResourceAsStream("graph-example-1-normalized_customEdgeLabel.xml"));
        //System.out.print(bos.toString());
        assertEquals(expected.replace("\n", "").replace("\r", ""), bos.toString().replace("\n", "").replace("\r", ""));
    }
    public void testAutomaticIndex () throws Exception {
        String edgeLabelKey = "label";
        TinkerGraph g = new TinkerGraph();
        GraphMLReader reader = new GraphMLReader(g);
        reader.setEdgeLabelKey(edgeLabelKey);
        reader.inputGraph(GraphMLReader.class.getResourceAsStream("graph-example-1_autoIndex.xml"));
        
        String autoIndexName = "age-idx";
        Index<Vertex> autoIndex = g.getIndex(autoIndexName, Vertex.class);
        assertNotNull("The automatic index '" + autoIndexName + "' does not exist", autoIndex);
        assertEquals("The automatic index '" + autoIndexName + "' is not AUTOMATIC", Type.AUTOMATIC, autoIndex.getIndexType());
        
        final int EXPECTED_LENGTH = 4;
        String[] keys = new String[EXPECTED_LENGTH];
        Arrays.fill(keys, "age");
        int[] ages = new int[]{29,27,32,35};
        String[] names = new String[] {"marko","vadas","josh","peter"};
        CloseableSequence<Vertex> indexedVertices;
        Vertex indexedVertex;
        for (int i = 0; i < EXPECTED_LENGTH; i++) {
            indexedVertices = autoIndex.get(keys[i], ages[i]);
            assertTrue("There is no auto indexed value for the pair: " + keys[i] + " - " + ages[i], indexedVertices.hasNext());
            indexedVertex = indexedVertices.next();
            assertEquals("The vertex identified by the pair: "+ keys[i] + " - " + ages[i] + "has not got the right name!",
                names[i],indexedVertex.getProperty("name"));
            assertFalse("The automatic index '" + autoIndexName + "' has more than one element", indexedVertices.hasNext());
            indexedVertices.close();
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GraphMLWriter w = new GraphMLWriter(g);
        w.setNormalize(true);
        w.setXmlSchemaLocation("../../../../../../../../../main/resources/com/tinkerpop/blueprints/pgm/util/io/graphml/ext-xsd/graphml_autoIndex_ext.xsd");
        w.setEdgeLabelKey(edgeLabelKey);
        w.outputGraph(bos);

        String expected = streamToString(GraphMLWriterTest.class.getResourceAsStream("graph-example-1-normalized_autoIndex.xml"));
        //System.out.print(bos.toString());
        assertEquals(expected.replace("\n", "").replace("\r", ""), bos.toString().replace("\n", "").replace("\r", ""));
    }
    
    public void testManualIndex () throws Exception {
        String edgeLabelKey = "label";
        TinkerGraph g = new TinkerGraph();
        GraphMLReader reader = new GraphMLReader(g);
        reader.setEdgeLabelKey(edgeLabelKey);
        reader.inputGraph(GraphMLReader.class.getResourceAsStream("graph-example-1_manualIndex.xml"));
        
        //javaLovers-idx
        int expectedLength = 1;
        String[] keys = new String[expectedLength];
        Arrays.fill(keys, "lang");
        String[] values = new String[expectedLength];
        Arrays.fill(values, "java");
        String[] propertyNames = new String[expectedLength];
        Arrays.fill(propertyNames, "id");
        Collection<Object> firstPropertyValues = new HashSet<Object>();
        firstPropertyValues.add("3");
        firstPropertyValues.add("5");
        List<Collection<Object>> listOfPropertyValues = new LinkedList<Collection<Object>>();
        listOfPropertyValues.add(firstPropertyValues);
        checkManualIndex("javaLovers-idx", g, keys, values, propertyNames, listOfPropertyValues, Vertex.class);
        
        //highWeight-idx
        keys = new String[]{"weight"};
        Float[] floatValues = new Float[]{(float) 1.0};
        propertyNames = new String[]{"id"};
        listOfPropertyValues.clear();
        firstPropertyValues.clear();
        firstPropertyValues.add("8");
        firstPropertyValues.add("10");
        listOfPropertyValues.add(firstPropertyValues);
        checkManualIndex("highWeight-idx", g, keys, floatValues, propertyNames, listOfPropertyValues, Edge.class);
        
        //lowWeight-idx
        keys = new String[]{"weight","weight"};
        floatValues = new Float[]{(float) 0.4,(float) 0.2};
        propertyNames = new String[]{"id","id"};
        listOfPropertyValues.clear();
        firstPropertyValues.clear();
        firstPropertyValues.add("9");
        firstPropertyValues.add("11");
        Collection<Object> secondPropertyValues = new LinkedList<Object>();
        secondPropertyValues.add("12");
        listOfPropertyValues.add(firstPropertyValues);
        listOfPropertyValues.add(secondPropertyValues);
        checkManualIndex("lowWeight-idx", g, keys, floatValues, propertyNames, listOfPropertyValues, Edge.class);
        
        //peter-idx
        keys = new String[]{"name","label"};
        values = new String[] {"peter","created"};
        propertyNames = new String[]{"id","id"};
        listOfPropertyValues.clear();
        firstPropertyValues.clear();
        firstPropertyValues.add("6");
        secondPropertyValues.clear();
        secondPropertyValues.add("12");
        listOfPropertyValues.add(firstPropertyValues);
        listOfPropertyValues.add(secondPropertyValues);
        checkManualIndex("peter-idx", g, keys, values, propertyNames, listOfPropertyValues, Element.class);
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GraphMLWriter w = new GraphMLWriter(g);
        w.setNormalize(true);
        w.setEdgeLabelKey(edgeLabelKey);
        w.setXmlSchemaLocation("../../../../../../../../../main/resources/com/tinkerpop/blueprints/pgm/util/io/graphml/ext-xsd/graphml_manualIndex_ext.xsd");
        w.outputGraph(bos);
        //System.out.print(bos.toString());
        
        String expected = streamToString(GraphMLWriterTest.class.getResourceAsStream("graph-example-1-normalized_manualIndex.xml"));
        assertEquals(expected.replace("\n", "").replace("\r", ""), bos.toString().replace("\n", "").replace("\r", ""));
    }
    
    private <T extends Element> void checkManualIndex (String manualIndexName, IndexableGraph g, 
            String[] keys, Object[] values, String[] indexedElementPropertyNames, 
            List<Collection<Object>> indexedElementPropertyValues, Class<T> indexClass) {
        Index<T> manualIndex = g.getIndex(manualIndexName, indexClass);
        assertNotNull("The manual index '" + manualIndexName + "' does not exist", manualIndex);
        assertEquals("The manual index '" + manualIndexName + "' is not MANUAL", Type.MANUAL, manualIndex.getIndexType());
        
        final int EXPECTED_LENGTH = keys.length;
        CloseableSequence<T> indexedVertices;
        T indexedElement;
        int noOfIndexedElements;
        int expectedNoOfIndexedElements;
        Object currentPropertyValue;
        Collection<Object> availablePropertyValues;
        for (int i = 0; i < EXPECTED_LENGTH; i++) {
            noOfIndexedElements = 0;
            indexedVertices = manualIndex.get(keys[i], values[i]);
            assertTrue("There is no indexed value for the pair: " + keys[i] + " - " + values[i], indexedVertices.hasNext());
            availablePropertyValues = indexedElementPropertyValues.get(i);
            expectedNoOfIndexedElements = availablePropertyValues.size();
            while (indexedVertices.hasNext()) {
                indexedElement = indexedVertices.next();
                if (indexedElementPropertyNames[i].equals(GraphMLTokens.ID))
                    currentPropertyValue = indexedElement.getId();
                else
                    currentPropertyValue = indexedElement.getProperty(indexedElementPropertyNames[i]);
                assertTrue("The element identified by the pair: "+ keys[i] + " - " + values[i] + " hasn't got the right value" +
                " for the property '" + indexedElementPropertyNames[i] + "'!",
                availablePropertyValues.contains(currentPropertyValue));
                availablePropertyValues.remove(currentPropertyValue);
                noOfIndexedElements++;
            }
            assertEquals("Wrong number of elements indexed by '" + manualIndexName + "'", expectedNoOfIndexedElements, noOfIndexedElements);
            indexedVertices.close();
        }
    }

    private String streamToString(final InputStream in) throws IOException {
        Writer writer = new StringWriter();

        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(
                    new InputStreamReader(in, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            in.close();
        }
        return writer.toString();
    }
    
    public void testExtraType () throws Exception {
        String edgeLabelKey = "label";
        TinkerGraph g = new TinkerGraph();
        GraphMLReader reader = new GraphMLReader(g);
        reader.setEdgeLabelKey(edgeLabelKey);
        reader.inputGraph(GraphMLReader.class.getResourceAsStream("graph-example-1_extraType.xml"));
        
        String propertyName = "date";
        String dateFormat = "yyyyMMdd";
        Object propertyValue;
        
        //check the date for vertex 1
        Vertex v = g.getVertex("1");
        propertyValue = v.getProperty(propertyName);
        assertTrue("The property '" + propertyName + "' is not a date!", propertyValue instanceof Date);
        assertEquals("The value of the property '" + propertyName + "' of the vertex '" + v.getId() + 
                "'does not match the expected value", new SimpleDateFormat(dateFormat).parse("19830210"), propertyValue);
        
        v = g.getVertex("2");
        propertyValue = v.getProperty(propertyName);
        assertTrue("The property '" + propertyName + "' is not a date!", propertyValue instanceof Date);
        assertEquals("The value of the property '" + propertyName + "' of the vertex '" + v.getId() + 
                "'does not match the expected value", new SimpleDateFormat(dateFormat).parse("19851010"), propertyValue);
        
        v = g.getVertex("4");
        propertyValue = v.getProperty(propertyName);
        assertTrue("The property '" + propertyName + "' is not a date!", propertyValue instanceof Date);
        assertEquals("The value of the property '" + propertyName + "' of the vertex '" + v.getId() + 
                "'does not match the expected value", new SimpleDateFormat(dateFormat).parse("19800807"), propertyValue);
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GraphMLWriter w = new GraphMLWriter(g);
        w.setNormalize(true);
        w.setEdgeLabelKey(edgeLabelKey);
        w.setXmlSchemaLocation("../../../../../../../../../main/resources/com/tinkerpop/blueprints/pgm/util/io/graphml/ext-xsd/graphml_extraType_ext.xsd");
        w.outputGraph(bos);
        //System.out.print(bos.toString());
        
        String expected = streamToString(GraphMLWriterTest.class.getResourceAsStream("graph-example-1-normalized_extraType.xml"));
        assertEquals(expected.replace("\n", "").replace("\r", ""), bos.toString().replace("\n", "").replace("\r", ""));
    }

}
