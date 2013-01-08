package model;

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
		
		// Nettobedarf berechnen
		// b'(v) = b(v) + l(delta_p(v)) - l(delta_m(v))
		// für alle Knoten v
		Key netDemandKey = network.addVertexData("Net demand");
		for (Vertex vertex : network.getVertices()) {
			NetworkVertex v = (NetworkVertex) vertex;
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
			v.addData(new Data(nb), netDemandKey);
//			NetworkEdge e = new NetworkEdge(k, (NetworkVertex)v);
//			NetworkEdge f = new NetworkEdge((NetworkVertex)v, k);
//			network.addEdge(e);
//			network.addEdge(f);
		}
		NetworkVertex k = new NetworkVertex();
		k.setName("k");
		network.addVertex(k);
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
