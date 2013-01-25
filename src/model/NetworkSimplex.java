package model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;

import model.graph.Data;
import model.graph.Edge;
import model.graph.Key;
import model.graph.Vertex;
import model.network.Network;
import model.network.NetworkEdge;
import model.network.NetworkReader;
import model.network.NetworkReaderException;
import model.network.NetworkSolutionWriter;
import model.network.NetworkVertex;

public class NetworkSimplex {
	
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage: java -jar netsimplex.jar <fileinput> <fileoutput>");
			return;
		}
//		String fileInput = args[0];
		String fileInput = "files/test.txt";
		String fileOutput = args[1];
		Network network;
		try {
			network = NetworkReader.read(fileInput);
			System.out.println("Succesfully read " + fileInput);
			System.out.println("Created:");
			System.out.println(network);
			System.out.println();
			inDebugMode = true;
			NetworkSimplex.findMinCostFlow(network);
			System.out.println(network);
//			NetworkSolutionWriter.write(network, fileOutput);
//			System.out.println("Succesfully write " + fileOutput);
		} catch (NetworkReaderException e) {
			e.printStackTrace();
		}
	}
	
	private static Network network;
	
	private static HashMap<Key, NetworkEdge> lEdges;
	
	private static HashMap<Key, NetworkEdge> uEdges;
	
	/**
	 * Spanning tree.
	 */
	private static NetworkEdge[] tree;
	
	/**
	 * Predecessor index in the tree
	 */
	private static int[] p;
	
	/**
	 * Depth index in the tree
	 */
	private static int[] d;
	
	/**
	 * Successor index in the tree based on depth first search traversion
	 */
	private static int[] s;
	
	/**
	 * Dummy root
	 */
	private static NetworkVertex root;
	
	private static Key vertexPriceDataKey;
	
	private static Key reducedCostDataKey;
	
	private static boolean inDebugMode;
	
	public static void findMinCostFlow(Network network) {
		
		NetworkSimplex.network = network;
		
		extendNetwork();
		
		defineSpanningTreeDataStructures();
		
		// Set the flow x
		computeInitialFlows();
		
		// Set the vertex prices
		setInitialVertexPrices();
		
		// Set the reduced costs
		setInitialReducedCosts();
		
		if (inDebugMode) {
			System.out.println(network);
			System.out.println();
			printTreeAndLowerAndUpperEdges();
			System.out.println();
		}
		
		// While entering edge exists
		int i=1;
		while (enteringEdgeExists() && i<=5) {
			
			// Choose an entering edge
			NetworkEdge enteringEdge = chooseAnEnteringEdge();
			
			if (inDebugMode) {
				System.out.println("------------ Iteration " + i + " -------------");
				System.out.println(network);
				System.out.println("Entering edge: " + enteringEdge);
			}
			
			// Find the apex w of the cycle
			// Find the cycle C in T + enteringEdge
			NetworkVertex apex = findApexOfTheCircle(enteringEdge);
			LinkedList<NetworkEdge> circle = findCircle(enteringEdge, apex);
			
			// Compute epsilon
			long epsilon = computeEpsilon(circle, apex);
			
			if (inDebugMode) {
				System.out.println("Apex: " + apex);
				for (NetworkEdge e : circle) {
					System.out.println("Edge in circle: "+e);
				}
				System.out.println("epsilon = " + epsilon);
			}
			
			// Update the flows in circle
			updateFlowsInCircle(circle, apex, epsilon);
			
			// Find leavingEdge
			NetworkEdge leavingEdge = findLeavingEdge(circle);

			if (inDebugMode) {
				System.out.println("Leaving edge: " + leavingEdge);
			}
			
			// T splits into two subtrees, if we remove leaving edge from T
			// Let T1 be the subtree containing the root k and T2 := T \ T1
			// Find subtree T2
			LinkedList<NetworkVertex> subtreeT2 = findSubtreeT2(leavingEdge);
			
			if (inDebugMode) {
				for (NetworkVertex vertex : subtreeT2) {
					System.out.println("Vertex in T2: " + vertex);
				}
			}
			
			// Update T, L and U
			updateTreeAndLowerAndUpperEdges(enteringEdge, leavingEdge, subtreeT2);
			
			// Update p, d and s
			updateTheDataStructuresPDS(enteringEdge, leavingEdge, subtreeT2);
			
			if (leavingEdge != enteringEdge) {
				// Update vertex prices and reduced costs in T2
				updateVertexPricesAndReducedCostsInSubtreeT2(subtreeT2, enteringEdge);
			}
			
			if (inDebugMode) {
				printTreeAndLowerAndUpperEdges();
				System.out.println();
			}
			
			i++;
		}
		
		// Test optimality
		
		network.removeVertex(root);
		network.removeVertexData(vertexPriceDataKey);
		network.removeEdgeData(reducedCostDataKey);
		
	}

	private static NetworkEdge chooseAnEnteringEdge() {
		int maximumNumberOfCandidates = 5;
		LinkedList<NetworkEdge> candidates = new LinkedList<NetworkEdge>();
		for (NetworkEdge e : lEdges.values()) {
			long rc = (Long) e.getData(reducedCostDataKey).getValue();
			if (rc < 0) {
				candidates.add(e);
			}
			if (candidates.size() >= maximumNumberOfCandidates) {
				break;
			}
		}
		if (candidates.size() < maximumNumberOfCandidates) {
			for (NetworkEdge e : uEdges.values()) {
				long rc = (Long) e.getData(reducedCostDataKey).getValue();
				if (rc > 0) {
					candidates.add(e);
				}
				if (candidates.size() >= maximumNumberOfCandidates) {
					break;
				}
			}
		}
		NetworkEdge enteringEdge = null;
		long rc = 0L;
		for (NetworkEdge e : candidates) {
			long erc = Math.abs((Long) e.getData(reducedCostDataKey).getValue());
			if (rc < erc) {
				rc = erc;
				enteringEdge = e;
			}
		}
		return enteringEdge;
	}

	private static NetworkVertex findApexOfTheCircle(NetworkEdge enteringEdge) {
		int u = ((NetworkVertex) enteringEdge.getTail()).getId();
		int v = ((NetworkVertex) enteringEdge.getHead()).getId();
		while (u != v) {
			if (d[u] > d[v]) {
				u = p[u];
			} else {
				v = p[v];
			}
		}
		return network.getVertex(u);
	}

	private static LinkedList<NetworkEdge> findCircle(NetworkEdge enteringEdge, NetworkVertex apex) {
		LinkedList<NetworkEdge> circle = new LinkedList<NetworkEdge>();
		int u = ((NetworkVertex) enteringEdge.getTail()).getId();
		int v = ((NetworkVertex) enteringEdge.getHead()).getId();
		int w = apex.getId();
		while (u != w) {
			circle.addFirst(tree[u]);
			u = p[u];
		}
		circle.add(enteringEdge);
		while (v != w) {
			circle.add(tree[v]);
			v = p[v];
		}
		return circle;
	}

	private static long computeEpsilon(LinkedList<NetworkEdge> circle, NetworkVertex apex) {
		long epsilon = Long.MAX_VALUE;
		NetworkVertex w = apex;
		for (NetworkEdge e : circle) {
			long r;
			if (e.getTail() == w) {
				// e is a forward edge
				r = e.getCapacity() - e.getFlow();
				w = (NetworkVertex) e.getHead();
			} else {
				// e is a backward edge
				r = e.getFlow() - e.getLowerBound();
				w = (NetworkVertex) e.getTail();
			}
			epsilon = Math.min(r, epsilon);
		}
		return epsilon;
	}

	private static void updateFlowsInCircle(LinkedList<NetworkEdge> circle,
			NetworkVertex apex, long epsilon) {
		if (epsilon == 0L) {
			return;
		}
		NetworkVertex w = apex;
		for (NetworkEdge e : circle) {
			if (e.getTail() == w) {
				// e is a forward edge
				e.setFlow(e.getFlow()+epsilon);
				w = (NetworkVertex) e.getHead();
			} else {
				// e is a backward edge
				e.setFlow(e.getFlow()-epsilon);
				w = (NetworkVertex) e.getTail();
			}
		}
	}

	private static NetworkEdge findLeavingEdge(LinkedList<NetworkEdge> circle) {
		NetworkEdge leavingEdge = null;
		for (NetworkEdge e : circle) {
			if (e.getFlow() == e.getLowerBound() || e.getFlow() == e.getCapacity()) {
				leavingEdge = e;
			}
		}
		return leavingEdge;
	}

	private static LinkedList<NetworkVertex> findSubtreeT2(NetworkEdge leavingEdge) {
		LinkedList<NetworkVertex> subtreeT2 = new LinkedList<NetworkVertex>();
		NetworkVertex z = (NetworkVertex) leavingEdge.getTail();
		NetworkVertex y = (NetworkVertex) leavingEdge.getHead();
		if (d[z.getId()] > d[y.getId()]) {
			// Make sure that y is the node located deeper than z in the tree
			y = z;
		}
		subtreeT2.add(y);
		int m = s[y.getId()];
		while (d[y.getId()] < d[m]) {
			NetworkVertex successor = network.getVertex(m);
			subtreeT2.add(successor);
			m = s[m];
		}
		return subtreeT2;
	}

	private static void updateVertexPricesAndReducedCostsInSubtreeT2(
			LinkedList<NetworkVertex> subtreeT2, NetworkEdge enteringEdge) {
		long change = -1 * (Long) enteringEdge.getData(reducedCostDataKey).getValue();
		// Let e = (u,v). If u in T2 then multiply change with -1
		if (subtreeT2.contains(enteringEdge.getTail())) {
			change = -1 * change;
		}
		for (NetworkVertex vertex : subtreeT2) {
			long price = ((Long) vertex.getData(vertexPriceDataKey).getValue()) + change;
			vertex.addData(new Data(price), vertexPriceDataKey);
		}
		for (NetworkVertex vertex : subtreeT2) {
			for (Edge edge : vertex.getOutgoingEdges()) {
				NetworkEdge e = (NetworkEdge) edge;
				computeReducedCost(e);
			}
			for (Edge edge : vertex.getIngoingEdges()) {
				NetworkEdge e = (NetworkEdge) edge;
				computeReducedCost(e);
			}
		}
	}
	
	private static void updateTreeAndLowerAndUpperEdges(
			NetworkEdge enteringEdge, NetworkEdge leavingEdge, LinkedList<NetworkVertex> subtreeT2) {
		
		// remove entering edge from L or U
		if (lEdges.containsKey(enteringEdge.getKey())) {
			lEdges.remove(enteringEdge.getKey());
		} else {
			uEdges.remove(enteringEdge.getKey());
		}
		
		// add leaving edge to L or U
		if (leavingEdge.getFlow() == leavingEdge.getLowerBound()) {
			lEdges.put(leavingEdge.getKey(), leavingEdge);
		} else {
			uEdges.put(leavingEdge.getKey(), leavingEdge);
		}

		if (leavingEdge == enteringEdge) {
			// no need to change T
			return;
		}

		int v = ((NetworkVertex) enteringEdge.getHead()).getId();
		// Let v be the node located in the subtree T2
		if (!subtreeT2.contains(enteringEdge.getHead())) {
			v = ((NetworkVertex) enteringEdge.getTail()).getId();
		}
		int z = ((NetworkVertex) leavingEdge.getTail()).getId();
		int y = ((NetworkVertex) leavingEdge.getHead()).getId();
		if (d[z] > d[y]) {
			// Make sure that y is the node located deeper than z in the tree
			y = z;
		}
		
		// remove leaving edge from T
		// by updating the array tree
		int w = v;
		NetworkEdge e = tree[w];
		while (w != y) {
			int parent = p[w];
			NetworkEdge edgeToParent = tree[parent];
			tree[parent] = e;
			e = edgeToParent;
			w = parent;
		}
		// add entering edge to T
		tree[v] = enteringEdge;
		
//		for (NetworkEdge edge : tree) {
//			System.out.println(edge);
//		}
		
	}
	
	private static void updateTheDataStructuresPDS(NetworkEdge enteringEdge,
			NetworkEdge leavingEdge, LinkedList<NetworkVertex> subtreeT2) {
		
		if (leavingEdge == enteringEdge) {
			return;
		}
		
		int u = ((NetworkVertex) enteringEdge.getTail()).getId();
		int v = ((NetworkVertex) enteringEdge.getHead()).getId();
		// Let v be the node located in the subtree T2
		if (!subtreeT2.contains(enteringEdge.getHead())) {
			v = u;
			u = ((NetworkVertex) enteringEdge.getHead()).getId();
		}
		int z = ((NetworkVertex) leavingEdge.getTail()).getId();
		int y = ((NetworkVertex) leavingEdge.getHead()).getId();
		if (d[z] > d[y]) {
			// Make sure that y is the node located deeper than z in the tree
			y = z;
			z = ((NetworkVertex) leavingEdge.getHead()).getId();
		}
		
		// update the p and d index for all nodes between v and y
		int x = p[v];
		int w = v;
		p[v] = u;
		d[v] = d[u] + 1;
		while (w != y) {
			int tmp = p[x];
			p[x] = w;
			d[x] = d[w] + 1;
			w = x;
			x = tmp;
		}
		
		// update the depth index for all vertex in subtree T2
		int succ = s[y];
		int lastVertexIdInSubtree = subtreeT2.getLast().getId();
		while (succ != s[lastVertexIdInSubtree]) {
			d[succ] = d[p[succ]] + 1;
			succ = s[succ];
		}
		
		// create a new network representing the subtree T2
		Network subtreeT2Network = new Network();
		subtreeT2Network.setDirected(false);
		subtreeT2Network.setName("T2");
		for (NetworkVertex vertex : subtreeT2) {
			NetworkVertex newVertex = new NetworkVertex();
			newVertex.setName(vertex.getName());
			subtreeT2Network.addVertex(newVertex, vertex.getId());
		}
		for (NetworkVertex vertex : subtreeT2) {
			NetworkVertex newVertex = subtreeT2Network.getVertex(vertex.getId());
			NetworkVertex newVertexParent = subtreeT2Network.getVertex(p[vertex.getId()]);
			if (newVertex != null && newVertexParent != null) {
				subtreeT2Network.addEdge(new NetworkEdge(newVertex, newVertexParent));
			}
		}
		// perform DFS in the subtree T2 starting from the node v
		NetworkVertex[] verticesOfSubtreeT2SortedByDFS = new NetworkVertex[subtreeT2Network.getNumberOfVertices()];
		int j = 0;
		Stack<NetworkVertex> stack = new Stack<NetworkVertex>();
		Key vertexExploredKey = subtreeT2Network.addVertexData("Explored");
		NetworkVertex nv = subtreeT2Network.getVertex(v);
		stack.push(nv);
		while (!stack.isEmpty()) {
			nv = stack.pop();
			nv.addData(new Data(true), vertexExploredKey);
			verticesOfSubtreeT2SortedByDFS[j] = nv;
			j++;
			for (Edge outgoingEdge : nv.getOutgoingEdges()) {
				NetworkVertex nw = (NetworkVertex) outgoingEdge.getHead();
				if (nw.getData(vertexExploredKey).getValue() == null) {
					nw.addData(new Data(true), vertexExploredKey);
					stack.push(nw);
				}
			}
		}
		
		// update the successor index
		int h = z;
		// let h be the last vertex before y in the traversal order (s[h] = y)
		while (s[h] != y) {
			h = s[h];
		}
		s[h] = s[lastVertexIdInSubtree];
		int len = verticesOfSubtreeT2SortedByDFS.length;
		for (j=0; j<len-1; j++) {
			nv = verticesOfSubtreeT2SortedByDFS[j];
			s[nv.getId()] = verticesOfSubtreeT2SortedByDFS[j+1].getId();
		}
		s[verticesOfSubtreeT2SortedByDFS[len-1].getId()] = s[u];
		s[u] = v;
		
	}
	
	/**
	 * Extends network by adding a root vertex and edges between this root and all nodes.
	 */
	private static void extendNetwork() {
		
		int n = network.getNumberOfVertices();
		
		// Compute M
		long bigM = 1;
		long maxCost = 0;
		for (Edge edge : network.getEdges()) {
			NetworkEdge e = (NetworkEdge) edge;
			long cost = Math.abs(e.getCost());
			if (maxCost < cost) {
				maxCost = cost;
			}
		}
		bigM = 1 + Math.round(0.5*n*maxCost);
		
		// Add a new vertex k to the network
		root = new NetworkVertex();
		root.setName("0");
		root.setDemand(0L);
		network.addVertex(root, 0); // we give k the id 0
		n = n+1;
		for (Vertex vertex : network.getVertices()) {
			NetworkVertex v = (NetworkVertex) vertex;
			if (v != root) {
				// Compute net demand (Nettobedarf: Mindestlieferung - Mindestbedarf)
				// b'(v) = b(v) - l(delta_p(v)) + l(delta_m(v))
				// delta_p(v) := {outgoing edges of v}
				// delta_m(v) := {ingoing edges of v}
				long b = v.getDemand();
				long nb = b;
				for (Edge edge : v.getOutgoingEdges()) {
					NetworkEdge e = (NetworkEdge) edge;
					nb -= e.getLowerBound();
				}
				for (Edge edge : v.getIngoingEdges()) {
					NetworkEdge e = (NetworkEdge) edge;
					nb += e.getLowerBound();
				}
				// Depends on the net demand,
				// add a new edge (v,k) or (k,v) to the network
				NetworkEdge e;
				if (nb > 0) {
					e = new NetworkEdge(v,root);
				} else {
					e = new NetworkEdge(root,v);
				}
				e.setLowerBound(0L);
				e.setCapacity(Long.MAX_VALUE);
				e.setCost(bigM);
				network.addEdge(e);
			}
		}
	}
	
	/**
	 * Initializes data structures needed.
	 */
	private static void defineSpanningTreeDataStructures() {
		
		int n = network.getNumberOfVertices();
		
		// Set T, L, U, p, d, s
		lEdges = new HashMap<Key, NetworkEdge>();
		uEdges = new HashMap<Key, NetworkEdge>();
		tree = new NetworkEdge[n];
		p = new int[n];
		d = new int[n];
		/*
		 * depth first search traversal at first:
		 * 0-1-2-3-4-...-0
		 * for every pair -i-j- above we set s(i) := j
		 */
		s = new int[n];
		for (Edge edge : network.getEdges()) {
			NetworkEdge e = (NetworkEdge) edge;
			if (e.getTail() == root || e.getHead() == root) {
				// let v be the vertex not equals to k
				Vertex v = e.getTail();
				if (v == root) {
					v = e.getHead();					
				}
				int i = Integer.parseInt(v.getName());
				tree[i] = e; // e is the edge between i and p[i]
				p[i] = 0;
				d[i] = 1;
				s[i] = i+1;
			} else {
				lEdges.put(e.getKey(), e);
			}
		}
		s[0] = 1;
		s[n-1] = 0;
	}
	
	/**
	 * Computes the initial flow in the network.
	 */
	private static void computeInitialFlows() {
		
		for (NetworkEdge e : lEdges.values()) {
			e.setFlow(e.getLowerBound());
		}
		for (int i=1; i<tree.length; i++) {
			NetworkEdge e = tree[i];
			NetworkVertex v = (NetworkVertex) e.getTail();
			if (v != root) {
				long x = v.getDemand();
				for (Edge edge : v.getIngoingEdges()) {
					if (edge != e) {
						NetworkEdge ingoingEdge = (NetworkEdge) edge;
						x += ingoingEdge.getFlow();
					}
				}
				for (Edge edge : v.getOutgoingEdges()) {
					if (edge != e) {
						NetworkEdge outgoingEdge = (NetworkEdge) edge;
						x -= outgoingEdge.getFlow();
					}
				}
				e.setFlow(x);
			} else {
				v = (NetworkVertex) e.getHead();
				long x = -v.getDemand();
				for (Edge edge : v.getIngoingEdges()) {
					if (edge != e) {
						NetworkEdge ingoingEdge = (NetworkEdge) edge;
						x += ingoingEdge.getFlow();
					}
				}
				for (Edge edge : v.getOutgoingEdges()) {
					if (edge != e) {
						NetworkEdge outgoingEdge = (NetworkEdge) edge;
						x -= outgoingEdge.getFlow();
					}
				}
				e.setFlow(x);
			}
		}
	}

	private static void setInitialVertexPrices() {
		vertexPriceDataKey = network.addVertexData("Vertex price");
		root.addData(new Data(0L), vertexPriceDataKey);
		for (Edge edge : root.getOutgoingEdges()) {
			NetworkEdge e = (NetworkEdge) edge;
			long cost = e.getCost();
			e.getHead().addData(new Data(-cost), vertexPriceDataKey);
		}
		for (Edge edge : root.getIngoingEdges()) {
			NetworkEdge e = (NetworkEdge) edge;
			long cost = e.getCost();
			e.getTail().addData(new Data(cost), vertexPriceDataKey);
		}
//		computeVertexPrices(network);
	}

	private static void setInitialReducedCosts() {
		reducedCostDataKey = network.addEdgeData("Reduced cost");
		for (Edge edge : network.getEdges()) {
			NetworkEdge e = (NetworkEdge) edge;
			computeReducedCost(e);
		}
	}
	
	private static void computeReducedCost(NetworkEdge e) {
		long yTail = (Long) e.getTail().getData(vertexPriceDataKey).getValue();
		long yHead = (Long) e.getHead().getData(vertexPriceDataKey).getValue();
		long cost = e.getCost();
		e.addData(new Data(cost - yTail + yHead), reducedCostDataKey);
	}
	
	private static void computeVertexPrices(Network network) {
		network.getVertex(0).addData(new Data(0L), vertexPriceDataKey);
		int j = s[0];
		while (j != 0) {
			int i = p[j];
			NetworkEdge e = tree[j];
			// if e = (i,j)
			if (network.getVertex(i) == e.getTail()) {
				long price = ((Long) network.getVertex(i).getData(vertexPriceDataKey).getValue()) - e.getCost();
				network.getVertex(j).addData(new Data(price), vertexPriceDataKey);
			} else { // e = (j,i)
				long price = ((Long) network.getVertex(i).getData(vertexPriceDataKey).getValue()) + e.getCost();
				network.getVertex(j).addData(new Data(price), vertexPriceDataKey);
			}
			j = s[j];
		}
	}
	
	/**
	 * Check if an entering edge exists.
	 * The entering edge is an edge in L with negative reduced cost
	 * or an edge in U with positive reduced cost.
	 * @return <code>true</code> if an entering edge exists, <code>false</code> otherwise.
	 */
	private static boolean enteringEdgeExists() {
		for (NetworkEdge e : lEdges.values()) {
			long rc = (Long) e.getData(reducedCostDataKey).getValue();
			if (rc < 0) {
				return true;
			}
		}
		for (NetworkEdge e : uEdges.values()) {
			long rc = (Long) e.getData(reducedCostDataKey).getValue();
			if (rc > 0) {
				return true;
			}
		}
		return false;
	}

	private static void printTreeAndLowerAndUpperEdges() {
		int max = 5;
		if (lEdges.size() <= max) {
			for (NetworkEdge e : lEdges.values()) {
				System.out.println("In L: " + e);
			}
		} else {
			System.out.println("|L| = " + lEdges.size());
		}
		if (uEdges.size() <= max) {
			for (NetworkEdge e : uEdges.values()) {
				System.out.println("In U: " + e);
			}
		} else {
			System.out.println("|U| = " + uEdges.size());
		}
		if (tree.length <= max) {
			for (int i=1; i<tree.length; i++) {
				NetworkEdge e = tree[i];
				System.out.println("In T: " + e);
			}
			System.out.println("p: " + Arrays.toString(p));
			System.out.println("d: " + Arrays.toString(d));
			System.out.println("s: " + Arrays.toString(s));
		}
	}

}
