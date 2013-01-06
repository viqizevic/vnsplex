package model;

import model.graph.Data;
import model.graph.Key;
import model.graph.Vertex;
import model.network.Network;
import model.network.NetworkEdge;
import model.network.NetworkReader;
import model.network.NetworkReaderException;
import model.network.NetworkVertex;

public class NetworkSimplex {
	
	public static void findMinCostFlow(Network network) {
		Key nettoDemandKey = network.addVertexAttribute("Netto demand");
		NetworkVertex k = new NetworkVertex();
		k.setName("k");
		for (Vertex vertex : network.getVertices()) {
			NetworkVertex v = (NetworkVertex) vertex;
			v.addAttribute(new Data(v.getDemand()), nettoDemandKey);
			System.out.println(v.getAttribute(nettoDemandKey).getValue());
//			NetworkEdge e = new NetworkEdge(k, (NetworkVertex)v);
//			NetworkEdge f = new NetworkEdge((NetworkVertex)v, k);
//			network.addEdge(e);
//			network.addEdge(f);
		}
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
