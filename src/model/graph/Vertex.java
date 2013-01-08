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
    
    private HashMap<Key, Data> datas = new HashMap<Key, Data>();
    
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
    
    public void addData(Data data, Key dataKey) {
    	datas.put(dataKey, data);
    }
    
    public Data getData(Key dataKey) {
    	if (!datas.containsKey(dataKey)) {
    		datas.put(dataKey, new Data());
    	}
    	return datas.get(dataKey);
    }
    
    public Data removeData(Key dataKey) {
    	return datas.remove(dataKey);
    }
    
    public String toString() {
    	String str = this.getClass().getSimpleName() + " ";
    	if (name != null) {
        	str += name + " ";
    	}
    	if (!datas.isEmpty()) {
    		str += "[";
    		for (Data a : datas.values()) {
    			str += a.toString() + ", ";
    		}
    		str = str.substring(0,str.length()-2) + "]";
    	}
    	return str;
    }

}
