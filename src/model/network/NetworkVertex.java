package model.network;

import model.graph.Data;
import model.graph.Vertex;

public class NetworkVertex extends Vertex {
	
	public int getId() {
		return Integer.parseInt(getName());
	}
	
	public void setDemand(long value) {
		addData(new Data(value), Network.DEMAND_KEY);
	}
	
	public long getDemand() {
		return (Long) getData(Network.DEMAND_KEY).getValue();
	}
	
}
