package com.tinkerpop.blueprints.util.io.gdf;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.batch.BatchGraph;

/**
 * Parses GDF files and builds a {@link Graph}
 * 
 * @author Anuj (https://github.com/anujsrc)
 */
public class GDFReader {
	
	/**
	 * Used to keep the data type of the property
	 */
	private static final String DATA_TYPE = "DT";
	
	/**
	 * Used to keep the default value of the property
	 */
	private static final String DEFAULT_VALUE = "DEF_VAL";
	
	/**
	 * Inputs the GDF Stream data into {@link Graph}
	 * @param g Target {@link Graph} to be populated
	 * @param gdfis An {@link InputStream} of GDF data
	 */
	public static void inputGraph(final Graph g, final InputStream gdfis){
		GDFReader.inputGraph(g, gdfis, 1000, "\"");
	}
	
	/**
	 * Inputs the GDF Stream data into {@link Graph}
	 * @param g Target {@link Graph} to be populated
	 * @param gdfis An {@link InputStream} of GDF data
	 * @param buf amount of elements to hold in memory before committing a transaction
	 */
	public static void inputGraph(final Graph g, final InputStream gdfis, int buf){
		GDFReader.inputGraph(g, gdfis, buf, "\"");
	}
	
	/**
	 * Inputs the GDF Stream data into {@link Graph}
	 * @param g Target {@link Graph} to be populated
	 * @param gdfis An {@link InputStream} of GDF data
	 * @param quote Quote character, like- " or '
	 */
	public static void inputGraph(final Graph g, final InputStream gdfis, String quote){
		GDFReader.inputGraph(g, gdfis, 1000, quote);
	}
	
	/**
	 * Inputs the GDF Stream data into {@link Graph}
	 * @param g Target {@link Graph} to be populated
	 * @param gdfis An {@link InputStream} of GDF data
	 * @param buf amount of elements to hold in memory before committing a transaction
	 * @param quote Quote character, like- " or '
	 */
	public static void inputGraph(final Graph g,
			final InputStream gdfis, int buf, String quote){
		// to scan through the gdf data
		Scanner fsc = new Scanner(gdfis);
		final BatchGraph<?> graph = BatchGraph.wrap(g, buf);
		
		// holders for node and edge properties
		List<String> nprops = new ArrayList<String>();
		List<String> eprops = new ArrayList<String>();
		Map<String, Map<String, Object>> nattr = new HashMap<String, Map<String,Object>>();
		Map<String, Map<String, Object>> eattr = new HashMap<String, Map<String,Object>>();
		
		// prepare the regex
		String fmtreg = String.format(",(?=([^%1$s]*%1$s[^%1$s]*%1$s)*[^%1$s]*$)", quote);
		// flag to track the edge definition
		boolean edgedef = false;
		
		// process line by line
		while(fsc.hasNextLine()){
			String lntxt = fsc.nextLine().trim();
			// skip empty lines and till you get to the start
			if((lntxt.isEmpty()) || ((! lntxt.startsWith(
					GDFTokens.NODE_START)) && (nprops.isEmpty()))){
				continue;
			}
			// look for node/edge definition
			if(lntxt.startsWith(GDFTokens.NODE_START)){
				lntxt = lntxt.replaceFirst(GDFTokens.NODE_START, "").trim();
				// get the tokens
				String[] tokens = lntxt.split(fmtreg);
				createProps(tokens, nprops, nattr, quote);
			} else if(lntxt.startsWith(GDFTokens.EDGE_START)){
				lntxt = lntxt.replaceFirst(GDFTokens.EDGE_START, "").trim();
				// get the tokens
				String[] tokens = lntxt.split(fmtreg);
				createProps(tokens, eprops, eattr, quote);
				edgedef = true;
			} else {
				// get the tokens
				String[] tokens = lntxt.split(fmtreg);
				if(edgedef) {
					addEdge(graph, tokens, eprops, eattr, quote);
				} else {
					addVertex(graph, tokens, nprops, nattr, quote);
				}
			}
		}
		graph.commit();
		fsc.close();
	}

	/**
	 * Adds a new edge to the graph
	 * @param graph {@link Graph}
	 * @param tokens edge tokens
	 * @param eprops {@link List} of Properties
	 * @param eattr {@link Map} of Attributes
	 * @param quote Quote character, like- " or '
	 */
	private static void addEdge(Graph graph, String[] tokens, List<String> eprops,
			Map<String, Map<String, Object>> eattr, String quote) {
		// base check
		if(eprops.isEmpty() || (tokens.length < 2)) return;
		// check for at-least two properties
		if(eprops.size() < 2) return;
		
		// get the first two node IDs
		String nidProp1 = eprops.get(0);
		String nidProp2 = eprops.get(1);		
		Object nid1 = clearToken(tokens[0], quote);
		Object nid2 = clearToken(tokens[1], quote);
		nid1 = getValue(nidProp1, nid1, eattr);
		nid2 = getValue(nidProp2, nid2, eattr);
		if((nid1 == null) || (nid1.toString().isEmpty())) return;
		if((nid2 == null) || (nid2.toString().isEmpty())) return;
		
		// get the vertex
		Vertex svtx = graph.getVertex(nid1);
		Vertex tvtx = graph.getVertex(nid2);
		if((svtx == null) || (tvtx == null)) return;
		
		// set the default edge label
		String edgeLbl = GDFTokens.DEFAULT_EDGE_LABEL;
		
		Map<String, Object> epropVals = new HashMap<String, Object>();
		for(String eprop : eprops){
			// get the index of the property
			int pidx = eprops.indexOf(eprop);
			if(pidx < tokens.length){
				if(eprop.equalsIgnoreCase(GDFTokens.EDGE_LABEL_PROP)){
					edgeLbl = clearToken(tokens[pidx], quote);
					edgeLbl = (String) getValue(eprop, edgeLbl, eattr);
				} else {
					Object value = clearToken(tokens[pidx], quote);
					value =  getValue(eprop, value, eattr);
					if(value != null) epropVals.put(eprop, value);
				}
			} else {
				// check if default value is defined
				Object value = getValue(eprop, null, eattr);
				if(value != null) epropVals.put(eprop, value);
			}
		}
		// create the edge
		Edge ed = graph.addEdge(null, svtx, tvtx, edgeLbl);
		for(String ep : epropVals.keySet()){
			ed.setProperty(ep, epropVals.get(ep));
		}
	}

	/**
	 * Adds a new vertex to the graph
	 * @param graph {@link Graph}
	 * @param tokens vertex tokens
	 * @param nprops {@link List} of Properties
	 * @param nattr {@link Map} of Attributes
	 * @param quote Quote character, like- " or '
	 */
	private static void addVertex(Graph graph, String[] tokens, List<String> nprops,
			Map<String, Map<String, Object>> nattr, String quote) {
		if(nprops.isEmpty() || (tokens.length == 0)) return;
		// get the name property
		String idProp = nprops.get(0);
		Object id = clearToken(tokens[0], quote);
		
		id = getValue(idProp, id, nattr);
		if((id == null) || (id.toString().isEmpty())) return;
		
		// add vertex
		Vertex v = graph.getVertex(id);
		if(v == null) {
			v = graph.addVertex(id);
		}
		for(String nprop : nprops){
			// get the index of the property
			int pidx = nprops.indexOf(nprop);
			if(pidx < tokens.length){
				// check for vertex Name/ID
				if((pidx == 0) || (nprop.equalsIgnoreCase("name"))){
					continue;
				} else {
					// set vertex property
					String tokval = clearToken(tokens[pidx], quote);
					Object value = getValue(nprop, tokval, nattr);
					if(value != null) v.setProperty(nprop, value);
				}
			} else {
				// check if default value is defined
				Object value = getValue(nprop, null, nattr);
				if(value != null) v.setProperty(nprop, value);
			}
		}
	}

	/**
	 * Creates Properties for Node/Edge
	 * @param tokens Property Tokens
	 * @param props {@link List} of Properties
	 * @param attr {@link Map} of Attributes
	 * @param quote Quote character, like- " or '
	 */
	private static void createProps(String[] tokens, List<String> props,
			Map<String, Map<String, Object>> attr, String quote) {
		for(String tok : tokens){
			tok = clearToken(tok, quote);
			String[] tokAttr = tok.split("\\s");
			// handle all cases including-
			// name,style,dept VARCHAR(32),salary INT default 40000
			int ti = 0;
			for(String ta : tokAttr){
				ta = ta.trim();
				if(ta.isEmpty()) continue;
				if(ti == 0){
					props.add(ta);
				} else if(ti == 1){
					String lbl = props.get(props.size()-1);
					if(! attr.containsKey(lbl)){
						attr.put(lbl, new HashMap<String, Object>());
					}
					attr.get(lbl).put(DATA_TYPE, ta);
				} else if(ti == 2){
					// check for default, else break
					if(! ta.equalsIgnoreCase(GDFTokens.DEFAULT_KEY)) break;
				} else if(ti == 3){
					String lbl = props.get(props.size()-1);
					if(! attr.containsKey(lbl)){
						attr.put(lbl, new HashMap<String, Object>());
					}
					attr.get(lbl).put(DEFAULT_VALUE, ta);
				}
				++ti;
			}
		}
	}
	
	/**
	 * Clears the token of extra spaces
	 * and quote characters
	 * @param tok Target token
	 * @param quote Quote character, like- " or '
	 * @return cleaned token
	 */
	private static String clearToken(String tok, String quote){
		if(tok == null) return tok;
		tok = tok.trim();
		if(tok.startsWith(quote)){
			tok = tok.substring(quote.length(), tok.lastIndexOf(quote));
		}
		return tok;
	}
	
	/**
	 * Gets the typed value
	 * @param prop Property Name
	 * @param value Target Value
	 * @param nattr {@link Map} of Attributes
	 * @return typed value
	 */
	private static Object getValue(String prop, Object value,
			Map<String, Map<String, Object>> nattr) {
		
		// basic validation
		if((prop == null) || (prop.trim().isEmpty()) ||
				(! nattr.containsKey(prop))) return value;
		
		// get the type
		String type = nattr.get(prop).get(DATA_TYPE).toString();
		if((value == null) || (value.toString().isEmpty())){
			if(! nattr.get(prop).containsKey(DEFAULT_VALUE)){
				return null;
			}
			// get default value
			value = nattr.get(prop).get(DEFAULT_VALUE);
		}
		String pval = String.valueOf(value).trim();
		// get typed value
        if (null == type || type.toLowerCase().startsWith(
        		GDFTokens.STRING.toLowerCase()))
            return pval;
        else if (type.equalsIgnoreCase(GDFTokens.BOOLEAN))
            return Boolean.valueOf(pval);
        else if (type.equalsIgnoreCase(GDFTokens.INT))
            return Integer.valueOf(pval);
        else if (type.equalsIgnoreCase(GDFTokens.LONG))
            return Long.valueOf(pval);
        else if (type.equalsIgnoreCase(GDFTokens.FLOAT))
            return Float.valueOf(pval);
        else if (type.equalsIgnoreCase(GDFTokens.DOUBLE))
            return Double.valueOf(pval);
        else
            return pval;
    }
	
}
