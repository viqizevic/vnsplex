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
     * datas
     */
    private HashMap<Key, Data> datas = new HashMap<Key, Data>();
    
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
    	String str = this.getClass().getSimpleName() + " (" + tail.getName() + "->" + head.getName() + ") ";
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