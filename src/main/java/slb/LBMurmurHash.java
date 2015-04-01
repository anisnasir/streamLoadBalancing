package slb;

import java.util.Collection;

public class LBMurmurHash implements LoadBalancer {
	private MMHash<Server> hash;
	
	public LBMurmurHash(Collection<Server> nodes) {
		hash = new MMHash<Server>( nodes);
    }
	
	public Server getSever(long timestamp, Object key) {
		return hash.get(key);
	}

}
