package slb;

import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

public class LBSimpleConsistentHash implements LoadBalancer {
	private final SortedMap<Integer, Server> circle = new TreeMap<Integer, Server>();
	private int numServers;
	
	public LBSimpleConsistentHash(Collection<Server> nodes) {
		this.numServers = nodes.size();
		int i=0;
		for (Server node : nodes)
			{
				circle.put(i, node);
				i++;
			}		
    }

	public Server getSever(long timestamp, Object key) {
		int serverID = Math.abs(key.toString().hashCode())%this.numServers;
		return this.circle.get(serverID);
	}

}
