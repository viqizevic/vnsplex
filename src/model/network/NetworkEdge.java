package model.network;

import model.graph.Data;
import model.graph.Edge;

public class NetworkEdge extends Edge {
	
	public NetworkEdge(NetworkVertex tail, NetworkVertex head) {
		super(tail, head);
	}
	
	public void setFlow(long value) {
		addData(new Data(value), Network.FLOW_KEY);
	}
	
	public long getFlow() {
		return (Long) getData(Network.FLOW_KEY).getValue();
	}
	
	public void setLowerBound(long value) {
		addData(new Data(value), Network.LOWER_BOUND_KEY);
	}
	
	public long getLowerBound() {
		return (Long) getData(Network.LOWER_BOUND_KEY).getValue();
	}
	
	public void setCapacity(long value) {
		addData(new Data(value), Network.CAPACITY_KEY);
	}
	
	public long getCapacity() {
		return (Long) getData(Network.CAPACITY_KEY).getValue();
	}
	
	public void setCost(long value) {
		addData(new Data(value), Network.COST_KEY);
	}
	
	public long getCost() {
		return (Long) getData(Network.COST_KEY).getValue();
	}

}