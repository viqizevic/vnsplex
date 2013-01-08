package model.network;

import model.graph.Graph;
import model.graph.Key;

/**
 * The Network class implements a network.
 */
public class Network extends Graph {
	
	protected static Key DEMAND_KEY;
	protected static Key LOWER_BOUND_KEY;
	protected static Key CAPACITY_KEY;
	protected static Key COST_KEY;
	protected static Key FLOW_KEY;

	public Network() {
		super();
		setDirected(true);
		setName("Network");
		DEMAND_KEY      = addVertexData("Demand");
		LOWER_BOUND_KEY = addEdgeData("Lower bound of capacity");
		CAPACITY_KEY    = addEdgeData("Capacity");
		COST_KEY        = addEdgeData("Cost");
		FLOW_KEY        = addEdgeData("Flow");
	}
	
	public void addVertex(NetworkVertex v) {
		super.addVertex(v);
	}
	
	public void addEdge(NetworkEdge e) {
		super.addEdge(e);
	}
	
}