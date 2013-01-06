package model.graph;

import java.util.Collection;
import java.util.HashMap;

/**
 * The Vertex class implements a vertex in a graph.
 */
public class Vertex {
    
    private String name;
	
    private HashMap<Key, Edge> ingoingEdges = new HashMap<Key, Edge>();

    private HashMap<Key, Edge> outgoingEdges = new HashMap<Key, Edge>();
    
    private HashMap<Key, Data> attributes = new HashMap<Key, Data>();
    
    private final Key key;
    
    public Vertex() {
    	key = new Key(Graph.internalIdCounter++);
    }
    
    public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void addIngoingEdge(Edge e) {
    	ingoingEdges.put(e.getKey(), e);
    }
    
    public void addOutgoingEdge(Edge e) {
    	outgoingEdges.put(e.getKey(), e);
    }
    
    public void removeIngoingEdge(Edge e) {
    	ingoingEdges.remove(e.getKey());
    }
    
    public void removeOutgoingEdge(Edge e) {
    	outgoingEdges.remove(e.getKey());
    }

    public Collection<Edge> getIngoingEdges() {
        return (this.ingoingEdges.values());
    }

    public Collection<Edge> getOutgoingEdges() {
        return (this.outgoingEdges.values());
    }

	public Key getKey() {
		return key;
	}
    
    public void addAttribute(Data attribute, Key attributeKey) {
    	attributes.put(attributeKey, attribute);
    }
    
    public Data getAttribute(Key attributeKey) {
    	if (!attributes.containsKey(attributeKey)) {
    		attributes.put(attributeKey, new Data());
    	}
    	return attributes.get(attributeKey);
    }
    
    public Data removeAttribute(Key attributeKey) {
    	return attributes.remove(attributeKey);
    }
    
    public String toString() {
    	String str = this.getClass().getSimpleName() + " ";
    	if (name != null) {
        	str += name + " ";
    	}
    	if (!attributes.isEmpty()) {
    		str += "[";
    		for (Data a : attributes.values()) {
    			str += a.toString() + ", ";
    		}
    		str = str.substring(0,str.length()-2) + "]";
    	}
    	return str;
    }

}
