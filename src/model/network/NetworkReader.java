package model.network;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * The NetworkReader class provides a method to create a {@link Network} object
 * described in a file.
 */
public class NetworkReader {
	
	public static Network read(String fileName) throws NetworkReaderException {
		Network network = new Network();
		network.setSimple(true);
		int n = 0;
		int m = 0;
		NetworkVertex[] nodes = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String line = br.readLine();
			while (line != null) {
				if (line.startsWith("p min ")) {
					String[] p = line.split(" ");
					if (p.length != 4 || n != 0) {
						String errMsg = "Unexpected line: " + line;
						if (n != 0) {
							errMsg += ". More than one problem line found.";
						}
						throw new NetworkReaderException(errMsg);
					}
					n = Integer.parseInt(p[2]);
					m = Integer.parseInt(p[3]);
					nodes = new NetworkVertex[n+1];
					for (int i=1; i<=n; i++) {
						nodes[i] = new NetworkVertex();
						network.addVertex(nodes[i]);
						nodes[i].setName(""+i);
						nodes[i].setDemand(0L);
					}
				} else if (line.startsWith("n ")) {
					if (nodes == null) {
						String errMsg = "Node descriptor line appears before any problem line";
						throw new NetworkReaderException(errMsg);
					}
					String[] nodeInfo = line.split(" ");
					if (nodeInfo.length != 3) {
						String errMsg = "Unexpected line: " + line;
						throw new NetworkReaderException(errMsg);
					}
					int id = Integer.parseInt(nodeInfo[1]);
					long demand = Long.parseLong(nodeInfo[2]);
					nodes[id].setDemand(demand);
				} else if (line.startsWith("a ")) {
					if (nodes == null) {
						String errMsg = "Arc descriptor line appears before any problem line or node descriptor line";
						throw new NetworkReaderException(errMsg);
					}
					String[] arcInfo = line.split(" ");
					int v = Integer.parseInt(arcInfo[1]);
					int w = Integer.parseInt(arcInfo[2]);
					long low = Long.parseLong(arcInfo[3]);
					long cap = Long.parseLong(arcInfo[4]);
					long cost = Long.parseLong(arcInfo[5]);
					NetworkEdge e = new NetworkEdge(nodes[v], nodes[w]);
					e.setLowerBound(low);
					e.setCapacity(cap);
					e.setCost(cost);
					network.addEdge(e);
				}
				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			String errMsg = "File not found: " + fileName;
			throw new NetworkReaderException(errMsg);
		} catch (IOException e) {
			String errMsg = "Error while reading file: " + fileName;
			throw new NetworkReaderException(errMsg);
		}
		int numbOfEdges = network.getEdges().size();
		if (m != numbOfEdges) {
			String errMsg = "Expected number of edges: " + m + ", ";
			errMsg += "Number of edges found: " + numbOfEdges;
			throw new NetworkReaderException(errMsg);
		}
		return network;
	}

}