package com.tinkerpop.blueprints.pgm.oupls.jung;

import java.util.Set;

import com.tinkerpop.blueprints.pgm.CloseableSequence;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraphFactory;
import com.tinkerpop.blueprints.pgm.oupls.IndexableGraphSource;

/**
 * 
 * @author Riccardo Tasso
 *
 */
public class IndexableGraphJung extends GraphJung implements edu.uci.ics.jung.graph.Graph<Vertex, Edge>, IndexableGraphSource {

	public IndexableGraphJung(IndexableGraph graph) {
		super(graph);
	}
	
	public IndexableGraph getGraph() {
        return (IndexableGraph) super.getGraph();
    }
	
	// TODO: evaluate the introduction of an interface IndexPolicy<T extends Element> which has the following:
	// * List<String> getIndices(T element): given an element (e.g. a Vertex) checks it's properties and returns an ordered list
	// containing the indexes in which search for it;
	// boolean isComplete(T Element): returns true if the policy for that given element is complete, i.e. if knowing
	// that those indexes don't contain the element, we can assume it won't be in the graph
	
	public boolean containsVertex(final Vertex vertex) {
		
		for(Index<? extends Element> idx : getGraph().getIndices()) {
			if(		idx instanceof Index &&
					Vertex.class.isAssignableFrom(idx.getIndexClass())
					) {
				@SuppressWarnings("unchecked")
				Index<Vertex> vertexIdx = (Index<Vertex>) idx;
				
				Set<String> vKeys = vertex.getPropertyKeys();
				for(String property : vKeys) {
					CloseableSequence<Vertex> it = vertexIdx.get(property, vertex.getProperty(property));
					while(it.hasNext()) {
						Vertex vc = it.next();
						while(it.hasNext()) {
							Vertex vf = it.next();
							if(vf.equals(vc))
								return true;
						}
					}	
					
				}
			}
		}
		
		// TODO: it should be nice return false, but we don't know if the indexes are complete
		return super.containsVertex(vertex);
	}
	
	public boolean containsEdge(final Edge edge) {
		
		for(Index<? extends Element> idx : getGraph().getIndices()) {
			if(		idx instanceof Index &&
					Edge.class.isAssignableFrom(idx.getIndexClass())
					) {
				@SuppressWarnings("unchecked")
				Index<Edge> edgeIdx = (Index<Edge>) idx;
				
				Set<String> eKeys = edge.getPropertyKeys();
				for(String property : eKeys) {
					CloseableSequence<Edge> it = edgeIdx.get(property, edge.getProperty(property));
					while(it.hasNext()) {
						Edge ec = it.next();
						while(it.hasNext()) {
							Edge ef = it.next();
							if(ef.equals(ec))
								return true;
						}
					}	
					
				}
			}
		}
		
		// TODO: it should be nice return false, but we don't know if the indexes are complete
		return super.containsEdge(edge);
	}
	
	
	
	// TODO: question: Have I to handle here the add/remove vertex/edge considering the indexable graph?

	public static void main(String args[]) {
		IndexableGraph g = TinkerGraphFactory.createTinkerGraph();
		
		IndexableGraphJung graph = new IndexableGraphJung(g);
		
		System.out.println(graph.containsVertex(g.addVertex("pippo")));
	}
	
	// classes that must not be touched, nor extended:
	// JungHelper
	// EdgeLabelTransformer
	// EdgeLabelWeightTransformer
	// EdgeWeightTransformer
	
}
