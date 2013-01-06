package model.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

/**
 * The Graph class implements a graph.
 */
public class Graph {
	
	private boolean isDirected;
	
	private boolean isSimple;

	private String name;
	
	/**
	 * Vertices of the graph.
	 */
	private HashMap<Key, Vertex> vertices = new HashMap<Key, Vertex>();
	
	/**
	 * Edges of the graph.
	 */
	private HashMap<Key, Edge> edges = new HashMap<Key, Edge>();
	
    // For fast access to attribute keys via their description or vice versa
    private Vector<Key> vertexAttributeKeys = new Vector<Key>();
    
    private Vector<String> vertexAttributeDescriptions = new Vector<String>();
    
    private Vector<Key> edgeAttributeKeys = new Vector<Key>();
    
    private Vector<String> edgeAttributeDescriptions = new Vector<String>();

    //id counter used for edge, vertex and attribute construction
    protected static Long internalIdCounter = 0L;
    
    public Graph() {}
	
	public boolean isDirected() {
		return isDirected;
	}

	public void setDirected(boolean isDirected) {
		this.isDirected = isDirected;
	}

	public boolean isSimple() {
		return isSimple;
	}

	public void setSimple(boolean isSimple) {
		this.isSimple = isSimple;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void addVertex(Vertex v) {
		vertices.put(v.getKey(), v);
	}
	
	/**
	 * Gets a vertex with the specified key.
	 * @param key The key of the vertex.
	 * @return The vertex with this key.
	 */
	public Vertex getVertex(Key key) {
		return vertices.get(key);
	}
	
	/**
	 * Removes the vertex from the graph.
	 * @param v The vertex to be removed.
	 */
	public void removeVertex(Vertex v) {
		vertices.remove(v.getKey());
		for (Edge e : v.getOutgoingEdges()) {
			edges.remove(e.getKey());
			e.getHead().removeIngoingEdge(e);
		}
		for (Edge e : v.getIngoingEdges()) {
			edges.remove(e.getKey());
			e.getTail().removeOutgoingEdge(e);
		}
	}
	
	public Collection<Vertex> getVertices() {
		return vertices.values();
	}
	
	public void addEdge(Edge e) {
		edges.put(e.getKey(), e);
		// If the graph is undirected, add an counter edge
		if (!isDirected) {
			Edge counterEdge = new Edge(e.getHead(), e.getTail());
			e.setCounterEdge(counterEdge);
			counterEdge.setCounterEdge(e);
			edges.put(counterEdge.getKey(), counterEdge);
			for (int j=0; j<edgeAttributeKeys.size(); j++) {
				Key attributeKey = edgeAttributeKeys.elementAt(j);
				counterEdge.addAttribute(e.getAttribute(attributeKey), attributeKey);
			}
		}
	}
	
	public Edge getEdge(Key key) {
		return edges.get(key);
	}
	
	public Vector<Edge> getEdges() {
		Vector<Edge> vec = new Vector<Edge>();
		if (isDirected) {
			for (Edge e: edges.values()) {
				vec.add(e);
			}
		} else {
			// TODO if the graph is undirected
		}
		return vec;
	}
	
	public Key addVertexAttribute(String description) {
		if (vertexAttributeDescriptions.contains(description)) {
			System.err.println("This description for the vertex attribute already available: " + description);
			return null;
		}
		Key k = new Key(internalIdCounter++);
		vertexAttributeKeys.add(k);
		vertexAttributeDescriptions.add(description);
		return k;
	}
	
	public Key getKeyOfVertexAttribute(String description) {
		if (!vertexAttributeDescriptions.contains(description)) {
			System.err.println("Unknown description for the vertex attribute: " + description);
			return null;
		}
		int idx = vertexAttributeDescriptions.indexOf(description);
		return vertexAttributeKeys.get(idx);
	}
	
	public void removeVertexAttribute(Key attributeKey) {
		if (vertexAttributeKeys.contains(attributeKey)) {
			for (Vertex v : vertices.values()) {
				v.removeAttribute(attributeKey);
			}
			int idx = vertexAttributeKeys.indexOf(attributeKey);
			vertexAttributeDescriptions.remove(idx);
			vertexAttributeKeys.remove(idx);
		}
	}
	
	public Key addEdgeAttribute(String description) {
		if (edgeAttributeDescriptions.contains(description)) {
			System.err.println("This description for the edge attribute already available: " + description);
			return null;
		}
		Key k = new Key(internalIdCounter++);
		edgeAttributeKeys.add(k);
		edgeAttributeDescriptions.add(description);
		return k;
	}

	public Key getKeyOfEdgeAttribute(String description) {
		if (!edgeAttributeDescriptions.contains(description)) {
			System.err.println("Unknown description for the edge attribute: " + description);
			return null;
		}
		int idx = edgeAttributeDescriptions.indexOf(description);
		return edgeAttributeKeys.get(idx);
	}

	public void removeEdgeAttribute(Key attributeKey) {
		if (edgeAttributeKeys.contains(attributeKey)) {
			for(Edge e : edges.values()) {
				e.removeAttribute(attributeKey);
			}
			int idx = edgeAttributeKeys.indexOf(attributeKey);
			edgeAttributeDescriptions.remove(idx);
			edgeAttributeKeys.remove(idx);
		}
	}
	
	public String toString() {
		String str = name + "\n";
		str += "Number of vertices: " + vertices.size() + "\n";
		for (Vertex v : getVertices()) {
			str += v + "\n";
		}
		str += "Number of edges: " + edges.size() + "\n";
		for (Edge e : getEdges()) {
			str += e + "\n";
		}
		return str;
	}
	
	public boolean isConsistent() {
		boolean good = true;
        //check if edge is stored as in/outgoing edge at tail/head
		for (Edge e : edges.values()) {
			if (!e.getHead().getIngoingEdges().contains(e)) {
				System.err.println("Edge is not saved correctly as an ingoing edge:");
				System.err.println(e);
				return !good;
			}
			if (!e.getTail().getOutgoingEdges().contains(e)) {
				System.err.println("Edge is not saved correctly as an outgoing edge:");
				System.err.println(e);
				return !good;
			}
			if (!vertices.containsKey(e.getHead().getKey())) {
				System.err.println("A head of an edge is not saved in the graph:");
				System.err.println(e.getHead());
				return !good;
			}
			if (!vertices.containsKey(e.getTail().getKey())) {
				System.err.println("A tail of an edge is not saved in the graph:");
				System.err.println(e.getTail());
				return !good;
			}
			if (!isDirected) {
				if (e.getCounterEdge() == null) {
					System.err.println("Found an edge without a counter edge in the undirected graph:");
					System.err.println(e);
					return !good;
				}
				// TODO
			}
		}
		for (Vertex v : vertices.values()) {
			for (Edge e : v.getIngoingEdges()) {
				if (!edges.containsKey(e.getKey())) {
					System.err.println("An ingoing edge is not saved in the graph:");
					System.err.println(e);
					return !good;
				}
			}
			for (Edge e : v.getOutgoingEdges()) {
				if (!edges.containsKey(e.getKey())) {
					System.err.println("An outgoing edge is not saved in the graph:");
					System.err.println(e);
					return !good;
				}
			}
		}
		return good;
	}
	
}
