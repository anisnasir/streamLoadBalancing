package slb;

import java.util.Collection;

public class LBConsistentHash implements LoadBalancer {
	private ConsistentHash<Server> hash;
	
	public LBConsistentHash(int numberOfReplicas, Collection<Server> nodes) {
		hash = new ConsistentHash<Server>(numberOfReplicas, nodes);
    }


	public Server getSever(long timestamp, Object key) {
		return hash.get(key);
	}

}
