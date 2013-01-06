package model.graph;

import java.util.HashMap;

/**
 * The Edge class implements an edge in a graph.
 */
public class Edge {
	
	/**
	 * Start vertex
	 */
    private Vertex tail;
    
    /**
     * End vertex
     */
    private Vertex head;
    
    /**
     * Attributes
     */
    private HashMap<Key, Data> attributes = new HashMap<Key, Data>();
    
    /**
     * Key object for hashing
     */
    private final Key key;

	/**
     * If the graph is not directed, this is used to store a counter edge reference
     */
    private Edge counterEdge;
    
    public Edge(Vertex tail, Vertex head) {
        key = new Key(Graph.internalIdCounter++);
		setTail(tail);
		setHead(head);
		head.addIngoingEdge(this);
		tail.addOutgoingEdge(this);
    }

	public Vertex getTail() {
		return tail;
	}

	public void setTail(Vertex tail) {
		this.tail = tail;
	}

	public Vertex getHead() {
		return head;
	}

	public void setHead(Vertex head) {
		this.head = head;
	}

	public Edge getCounterEdge() {
		return counterEdge;
	}

	public void setCounterEdge(Edge counterEdge) {
		this.counterEdge = counterEdge;
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
    	String str = this.getClass().getSimpleName() + " (" + tail.getName() + "->" + head.getName() + ") ";
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