package model.network;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import model.graph.Edge;

public class NetworkSolutionWriter {
	
	public static void write(Network network, String fileName) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
			bw.write(network.computeTotalCost()+"\n");
			for (Edge edge : network.getEdges()) {
				NetworkEdge e = (NetworkEdge) edge;
				String line = e.getTail().getName() + " " + e.getHead().getName();
				line += " " + e.getFlow() + "\n";
				bw.write(line);
			}
			bw.flush();
			bw.close();
		} catch (IOException e) {
			String errMsg = "Error while writing file: " + fileName;
			System.err.println(errMsg);
			e.printStackTrace();
		}
	}

}
