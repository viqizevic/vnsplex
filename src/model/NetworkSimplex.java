package model;

import java.util.LinkedList;

import model.graph.Data;
import model.graph.Edge;
import model.graph.Key;
import model.graph.Vertex;
import model.network.Network;
import model.network.NetworkEdge;
import model.network.NetworkReader;
import model.network.NetworkReaderException;
import model.network.NetworkVertex;

public class NetworkSimplex {
	
	private static Network network;
	
	private static LinkedList<NetworkEdge> tEdges;
	
	private static LinkedList<NetworkEdge> lEdges;
	
	private static LinkedList<NetworkEdge> uEdges;
	
	private static Key reducedCostDataKey;
	
	public static void findMinCostFlow(Network n) {
		
		NetworkSimplex.network = n;
		
		// Extend the network
		long bigM = network.getBigM();
		// Add a new vertex k to the network
		NetworkVertex k = new NetworkVertex();
		k.setName("k");
		k.setDemand(0L);
		network.addVertex(k);
		for (Vertex vertex : network.getVertices()) {
			NetworkVertex v = (NetworkVertex) vertex;
			if (v != k) {
				// Compute net demand (Nettobedarf: Mindestbedarf - Mindestlieferung)
				// b'(v) = b(v) + l(delta_p(v)) - l(delta_m(v))
				long b = v.getDemand();
				long nb = b;
				for (Edge edge : v.getOutgoingEdges()) {
					NetworkEdge e = (NetworkEdge) edge;
					nb += e.getLowerBound();
				}
				for (Edge edge : v.getIngoingEdges()) {
					NetworkEdge e = (NetworkEdge) edge;
					nb -= e.getLowerBound();
				}
				// Depends on the net demand,
				// add a new edge (v,k) or (k,v) to the network
				NetworkEdge e;
				if (nb < 0) {
					e = new NetworkEdge(v,k);
				} else {
					e = new NetworkEdge(k,v);
				}
				e.setLowerBound(0L);
				e.setCapacity(Long.MAX_VALUE);
				e.setCost(bigM);
				network.addEdge(e);
			}
		}
		
		// Set T, L and U
		tEdges = new LinkedList<NetworkEdge>();
		lEdges = new LinkedList<NetworkEdge>();
		uEdges = new LinkedList<NetworkEdge>();
		for (Edge edge : network.getEdges()) {
			NetworkEdge e = (NetworkEdge) edge;
			if (e.getTail() == k || e.getHead() == k) {
				tEdges.add(e);
			} else {
				lEdges.add(e);
			}
		}
		
		// Set the flow x
		for (NetworkEdge e : lEdges) {
			e.setFlow(e.getLowerBound());
		}
		for (NetworkEdge e : tEdges) {
			NetworkVertex v = (NetworkVertex) e.getTail();
			if (v != k) {
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
			} else {
				v = (NetworkVertex) e.getHead();
				long x = v.getDemand();
				for (Edge edge : v.getIngoingEdges()) {
					if (edge != e) {
						NetworkEdge ingoingEdge = (NetworkEdge) edge;
						x -= ingoingEdge.getFlow();
					}
				}
				for (Edge edge : v.getOutgoingEdges()) {
					if (edge != e) {
						NetworkEdge outgoingEdge = (NetworkEdge) edge;
						x += outgoingEdge.getFlow();
					}
				}
				e.setFlow(x);
			}
		}
		
		// Set the vertex prices
		Key vertexPriceDataKey = network.addVertexData("Vertex price");
		k.addData(new Data(0L), vertexPriceDataKey);
		for (Edge edge : k.getOutgoingEdges()) {
			NetworkEdge e = (NetworkEdge) edge;
			long cost = e.getCost();
			e.getHead().addData(new Data(cost), vertexPriceDataKey);
		}
		for (Edge edge : k.getIngoingEdges()) {
			NetworkEdge e = (NetworkEdge) edge;
			long cost = e.getCost();
			e.getTail().addData(new Data(-cost), vertexPriceDataKey);
		}
		
		// Set the reduced cost
		reducedCostDataKey = network.addEdgeData("Reduced cost");
		for (Edge edge : network.getEdges()) {
			NetworkEdge e = (NetworkEdge) edge;
			long c = e.getCost();
			long yTail = (Long) e.getTail().getData(vertexPriceDataKey).getValue();
			long yHead = (Long) e.getHead().getData(vertexPriceDataKey).getValue();
			e.addData(new Data(c + yTail - yHead), reducedCostDataKey);
		}
		
		// While entering edge exists
		int i=1;
		while (enteringEdgeExists() && i <= 1) {
			
			// Choose an entering edge e
			
			// Find the cycle C in T+e
			
			// Compute epsilon
			
			// Update the flow
			
			// Find a leaving edge f
			
			// Update T, L and U
			
			i++;
		}
		
		// Test optimality
		
		
	}
	
	/**
	 * Check if an entering edge exists.
	 * The entering edge is an edge in L with negative reduced cost
	 * or an edge in U with positive reduced cost.
	 * @return <code>true</code> if an entering edge exists, <code>false</code> otherwise.
	 */
	private static boolean enteringEdgeExists() {
		for (NetworkEdge e : lEdges) {
			long rc = (Long) e.getData(reducedCostDataKey).getValue();
			if (rc < 0) {
				return true;
			}
		}
		for (NetworkEdge e : uEdges) {
			long rc = (Long) e.getData(reducedCostDataKey).getValue();
			if (rc > 0) {
				return true;
			}
		}
		return false;
	}
	
	public static void main(String[] args) {
		Network network;
		try {
//			network = NetworkReader.read("files/gte_bad.20.txt");
			network = NetworkReader.read("files/test.txt");
			NetworkSimplex.findMinCostFlow(network);
			System.out.println(network);
		} catch (NetworkReaderException e) {
			e.printStackTrace();
		}
	}

}
