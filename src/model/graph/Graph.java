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
	
    // For fast access to data keys via their description or vice versa
    private Vector<Key> vertexDataKeys = new Vector<Key>();
    
    private Vector<String> vertexDataDescriptions = new Vector<String>();
    
    private Vector<Key> edgeDataKeys = new Vector<Key>();
    
    private Vector<String> edgeDataDescriptions = new Vector<String>();

    //id counter used for edge, vertex and data construction
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
	
	public int getNumberOfVertices() {
		return vertices.size();
	}
	
	public void addEdge(Edge e) {
		edges.put(e.getKey(), e);
		// If the graph is undirected, add an counter edge
		if (!isDirected) {
			Edge counterEdge = new Edge(e.getHead(), e.getTail());
			e.setCounterEdge(counterEdge);
			counterEdge.setCounterEdge(e);
			edges.put(counterEdge.getKey(), counterEdge);
			for (int j=0; j<edgeDataKeys.size(); j++) {
				Key dataKey = edgeDataKeys.elementAt(j);
				counterEdge.addData(e.getData(dataKey), dataKey);
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
			for (Edge e: edges.values()) {
				vec.add(e);
			}
		}
		return vec;
	}
	
	public Key addVertexData(String description) {
		if (vertexDataDescriptions.contains(description)) {
			System.err.println("This description for the vertex data already available: " + description);
			return null;
		}
		Key k = new Key(internalIdCounter++);
		vertexDataKeys.add(k);
		vertexDataDescriptions.add(description);
		return k;
	}
	
	public Key getKeyOfVertexData(String description) {
		if (!vertexDataDescriptions.contains(description)) {
			System.err.println("Unknown description for the vertex data: " + description);
			return null;
		}
		int idx = vertexDataDescriptions.indexOf(description);
		return vertexDataKeys.get(idx);
	}
	
	public void removeVertexData(Key dataKey) {
		if (vertexDataKeys.contains(dataKey)) {
			for (Vertex v : vertices.values()) {
				v.removeData(dataKey);
			}
			int idx = vertexDataKeys.indexOf(dataKey);
			vertexDataDescriptions.remove(idx);
			vertexDataKeys.remove(idx);
		}
	}
	
	public Key addEdgeData(String description) {
		if (edgeDataDescriptions.contains(description)) {
			System.err.println("This description for the edge data already available: " + description);
			return null;
		}
		Key k = new Key(internalIdCounter++);
		edgeDataKeys.add(k);
		edgeDataDescriptions.add(description);
		return k;
	}

	public Key getKeyOfEdgeData(String description) {
		if (!edgeDataDescriptions.contains(description)) {
			System.err.println("Unknown description for the edge data: " + description);
			return null;
		}
		int idx = edgeDataDescriptions.indexOf(description);
		return edgeDataKeys.get(idx);
	}

	public void removeEdgeData(Key dataKey) {
		if (edgeDataKeys.contains(dataKey)) {
			for(Edge e : edges.values()) {
				e.removeData(dataKey);
			}
			int idx = edgeDataKeys.indexOf(dataKey);
			edgeDataDescriptions.remove(idx);
			edgeDataKeys.remove(idx);
		}
	}
	
	public String toString() {
		String str = "";
		if (name != null) {
			str = name + "\n";
		}
		str += "Number of vertices: " + vertices.size() + "\n";
		if (vertices.size() <= 20) {
			for (Vertex v : getVertices()) {
				str += v + "\n";
			}
		}
		str += "Number of edges: " + edges.size() + "\n";
		if (edges.size() <= 20) {
			for (Edge e : getEdges()) {
				str += e + "\n";
			}
		}
		str = str.substring(0, str.length()-1);
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
