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
	
	public static void findMinCostFlow(Network network) {
		
		// extend the network
		long bigM = network.getBigM();
		// add a new vertex k to the network
		NetworkVertex k = new NetworkVertex();
		k.setName("k");
		k.setDemand(0L);
		network.addVertex(k);
		for (Vertex vertex : network.getVertices()) {
			NetworkVertex v = (NetworkVertex) vertex;
			if (v != k) {
				// compute net demand (Nettobedarf: Mindestbedarf - Mindestlieferung)
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
				// depends on the net demand,
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
		
		// set T, L and U
		LinkedList<NetworkEdge> tEdges = new LinkedList<NetworkEdge>();
		LinkedList<NetworkEdge> lEdges = new LinkedList<NetworkEdge>();
		LinkedList<NetworkEdge> uEdges = new LinkedList<NetworkEdge>();
		for (Edge edge : network.getEdges()) {
			NetworkEdge e = (NetworkEdge) edge;
			if (e.getTail() == k || e.getHead() == k) {
				tEdges.add(e);
			} else {
				lEdges.add(e);
			}
		}
		
		// set the flow x
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
		
		System.out.println(network);
	}
	
	public static void main(String[] args) {
		Network network;
		try {
//			network = NetworkReader.read("files/gte_bad.20.txt");
			network = NetworkReader.read("files/test.txt");
			NetworkSimplex.findMinCostFlow(network);
			if (!network.isConsistent()) {
				System.err.println("Network is not consistent");
			}
		} catch (NetworkReaderException e) {
			e.printStackTrace();
		}
	}

}
