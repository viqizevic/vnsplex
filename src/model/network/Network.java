package model.network;

import java.util.Collection;

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
		DEMAND_KEY      = addVertexAttribute("Demand");
		LOWER_BOUND_KEY = addEdgeAttribute("Lower bound of capacity");
		CAPACITY_KEY    = addEdgeAttribute("Capacity");
		COST_KEY        = addEdgeAttribute("Cost");
		FLOW_KEY        = addEdgeAttribute("Flow");
	}
	
	public void addVertex(NetworkVertex v) {
		super.addVertex(v);
	}
	
	public void addEdge(NetworkEdge e) {
		super.addEdge(e);
	}
	
}