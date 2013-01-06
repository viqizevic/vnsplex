package model.network;

import model.graph.Data;
import model.graph.Edge;

public class NetworkEdge extends Edge {
	
	public NetworkEdge(NetworkVertex tail, NetworkVertex head) {
		super(tail, head);
	}
	
	public void setFlow(long value) {
		addAttribute(new Data(value), Network.FLOW_KEY);
	}
	
	public void setLowerBound(long value) {
		addAttribute(new Data(value), Network.LOWER_BOUND_KEY);
	}
	
	public void setCapacity(long value) {
		addAttribute(new Data(value), Network.CAPACITY_KEY);
	}
	
	public void setCost(long value) {
		addAttribute(new Data(value), Network.COST_KEY);
	}

}